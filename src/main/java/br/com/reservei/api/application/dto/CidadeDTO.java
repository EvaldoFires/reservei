package br.com.reservei.api.application.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CidadeDTO(

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        @Schema(example = "1")
        Long id,
        @NotBlank(message = "O Nome é obrigatório")
        @Schema(example = "Belo Horizonte")
        String nome,
        @NotBlank(message = "O id do estado é obrigatorio")
        @Schema(example = "1")
        Long estadoId
) {
}
