package br.com.reservei.api.infrastructure.utils;

import br.com.reservei.api.application.dto.EstadoDTO;
import br.com.reservei.api.domain.model.Cidade;
import br.com.reservei.api.domain.model.Estado;
import br.com.reservei.api.domain.repository.EstadoRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.UUID;

public class EstadoHelper {

    public static Estado gerarEstado(){
        return Estado.builder()
                .id(Math.abs(UUID.randomUUID().getMostSignificantBits()))
                .nome("Bahia")
                .sigla("BA")
                .cidades(new ArrayList<Cidade>())
                .build();
    }

    public static Estado gerarEstadoSemId(){
        return Estado.builder()
                .nome("Bahia")
                .sigla("BA")
                .cidades(new ArrayList<Cidade>())
                .build();
    }

    public static EstadoDTO gerarEstadoDto(Estado estado){
        return new EstadoDTO(estado.getId(), estado.getNome(), estado.getSigla());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public static Estado salvarEstado(EstadoRepository estadoRepository){
        var estado = gerarEstado();
        return estadoRepository.save(estado);
    }
}
