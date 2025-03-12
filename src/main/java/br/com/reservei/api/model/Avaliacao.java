package br.com.reservei.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int nota;
    private String comentario;
    private LocalDateTime dataCriacao;

    @ManyToOne
    private Restaurante restaurante;

    public Avaliacao(int nota, String comentario, Restaurante restaurante) {
        this.nota = nota;
        this.comentario = comentario;
        this.restaurante = restaurante;
        this.dataCriacao = LocalDateTime.now();
    }
}
