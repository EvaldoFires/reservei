package br.com.reservei.api.infrastructure.utils;

import br.com.reservei.api.application.dto.AvaliacaoDTO;
import br.com.reservei.api.domain.model.Avaliacao;

import java.util.UUID;

import static br.com.reservei.api.infrastructure.utils.RestauranteHelper.gerarRestaurante;

public class AvaliacaoHelper {

    public static Avaliacao gerarAvaliacao(){
        return Avaliacao.builder()
                .id(Math.abs(UUID.randomUUID().getMostSignificantBits()))
                .nota(5)
                .comentario("Bom demais")
                .restaurante(gerarRestaurante())
                .build();
    }

    public static AvaliacaoDTO gerarAvaliacaoDto(Avaliacao avaliacao){
        return new AvaliacaoDTO(avaliacao.getId(),
                avaliacao.getNota(),
                avaliacao.getComentario(),
                avaliacao.getDataCriacao(),
                avaliacao.getRestaurante().getId());

    }

    public static AvaliacaoDTO gerarAvaliacaoDtoSemId(Long restauranteId){
        return new AvaliacaoDTO(null,
                5,
                "Tudo ótimo",
                null,
                restauranteId);

    }

//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public static Avaliacao salvarAvaliacao(AvaliacaoRepository avaliacaoRepository,
//                                        RestauranteRepository restauranteRepository){
//        salvarRestaurante(restauranteRepository);
//        var avaliacao = gerarAvaliacao();
//        return avaliacaoRepository.save(avaliacao);
//    }
}
