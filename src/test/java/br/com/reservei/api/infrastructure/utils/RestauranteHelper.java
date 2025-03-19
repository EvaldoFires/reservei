package br.com.reservei.api.infrastructure.utils;

import br.com.reservei.api.application.dto.RestauranteDTO;
import br.com.reservei.api.domain.model.Restaurante;
import br.com.reservei.api.domain.repository.CidadeRepository;
import br.com.reservei.api.domain.repository.EnderecoRepository;
import br.com.reservei.api.domain.repository.EstadoRepository;
import br.com.reservei.api.domain.repository.RestauranteRepository;

import java.time.LocalTime;
import java.util.UUID;

import static br.com.reservei.api.infrastructure.utils.EnderecoHelper.gerarEndereco;
import static br.com.reservei.api.infrastructure.utils.EnderecoHelper.salvarEndereco;

public class RestauranteHelper {

    public static Restaurante gerarRestaurante(){
        return Restaurante.builder()
                .id(Math.abs(UUID.randomUUID().getMostSignificantBits()))
                .nome("Germogli")
                .cozinha(Cozinha.ITALIANA)
                .endereco(gerarEndereco())
                .reservasPorHora(10)
                .inicioExpediente(LocalTime.NOON)
                .finalExpediente(LocalTime.MIDNIGHT)
                .build();
    }

    public static RestauranteDTO gerarRestauranteDto(Restaurante restaurante){
        return new RestauranteDTO(restaurante.getId(),
                restaurante.getNome(),
                restaurante.getCozinha(),
                restaurante.getEndereco().getId(),
                restaurante.getReservasPorHora(),
                restaurante.getInicioExpediente(),
                restaurante.getFinalExpediente());
    }

    public static RestauranteDTO gerarRestauranteDtoSemId(Long enderecoId){
        return new RestauranteDTO(null,
                "Germogli",
                Cozinha.ITALIANA,
                enderecoId,
                10,
                LocalTime.NOON,
                LocalTime.MIDNIGHT);
    }

//    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public static Restaurante salvarRestaurante(RestauranteRepository restauranteRepository,
                                                EnderecoRepository enderecoRepository,
                                                CidadeRepository cidadeRepository,
                                                EstadoRepository estadoRepository){
        salvarEndereco(enderecoRepository, cidadeRepository, estadoRepository);
        var restaurante = gerarRestaurante();
        return restauranteRepository.save(restaurante);
    }
}
