package br.com.reservei.api.service;

import br.com.reservei.api.dto.CidadeDTO;
import br.com.reservei.api.dto.EnderecoDTO;
import br.com.reservei.api.exceptions.RecursoNaoEncontradoException;
import br.com.reservei.api.mapper.EnderecoMapper;
import br.com.reservei.api.model.Endereco;
import br.com.reservei.api.repository.EnderecoRepository;
import br.com.reservei.api.service.impl.EnderecoServiceImpl;
import br.com.reservei.api.utils.EnderecoHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static br.com.reservei.api.utils.CidadeHelper.gerarCidade;
import static br.com.reservei.api.utils.CidadeHelper.gerarCidadeDto;
import static br.com.reservei.api.utils.EnderecoHelper.gerarEndereco;
import static br.com.reservei.api.utils.EnderecoHelper.gerarEnderecoDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnderecoServiceTest {

    @Mock
    private EnderecoRepository enderecoRepository;

    @Mock
    private EnderecoMapper enderecoMapper;

    @Mock
    private CidadeService cidadeService;

    @InjectMocks
    private EnderecoServiceImpl enderecoService;

    private Endereco endereco;
    private EnderecoDTO enderecoDTO;
    private CidadeDTO cidadeDTO;

    @BeforeEach
    void setUp(){
        this.endereco = gerarEndereco();
        this.enderecoDTO = gerarEnderecoDto(endereco);
        this.cidadeDTO = gerarCidadeDto(gerarCidade());
    }

    @DisplayName("Buscar Endereço")
    @Nested
    class BuscarEndereco {

        @DisplayName("Deve buscar um Endereço pelo ID fornecido")
        @Test
        void deveBuscarEnderecoPorId() {
            // Arrange
            when(enderecoRepository.findById(endereco.getId())).thenReturn(Optional.of(endereco));
            when(enderecoMapper.toDto(endereco)).thenReturn(gerarEnderecoDto(endereco));

            // Act
            var enderecoRecebido = enderecoService.buscarPorId(endereco.getId());

            // Assert
            assertThat(enderecoRecebido)
                    .usingRecursiveComparison()
                    .ignoringFields("cidadeId")
                    .isEqualTo(endereco);

            assertThat(enderecoRecebido.cidadeId()).isEqualTo(endereco.getCidade().getId());

            verify(enderecoMapper).toDto(endereco);
            verify(enderecoRepository).findById(endereco.getId());
        }

        @DisplayName("Deve lançar exceção ao buscar Endereço com ID inexistente")
        @Test
        void deveGerarExcecao_QuandoBuscarEndereco_PorIdInexistente() {
            // Arrange
            when(enderecoRepository.findById(endereco.getId())).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> enderecoService.buscarPorId(endereco.getId()))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Endereço não encontrado com id: " + endereco.getId());
            verify(enderecoRepository).findById(endereco.getId());
        }

        @DisplayName("Deve retornar uma lista de endereços salvos")
        @Test
        void deveBuscarTodosOsEnderecos() {
            // Arrange
            var enderecos = List.of(gerarEndereco(), gerarEndereco(), gerarEndereco());
            var enderecosDto = enderecos.stream()
                    .map(EnderecoHelper::gerarEnderecoDto)
                    .toList();

            when(enderecoRepository.findAll()).thenReturn(enderecos);
            when(enderecoMapper.toDto(any(Endereco.class)))
                    .thenAnswer(invocation -> {
                        endereco = invocation.getArgument(0);
                        return gerarEnderecoDto(endereco);
                    });

            // Act
            List<EnderecoDTO> enderecosRecebidos = enderecoService.buscarTodos();

            // Assert
            assertThat(enderecosRecebidos)
                    .isNotNull()
                    .isNotEmpty()
                    .hasSize(3)
                    .containsExactlyElementsOf(enderecosDto);
            verify(enderecoRepository).findAll();
            verify(enderecoMapper, times(3)).toDto(any(Endereco.class));
        }
    }

    @DisplayName("Cadastrar Endereço")
    @Nested
    class CadastrarEndereco {

        @DisplayName("Deve cadastrar Endereço")
        @Test
        void deveCadastrarEndereco() {
            // Arrange
            when(enderecoMapper.toEntity(enderecoDTO)).thenReturn(endereco);
            when(enderecoMapper.toDto(endereco)).thenReturn(enderecoDTO);
            when(enderecoRepository.save(endereco)).thenReturn(endereco);
            when(cidadeService.buscarPorId(enderecoDTO.cidadeId())).thenReturn(cidadeDTO);

            // Act
            var enderecoSalvo = enderecoService.salvar(enderecoDTO);

            // Assert
            assertThat(enderecoSalvo)
                    .isNotNull()
                    .isInstanceOf(EnderecoDTO.class)
                    .isEqualTo(enderecoDTO);

            verify(cidadeService).buscarPorId(enderecoDTO.cidadeId());
            verify(enderecoRepository).save(endereco);
            verify(enderecoMapper).toDto(endereco);
            verify(enderecoMapper).toEntity(enderecoDTO);
        }

        @DisplayName("Deve lançar exceção ao tentar salvar endereço com cidade inexistente")
        @Test
        void deveGerarExcecao_QuandoCadastrarEndereco_ComCidadeInexistente() {
            // Arrange
            when(cidadeService.buscarPorId(enderecoDTO.cidadeId()))
                    .thenThrow(new RecursoNaoEncontradoException("Cidade não encontrada com id: " +
                            enderecoDTO.cidadeId()));

            // Act & Assert
            assertThatThrownBy(() -> enderecoService.salvar(enderecoDTO))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Cidade não encontrada com id: " + enderecoDTO.cidadeId());

            verify(cidadeService).buscarPorId(enderecoDTO.cidadeId());
            verifyNoInteractions(enderecoRepository);
        }
    }

    @DisplayName("Alterar Endereço")
    @Nested
    class AlterarEndereco{

        @DisplayName("Deve alterar Endereço cadastrada")
        @Test
        void deveAlterarEnderecoPorId() {
            // Arrange
            when(enderecoMapper.toEntity(enderecoDTO)).thenReturn(endereco);
            when(enderecoMapper.toDto(endereco)).thenReturn(enderecoDTO);
            doNothing().when(enderecoMapper).updateFromDto(enderecoDTO, endereco);
            when(cidadeService.buscarPorId(enderecoDTO.cidadeId())).thenReturn(cidadeDTO);
            when(enderecoRepository.save(endereco)).thenReturn(endereco);
            when(enderecoRepository.findById(endereco.getId())).thenReturn(Optional.of(endereco));

            // Act
            var enderecoSalvo = enderecoService.atualizar(enderecoDTO.id(), enderecoDTO);

            // Assert
            assertThat(enderecoSalvo)
                    .isNotNull()
                    .isInstanceOf(EnderecoDTO.class)
                    .isEqualTo(enderecoDTO);
            verify(enderecoRepository).findById(enderecoDTO.id());
            verify(enderecoRepository).save(endereco);
            verify(cidadeService).buscarPorId(enderecoDTO.cidadeId());
            verify(enderecoMapper).updateFromDto(enderecoDTO, endereco);
            verify(enderecoMapper).toEntity(enderecoDTO);
            verify(enderecoMapper, times(2)).toDto(endereco);
        }

        @DisplayName("Deve lançar exceção ao tentar alterar Endereço com id inexistente")
        @Test
        void deveGerarExcecao_QuandoTentarAlterarEndereco_PorIdInexistente() {
            // Arrange
            when(enderecoRepository.findById(enderecoDTO.id())).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> enderecoService.atualizar(enderecoDTO.id(), enderecoDTO))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Endereço não encontrado com id: " + endereco.getId());

            verify(enderecoRepository).findById(enderecoDTO.id());
            verifyNoMoreInteractions(enderecoRepository);
        }

        @DisplayName("Deve lançar exceção ao tentar alterar Endereço com cidade inexistente")
        @Test
        void deveGerarExcecao_QuandoAlterarEndereco_ComCidadeInexistente() {
            // Arrange
            when(enderecoRepository.findById(enderecoDTO.id())).thenReturn(Optional.of(endereco));
            when(enderecoMapper.toDto(endereco)).thenReturn(enderecoDTO);
            when(enderecoMapper.toEntity(enderecoDTO)).thenReturn(endereco);
            when(cidadeService.buscarPorId(enderecoDTO.cidadeId()))
                    .thenThrow(new RecursoNaoEncontradoException("Cidade não encontrada com id: " +
                            enderecoDTO.cidadeId()));
            // Act & Assert
            assertThatThrownBy(() -> enderecoService.atualizar(enderecoDTO.id(), enderecoDTO))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Cidade não encontrada com id: " + enderecoDTO.cidadeId());

            verify(cidadeService).buscarPorId(enderecoDTO.cidadeId());
            verify(enderecoRepository).findById(enderecoDTO.id());
            verifyNoMoreInteractions(enderecoRepository);
        }

    }

    @DisplayName("Deletar Endereço")
    @Nested
    class DeletarEndereco{

        @DisplayName("Deve deletar Endereço")
        @Test
        void deveDeletarEnderecoPorId(){
            // Arrange
            when(enderecoRepository.findById(endereco.getId()))
                    .thenReturn(Optional.of(endereco));
            doNothing().when(enderecoRepository).deleteById(endereco.getId());

            // Act
            enderecoService.deletarPorId(endereco.getId());

            // Assert
            verify(enderecoRepository).findById(endereco.getId());
            verify(enderecoRepository).deleteById(endereco.getId());
        }

        @DisplayName("Deve lançar exceção ao tentar deletar Endereço por id inexistente")
        @Test
        void deveGerarExcecao_QuandoTentarDeletarEndereco_PorIdInexistente(){
            // Arrange
            when(enderecoRepository.findById(endereco.getId())).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> enderecoService.deletarPorId(endereco.getId()))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Endereço não encontrado com id: " + endereco.getId());

            verify(enderecoRepository).findById(endereco.getId());
        }
    }
}
