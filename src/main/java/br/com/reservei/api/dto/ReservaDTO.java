package br.com.reservei.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ReservaDTO(
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        Long id,
        @NotBlank(message = "O id do restaurante é obrigatório.")
        long restauranteId,
        @NotNull(message = "O horario de reserva não pode ser nulo")
        LocalDateTime horaDaReserva
) {
}
