package br.com.reservei.api.application.usecases.avaliacao;

import br.com.reservei.api.application.dto.AvaliacaoDTO;
import br.com.reservei.api.domain.exceptions.RecursoNaoEncontradoException;
import br.com.reservei.api.interfaces.mapper.AvaliacaoMapper;
import br.com.reservei.api.domain.model.Avaliacao;
import br.com.reservei.api.domain.repository.AvaliacaoRepository;
import br.com.reservei.api.application.usecases.restaurante.RestauranteService;
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
