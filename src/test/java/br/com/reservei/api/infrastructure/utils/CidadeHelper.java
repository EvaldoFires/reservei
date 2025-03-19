package br.com.reservei.api.infrastructure.utils;

import br.com.reservei.api.application.dto.CidadeDTO;
import br.com.reservei.api.domain.model.Cidade;
import br.com.reservei.api.domain.repository.CidadeRepository;
import br.com.reservei.api.domain.repository.EstadoRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static br.com.reservei.api.infrastructure.utils.EstadoHelper.gerarEstado;
import static br.com.reservei.api.infrastructure.utils.EstadoHelper.salvarEstado;

public class CidadeHelper {
    public static Cidade gerarCidade(){
        return Cidade.builder()
                .id(Math.abs(UUID.randomUUID().getMostSignificantBits()))
                .nome("Salvador")
                .estado(gerarEstado())
                .build();
    }

    public static CidadeDTO gerarCidadeDtoSemId(Long estadoId){
        return new CidadeDTO(null,
                "Salvador",
                estadoId);
    }

    public static CidadeDTO gerarCidadeDto(Cidade cidade){
        return new CidadeDTO(cidade.getId(), cidade.getNome(), cidade.getEstado().getId());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public static Cidade salvarCidade(CidadeRepository cidadeRepository,
                                      EstadoRepository estadoRepository){
        salvarEstado(estadoRepository);
        var cidade = gerarCidade();
        return cidadeRepository.save(cidade);
    }
}
