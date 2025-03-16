package br.com.reservei.api.application.usecases.restaurante;

import br.com.reservei.api.application.dto.RestauranteDTO;

import java.util.List;

public interface RestauranteService {

    RestauranteDTO buscarPorId(Long id);
    List<RestauranteDTO> buscarTodos();
    RestauranteDTO salvar(RestauranteDTO restauranteDto);
    RestauranteDTO atualizar(Long id, RestauranteDTO restauranteDto);
    void deletarPorId(Long id);
}
