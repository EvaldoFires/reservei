package br.com.reservei.api.application.usecases.endereco;

import br.com.reservei.api.application.dto.EstadoDTO;

import java.util.List;

public interface EstadoService {

    EstadoDTO buscarPorId(Long id);
    List<EstadoDTO> buscarTodos();
    EstadoDTO salvar(EstadoDTO estadoDto);
    EstadoDTO atualizar(Long id, EstadoDTO estadoDto);
    void deletarPorId(Long id);
}
