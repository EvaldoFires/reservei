package br.com.reservei.api.application.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record CidadeDTO(

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        Long id,

        @NotBlank(message = "O Nome é obrigatório")
        String nome,
        @NotBlank(message = "O id do estado é obrigatorio")
        Long estadoId
) {
}
