package br.com.reservei.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @ManyToOne
    private Restaurante restaurante;

    @PrePersist
    protected void prePersist() {
        this.dataCriacao = LocalDateTime.now();
    }
}
