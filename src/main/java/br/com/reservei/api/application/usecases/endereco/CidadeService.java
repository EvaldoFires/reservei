package br.com.reservei.api.application.usecases.endereco;

import br.com.reservei.api.application.dto.CidadeDTO;

import java.util.List;

public interface CidadeService {

    CidadeDTO buscarPorId(Long id);
    List<CidadeDTO> buscarTodos();
    CidadeDTO salvar(CidadeDTO cidadeDto);
    CidadeDTO atualizar(Long id, CidadeDTO cidadeDto);
    void deletarPorId(Long id);
}
