package br.com.reservei.api.service;

import br.com.reservei.api.dto.CidadeDTO;

import java.util.List;

public interface CidadeService {

    public CidadeDTO buscarPorId(Long id);
    public List<CidadeDTO> buscarTodos();
    public CidadeDTO salvar(CidadeDTO cidadeDto);
    public CidadeDTO atualizar(Long id, CidadeDTO cidadeDto);
    public void deletarPorId(Long id);
}
