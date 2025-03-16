package br.com.reservei.api.application.usecases.endereco;

import br.com.reservei.api.application.dto.EstadoDTO;
import br.com.reservei.api.domain.exceptions.RecursoJaSalvoException;
import br.com.reservei.api.domain.exceptions.RecursoNaoEncontradoException;
import br.com.reservei.api.interfaces.mapper.EstadoMapper;
import br.com.reservei.api.domain.model.Estado;
import br.com.reservei.api.domain.repository.EstadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class EstadoServiceImpl implements EstadoService {

    private final EstadoRepository estadoRepository;
    private final EstadoMapper estadoMapper;

    @Override
    public EstadoDTO buscarPorId(Long id){
        Estado estado = estadoRepository.findById(id).orElseThrow(()->
                new RecursoNaoEncontradoException("Estado não encontrado com id: " + id)
        );
        return estadoMapper.toDto(estado);
    }

    @Override
    public List<EstadoDTO> buscarTodos() {
        return estadoRepository.findAll()
                .stream()
                .map(estadoMapper::toDto)
                .toList();
    }

    @Override
    public EstadoDTO salvar(EstadoDTO estadoDto) {
        estadoRepository.findByNomeOrSigla(estadoDto.nome(), estadoDto.sigla())
                .ifPresent(estadoPresente -> {
                    throw new RecursoJaSalvoException("Um estado com sigla '" + estadoPresente.getSigla() +
                            "' ou nome '" + estadoPresente.getNome() + "' já existe no banco de dados.");
                });
        Estado estado = estadoMapper.toEntity(estadoDto);
        estado = estadoRepository.save(estado);
        return estadoMapper.toDto(estado);
    }

    @Override
    public EstadoDTO atualizar(Long id, EstadoDTO estadoDto) {
        Estado estado = estadoMapper.toEntity(this.buscarPorId(id));
        estadoRepository.findByNomeOrSiglaAndIdNot(estadoDto.nome(), estadoDto.sigla(), id)
                .ifPresent(estadoPresente -> {
                    throw new RecursoJaSalvoException("Um estado com sigla '" + estadoPresente.getSigla() +
                            "' ou nome '" + estadoPresente.getNome() + "' já existe no banco de dados.");
                });
        estadoMapper.updateFromDto(estadoDto, estado);
        estado = estadoRepository.save(estado);
        return estadoMapper.toDto(estado);
    }

    @Override
    public void deletarPorId(Long id) {
        this.buscarPorId(id);
        estadoRepository.deleteById(id);
    }
}
