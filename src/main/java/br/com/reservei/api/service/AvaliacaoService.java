package br.com.reservei.api.service;

import br.com.reservei.api.dto.AvaliacaoDTO;

import java.util.List;

public interface AvaliacaoService {

    AvaliacaoDTO buscarPorId(Long id);
    List<AvaliacaoDTO> buscarTodos();
    AvaliacaoDTO salvar(AvaliacaoDTO avaliacaoDto);
    AvaliacaoDTO atualizar(Long id, AvaliacaoDTO avaliacaoDto);
    void deletarPorId(Long id);
}
