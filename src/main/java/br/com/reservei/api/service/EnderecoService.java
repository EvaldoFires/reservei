package br.com.reservei.api.service;

import br.com.reservei.api.dto.EnderecoDTO;

import java.util.List;

public interface EnderecoService {

    EnderecoDTO buscarPorId(Long id);
    List<EnderecoDTO> buscarTodos();
    EnderecoDTO salvar(EnderecoDTO enderecoDto);
    EnderecoDTO atualizar(Long id, EnderecoDTO enderecoDto);
    void deletarPorId(Long id);
}
