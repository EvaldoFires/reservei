package br.com.reservei.api.application.dto;

import br.com.reservei.api.infrastructure.utils.Cozinha;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record RestauranteDTO(

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        Long id,
        @NotBlank(message = "O nome é obrigatório.")
        String nome,
        @NotBlank(message = "A cozinha é obrigatória.")
        Cozinha cozinha,
        @NotBlank(message = "O id do endereço é obrigatório.")
        Long enderecoId,
        @NotNull(message = "O numero de reservas por hora não pode ser nulo")
        @Min(value = 1, message = "O numero de reservas por hora tem que ser maior do que zero")
        int reservasPorHora,

        @NotNull(message = "O horário de inicio de expediente não pode ser nulo")
        LocalTime inicioExpediente,
        @NotNull(message = "O horário de final de expediente não pode ser nulo")
        LocalTime finalExpediente ){
}
