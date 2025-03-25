package br.com.reservei.api.application.usecases.restaurante;

import br.com.reservei.api.application.dto.RestauranteDTO;
import br.com.reservei.api.infrastructure.utils.Cozinha;

import java.util.List;

public interface RestauranteService {

    RestauranteDTO buscarPorId(Long id);
    List<RestauranteDTO> buscarTodos();
    RestauranteDTO buscarPorNome(String nome);
    List<RestauranteDTO> buscarPorCozinha(Cozinha cozinha);
    RestauranteDTO salvar(RestauranteDTO restauranteDto);
    RestauranteDTO atualizar(Long id, RestauranteDTO restauranteDto);
    void deletarPorId(Long id);
}
