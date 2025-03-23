package br.com.reservei.api.application.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record EnderecoDTO(

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        @Schema(example = "1")
        Long id,

        @NotBlank(message = "O id da cidade é obrigatório")
        @Schema(example = "1")
        Long cidadeId,
        @NotBlank(message = "O bairro é obrigatório")
        @Schema(example = "Centro")
        String bairro,
        @NotBlank(message = "A rua é obrigatório")
        @Schema(example = "Rua das Flores")
        String rua,
        @Schema(example = "123")
        String numero
) {
}
