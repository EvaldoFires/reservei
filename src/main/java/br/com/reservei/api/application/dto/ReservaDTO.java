package br.com.reservei.api.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ReservaDTO(
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        @Schema(example = "1")
        Long id,
        @NotBlank(message = "O id do restaurante é obrigatório.")
        @Schema(example = "1")
        Long restauranteId,
        @NotNull(message = "O horário de reserva não pode ser nulo")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(example = "2021-10-10 20:00:00")
        LocalDateTime horaDaReserva
) {
}
