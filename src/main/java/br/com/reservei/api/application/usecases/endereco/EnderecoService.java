package br.com.reservei.api.application.usecases.endereco;

import br.com.reservei.api.application.dto.EnderecoDTO;

import java.util.List;

public interface EnderecoService {

    EnderecoDTO buscarPorId(Long id);
    List<EnderecoDTO> buscarTodos();
    EnderecoDTO salvar(EnderecoDTO enderecoDto);
    EnderecoDTO atualizar(Long id, EnderecoDTO enderecoDto);
    void deletarPorId(Long id);
}
