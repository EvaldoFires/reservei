package br.com.reservei.api.service.impl;

import br.com.reservei.api.dto.AvaliacaoDTO;
import br.com.reservei.api.exceptions.RecursoNaoEncontradoException;
import br.com.reservei.api.mapper.AvaliacaoMapper;
import br.com.reservei.api.model.Avaliacao;
import br.com.reservei.api.repository.AvaliacaoRepository;
import br.com.reservei.api.service.AvaliacaoService;
import br.com.reservei.api.service.RestauranteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AvaliacaoServiceImpl implements AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final AvaliacaoMapper avaliacaoMapper;
    private final RestauranteService restauranteService;

    @Override
    public AvaliacaoDTO buscarPorId(Long id){
        Avaliacao avaliacao = avaliacaoRepository.findById(id).orElseThrow(()->
                new RecursoNaoEncontradoException("Avaliação não encontrada com id: " + id)
        );
        return avaliacaoMapper.toDto(avaliacao);
    }

    @Override
    public List<AvaliacaoDTO> buscarTodos() {
        return avaliacaoRepository.findAll()
                .stream()
                .map(avaliacaoMapper::toDto)
                .toList();
    }

    @Override
    public AvaliacaoDTO salvar(AvaliacaoDTO avaliacaoDto) {
        restauranteService.buscarPorId(avaliacaoDto.restauranteId());
        Avaliacao avaliacao = avaliacaoMapper.toEntity(avaliacaoDto);
        avaliacao = avaliacaoRepository.save(avaliacao);
        return avaliacaoMapper.toDto(avaliacao);
    }

    @Override
    public AvaliacaoDTO atualizar(Long id, AvaliacaoDTO avaliacaoDto) {
        Avaliacao avaliacao = avaliacaoMapper.toEntity(this.buscarPorId(id));
        restauranteService.buscarPorId(avaliacaoDto.restauranteId());
        avaliacaoMapper.updateFromDto(avaliacaoDto, avaliacao);
        avaliacao = avaliacaoRepository.save(avaliacao);
        return avaliacaoMapper.toDto(avaliacao);
    }

    @Override
    public void deletarPorId(Long id) {
        this.buscarPorId(id);
        avaliacaoRepository.deleteById(id);
    }
}
