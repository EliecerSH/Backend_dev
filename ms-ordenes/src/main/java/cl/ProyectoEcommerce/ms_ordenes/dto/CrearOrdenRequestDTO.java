package cl.ProyectoEcommerce.ms_ordenes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CrearOrdenRequestDTO(
        @NotEmpty(message = "La orden debe tener al menos un ítem") @Valid List<ItemOrdenRequestDTO> items) {
}
