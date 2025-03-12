package br.com.reservei.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record EstadoDTO(

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        Long id,
        @NotBlank(message = "O nome é obrigatório")
        String nome,
        @NotBlank(message = "A sigla é obrigatória")
        String sigla
) {
}
