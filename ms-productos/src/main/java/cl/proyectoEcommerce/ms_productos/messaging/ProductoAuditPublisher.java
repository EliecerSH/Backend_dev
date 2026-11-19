package cl.proyectoEcommerce.ms_productos.messaging;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

// Publica eventos de auditoría de productos en el tópico Kafka "auditoria.eventos" (lo consume ms-auditoria).
// Antes se publicaban en RabbitMQ y los capturaba la cola pedidos360.auditoria.queue (binding "#").
// - El tipo de evento va en el header "tipo-evento" (ej: pedidos360.producto.modificado).
// - Con transacción abierta se envía DESPUÉS del commit: no se audita un cambio que se revirtió.
// - Best effort: si Kafka no está disponible se registra en log y la operación sigue.
@Component
public class ProductoAuditPublisher {

    public static final String PRODUCTO_CREADO = "pedidos360.producto.creado";
    public static final String PRODUCTO_MODIFICADO = "pedidos360.producto.modificado";
    public static final String PRODUCTO_ELIMINADO = "pedidos360.producto.eliminado";
    public static final String STOCK_ACTUALIZADO = "pedidos360.stock.actualizado";
    public static final String STOCK_ERROR = "pedidos360.stock.error";

    private static final Logger log = LoggerFactory.getLogger(ProductoAuditPublisher.class);
    private static final String ORIGEN = "ms-productos";

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public ProductoAuditPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${pedidos360.auditoria.topic:auditoria.eventos}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publicar(String tipoEvento, Object clave, Object payload) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    enviar(tipoEvento, clave, payload);
                }
            });
        } else {
            enviar(tipoEvento, clave, payload);
        }
    }

    private void enviar(String tipoEvento, Object clave, Object payload) {
        try {
            ProducerRecord<String, Object> record =
                    new ProducerRecord<>(topic, String.valueOf(clave), payload);
            record.headers().add("tipo-evento", tipoEvento.getBytes(StandardCharsets.UTF_8));
            record.headers().add("event-id", UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
            record.headers().add("origen", ORIGEN.getBytes(StandardCharsets.UTF_8));
            kafkaTemplate.send(record).whenComplete((r, ex) -> {
                if (ex != null) {
                    log.warn("No se pudo auditar {} ({}): {}", tipoEvento, clave, ex.getMessage());
                }
            });
        } catch (Exception e) {
            log.warn("No se pudo auditar {} ({}): {}", tipoEvento, clave, e.getMessage());
        }
    }
}
