package br.com.reservei.api.bdd.contexto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@Scope("cucumber-glue")
public class ContextoIds {

    private Long estadoId;
    private Long cidadeId;
    private Long enderecoId;
    private Long restauranteId;
    private Long avaliacaoId;
    private Long reservaId;
}
