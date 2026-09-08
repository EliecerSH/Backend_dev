package cl.proyectoEcommerce.ms_productos.exceptions;

import cl.proyectoEcommerce.ms_productos.dto.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> manejarValidaciones(MethodArgumentNotValidException ex) {
        Map<String, String> erroresCampos = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            erroresCampos.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        ErrorResponseDTO response = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Error de Validación",
                "Uno o más campos de la petición no cumplen con los requisitos",
                erroresCampos
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Ahora solo captura el "no encontrado" real
    @ExceptionHandler(ProductoNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> manejarProductoNoEncontrado(ProductoNotFoundException ex) {
        ErrorResponseDTO response = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                "Recurso No Encontrado",
                ex.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // Cualquier otra RuntimeException NO controlada -> 500, no 404
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> manejarExcepcionesGenerales(Exception ex) {
        ErrorResponseDTO response = new ErrorResponseDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error Interno del Servidor",
                "Ocurrió un error inesperado. Por favor consulte con el administrador."
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
