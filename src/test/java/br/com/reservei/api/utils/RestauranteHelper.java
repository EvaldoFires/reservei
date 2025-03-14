package br.com.reservei.api.utils;

import br.com.reservei.api.model.Restaurante;
import br.com.reservei.api.repository.CidadeRepository;
import br.com.reservei.api.repository.EnderecoRepository;
import br.com.reservei.api.repository.EstadoRepository;
import br.com.reservei.api.repository.RestauranteRepository;

import java.time.LocalTime;

import static br.com.reservei.api.utils.EnderecoHelper.gerarEndereco;
import static br.com.reservei.api.utils.EnderecoHelper.salvarEndereco;

public class RestauranteHelper {

    public static Restaurante gerarRestaurante(){
        return Restaurante.builder()
//                .id(Math.abs(UUID.randomUUID().getMostSignificantBits()))
                .nome("Germogli")
                .cozinha(Cozinha.ITALIANA)
                .endereco(gerarEndereco())
                .reservasPorHora(10)
                .inicioExpediente(LocalTime.NOON)
                .finalExpediente(LocalTime.MIDNIGHT)
                .build();
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
