package cl.ProyectoEcommerce.ms_ordenes.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

// Snapshot de un ítem del carrito enviado por el frontend al momento de "Finalizar compra"
public record ItemOrdenRequestDTO(
        @NotNull(message = "El ID del producto es obligatorio") Long productoId,

        @NotNull(message = "La cantidad es obligatoria") @Min(value = 1, message = "La cantidad debe ser al menos 1") Integer cantidad,

        @NotNull(message = "El precio unitario es obligatorio") @Min(value = 0, message = "El precio unitario no puede ser negativo") BigDecimal precioUnitario) {
}
