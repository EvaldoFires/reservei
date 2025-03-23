package br.com.reservei.api.application.dto;

import br.com.reservei.api.infrastructure.utils.Cozinha;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record RestauranteDTO(

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        @Schema(example = "1")
        Long id,
        @NotBlank(message = "O nome é obrigatório.")
        @Schema(example = "KiKumyda Boua")
        String nome,
        @NotBlank(message = "A cozinha é obrigatória.")
        @Schema(example = "JAPONESA", allowableValues = {"JAPONESA", "ITALIANA", "CHINESA", "ARABE", "FRANCESA", "NORDESTINA", "MINEIRA", "MEXICANA", "TAILANDESA"})
        Cozinha cozinha,
        @NotBlank(message = "O id do endereço é obrigatório.")
        @Schema(example = "1")
        Long enderecoId,
        @NotNull(message = "O numero de reservas por hora não pode ser nulo")
        @Min(value = 1, message = "O numero de reservas por hora tem que ser maior do que zero")
        @Schema(example = "10")
        int reservasPorHora,

        @NotNull(message = "O horário de inicio de expediente não pode ser nulo")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
        @Schema(example = "08:00:00")
        LocalTime inicioExpediente,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
        @NotNull(message = "O horário de final de expediente não pode ser nulo")
        @Schema(example = "20:00:00")
        LocalTime finalExpediente ){
}
