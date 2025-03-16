package br.com.reservei.api.application.usecases.endereco;

import br.com.reservei.api.application.dto.EnderecoDTO;
import br.com.reservei.api.domain.exceptions.RecursoNaoEncontradoException;
import br.com.reservei.api.interfaces.mapper.EnderecoMapper;
import br.com.reservei.api.domain.model.Endereco;
import br.com.reservei.api.domain.repository.EnderecoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class EnderecoServiceImpl implements EnderecoService {

    private final EnderecoRepository enderecoRepository;
    private final EnderecoMapper enderecoMapper;
    private final CidadeService cidadeService;


    @Override
    public EnderecoDTO buscarPorId(Long id){
        Endereco endereco = enderecoRepository.findById(id).orElseThrow(()->
                new RecursoNaoEncontradoException("Endereço não encontrado com id: " + id)
        );
        return enderecoMapper.toDto(endereco);
    }

    @Override
    public List<EnderecoDTO> buscarTodos() {
        return enderecoRepository.findAll()
                .stream()
                .map(enderecoMapper::toDto)
                .toList();
    }

    @Override
    public EnderecoDTO salvar(EnderecoDTO enderecoDto) {
        cidadeService.buscarPorId(enderecoDto.cidadeId());
        Endereco endereco = enderecoMapper.toEntity(enderecoDto);
        endereco = enderecoRepository.save(endereco);
        return enderecoMapper.toDto(endereco);
    }

    @Override
    public EnderecoDTO atualizar(Long id, EnderecoDTO enderecoDto) {
        Endereco endereco = enderecoMapper.toEntity(this.buscarPorId(id));
        cidadeService.buscarPorId(enderecoDto.cidadeId());
        enderecoMapper.updateFromDto(enderecoDto, endereco);
        endereco = enderecoRepository.save(endereco);
        return enderecoMapper.toDto(endereco);
    }

    @Override
    public void deletarPorId(Long id) {
        this.buscarPorId(id);
        enderecoRepository.deleteById(id);
    }
}
