package br.com.reservei.api.utils;

import br.com.reservei.api.dto.CidadeDTO;
import br.com.reservei.api.model.Cidade;
import br.com.reservei.api.repository.CidadeRepository;
import br.com.reservei.api.repository.EstadoRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static br.com.reservei.api.utils.EstadoHelper.gerarEstado;
import static br.com.reservei.api.utils.EstadoHelper.salvarEstado;

public class CidadeHelper {
    public static Cidade gerarCidade(){
        return Cidade.builder()
                .id(Math.abs(UUID.randomUUID().getMostSignificantBits()))
                .nome("Salvador")
                .estado(gerarEstado())
                .build();
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
