package br.com.reservei.api.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record AvaliacaoDTO(

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        @Schema(example = "1")
        Long id,

        @NotBlank(message = "A nota é obrigatório.")
        @Min(value = 1, message = "A nota tem que ser maior do que 1")
        @Max(value = 5, message = "A nota não pode ser maior do que 5")
        @Schema(example = "5")
        int nota,

        @Schema(example = "Muito bom!")
        String comentario,

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        @Schema(example = "2021-10-10 20:00:00")
        LocalDateTime dataCriacao,

        @NotBlank(message = "O id do restaurante é obrigatório.")
        @Schema(example = "1")
        Long restauranteId
) {
}
