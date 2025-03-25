package br.com.reservei.api.application.usecases.restaurante;

import br.com.reservei.api.application.dto.RestauranteDTO;
import br.com.reservei.api.domain.exceptions.RecursoNaoEncontradoException;
import br.com.reservei.api.infrastructure.utils.Cozinha;
import br.com.reservei.api.interfaces.mapper.RestauranteMapper;
import br.com.reservei.api.domain.model.Restaurante;
import br.com.reservei.api.domain.repository.RestauranteRepository;
import br.com.reservei.api.application.usecases.endereco.EnderecoService;
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
    public RestauranteDTO buscarPorNome(String nome) {
        Restaurante restaurante = restauranteRepository.findByNome(nome).orElseThrow(()->
                new RecursoNaoEncontradoException("Restaurante não encontrado com nome: " + nome)
        );
        return restauranteMapper.toDto(restaurante);
    }

    @Override
    public List<RestauranteDTO> buscarPorCozinha(Cozinha cozinha) {
        return restauranteRepository.findByCozinha(cozinha)
                .stream()
                .map(restauranteMapper::toDto)
                .toList();
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
