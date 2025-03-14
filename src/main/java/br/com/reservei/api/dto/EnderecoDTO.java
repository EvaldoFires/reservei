package br.com.reservei.api.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record EnderecoDTO(

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        Long id,

        @NotBlank(message = "O id da cidade é obrigatório")
        Long cidadeId,
        @NotBlank(message = "O bairro é obrigatório")
        String bairro,
        @NotBlank(message = "A rua é obrigatório")
        String rua,
        String numero
) {
}
