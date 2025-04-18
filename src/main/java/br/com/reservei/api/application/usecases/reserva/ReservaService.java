package br.com.reservei.api.application.usecases.reserva;

import br.com.reservei.api.application.dto.ReservaDTO;

import java.util.List;

public interface ReservaService {

    ReservaDTO buscarPorId(Long id);
    List<ReservaDTO> buscarTodos();
    ReservaDTO salvar(ReservaDTO reservaDto);
    ReservaDTO atualizar(Long id, ReservaDTO reservaDto);
    void deletarPorId(Long id);
}
