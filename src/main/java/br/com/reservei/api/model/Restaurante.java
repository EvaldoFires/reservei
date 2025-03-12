package br.com.reservei.api.model;

import br.com.reservei.api.utils.Cozinha;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Restaurante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private Cozinha cozinha;
    @OneToOne
    private Endereco endereco;
    private int numeroReservasPorHora;
    private LocalTime inicioExpediente;
    private LocalTime finalExpediente;
}
