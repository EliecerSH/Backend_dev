package cl.proyectoEcommerce.ms_auditoria.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "registros_auditoria")
public class RegistroAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Routing key del evento tal como llegó a RabbitMQ (ej: "orden.creada", "stock.actualizado")
    @Column(name = "tipo_evento", nullable = false, length = 100)
    private String tipoEvento;

    // Contenido íntegro del evento en JSON, tal como fue publicado por el microservicio origen
    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "recibido_en", updatable = false)
    private LocalDateTime recibidoEn;

    public RegistroAuditoria() {
    }

    public RegistroAuditoria(String tipoEvento, String payload) {
        this.tipoEvento = tipoEvento;
        this.payload = payload;
    }

    @PrePersist
    protected void onCreate() {
        this.recibidoEn = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public LocalDateTime getRecibidoEn() { return recibidoEn; }
}
