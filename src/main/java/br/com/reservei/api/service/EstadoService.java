package br.com.reservei.api.service;

import br.com.reservei.api.dto.EstadoDTO;

import java.util.List;

public interface EstadoService {

    public EstadoDTO buscarPorId(Long id);
    public List<EstadoDTO> buscarTodos();
    public EstadoDTO salvar(EstadoDTO estadoDto);
    public EstadoDTO atualizar(Long id, EstadoDTO estadoDto);
    public void deletarPorId(Long id);
}
