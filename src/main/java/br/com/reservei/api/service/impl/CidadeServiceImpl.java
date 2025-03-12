package br.com.reservei.api.service.impl;

import br.com.reservei.api.dto.CidadeDTO;
import br.com.reservei.api.dto.EstadoDTO;
import br.com.reservei.api.exceptions.RecursoJaSalvoException;
import br.com.reservei.api.exceptions.RecursoNaoEncontradoException;
import br.com.reservei.api.mapper.CidadeMapper;
import br.com.reservei.api.model.Cidade;
import br.com.reservei.api.model.Estado;
import br.com.reservei.api.repository.CidadeRepository;
import br.com.reservei.api.service.CidadeService;
import br.com.reservei.api.service.EstadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CidadeServiceImpl implements CidadeService {

    private final CidadeRepository cidadeRepository;
    private final CidadeMapper cidadeMapper;
    private final EstadoService estadoService;


    @Override
    public CidadeDTO buscarPorId(Long id){
        Cidade cidade = cidadeRepository.findById(id).orElseThrow(()->
                new RecursoNaoEncontradoException("Cidade não encontrado com id: " + id)
        );
        return cidadeMapper.toDto(cidade);
    }

    @Override
    public List<CidadeDTO> buscarTodos() {
        return cidadeRepository.findAll()
                .stream()
                .map(cidadeMapper::toDto)
                .toList();
    }

    @Override
    public CidadeDTO salvar(CidadeDTO cidadeDto) {
        estadoService.buscarPorId(cidadeDto.estadoId());
        cidadeRepository.findByNomeAndEstado_Id(cidadeDto.nome(), cidadeDto.estadoId())
                .ifPresent(cidadePresente -> {
                    throw new RecursoJaSalvoException("Uma cidade com nome '" + cidadePresente.getNome() +
                            "' do estado '" + cidadePresente.getEstado().getNome() + "' já existe no banco de dados.");
                });
        Cidade cidade = cidadeMapper.toEntity(cidadeDto);
        cidade = cidadeRepository.save(cidade);
        return cidadeMapper.toDto(cidade);
    }

    @Override
    public CidadeDTO atualizar(Long id, CidadeDTO cidadeDto) {
        Cidade cidade = cidadeMapper.toEntity(this.buscarPorId(id));
        estadoService.buscarPorId(cidadeDto.estadoId());
        cidadeRepository.findByNomeAndEstado_IdAndNotId(cidadeDto.nome(), cidadeDto.estadoId(), id)
                .ifPresent(cidadePresente -> {
                    throw new RecursoJaSalvoException("Uma cidade com nome '" + cidadePresente.getNome() +
                            "' do estado '" + cidadePresente.getEstado().getNome() + "' já existe no banco de dados.");
                });
        cidadeMapper.updateFromDto(cidadeDto, cidade);
        cidade = cidadeRepository.save(cidade);
        return cidadeMapper.toDto(cidade);
    }

    @Override
    public void deletarPorId(Long id) {
        buscarPorId(id);
        cidadeRepository.deleteById(id);


    }
}
