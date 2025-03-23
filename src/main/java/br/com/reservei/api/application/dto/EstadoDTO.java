package br.com.reservei.api.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record EstadoDTO(

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        @Schema(example = "1")
        Long id,
        @NotBlank(message = "O nome é obrigatório")
        @Schema(example = "Minas Gerais")
        String nome,
        @NotBlank(message = "A sigla é obrigatória")
        @Schema(example = "MG")
        String sigla
) {
}
