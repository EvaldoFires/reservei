package br.com.reservei.api.service.impl;

import br.com.reservei.api.dto.RestauranteDTO;
import br.com.reservei.api.exceptions.RecursoJaSalvoException;
import br.com.reservei.api.exceptions.RecursoNaoEncontradoException;
import br.com.reservei.api.mapper.RestauranteMapper;
import br.com.reservei.api.model.Restaurante;
import br.com.reservei.api.repository.RestauranteRepository;
import br.com.reservei.api.service.EnderecoService;
import br.com.reservei.api.service.RestauranteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class RestauranteServiceImpl implements RestauranteService {

    private final RestauranteRepository restauranteRepository;
    private final RestauranteMapper restauranteMapper;
    private final EnderecoService enderecoService;

    @Override
    public RestauranteDTO buscarPorId(Long id){
        Restaurante restaurante = restauranteRepository.findById(id).orElseThrow(()->
                new RecursoNaoEncontradoException("Restaurante não encontrado com id: " + id)
        );
        return restauranteMapper.toDto(restaurante);
    }

    @Override
    public List<RestauranteDTO> buscarTodos() {
        return restauranteRepository.findAll()
                .stream()
                .map(restauranteMapper::toDto)
                .toList();
    }

    @Override
    public RestauranteDTO salvar(RestauranteDTO restauranteDto) {
        enderecoService.buscarPorId(restauranteDto.enderecoId());
        Restaurante restaurante = restauranteMapper.toEntity(restauranteDto);
        restaurante = restauranteRepository.save(restaurante);
        return restauranteMapper.toDto(restaurante);
    }

    @Override
    public RestauranteDTO atualizar(Long id, RestauranteDTO restauranteDto) {
        Restaurante restaurante = restauranteMapper.toEntity(this.buscarPorId(id));
        enderecoService.buscarPorId(restauranteDto.enderecoId());
        restauranteMapper.updateFromDto(restauranteDto, restaurante);
        restaurante = restauranteRepository.save(restaurante);
        return restauranteMapper.toDto(restaurante);
    }

    @Override
    public void deletarPorId(Long id) {
        this.buscarPorId(id);
        restauranteRepository.deleteById(id);

    }
}
