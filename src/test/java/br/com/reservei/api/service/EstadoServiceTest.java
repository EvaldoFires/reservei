package br.com.reservei.api.service;

import br.com.reservei.api.dto.EstadoDTO;
import br.com.reservei.api.exceptions.RecursoJaSalvoException;
import br.com.reservei.api.exceptions.RecursoNaoEncontradoException;
import br.com.reservei.api.mapper.EstadoMapper;
import br.com.reservei.api.model.Estado;
import br.com.reservei.api.repository.EstadoRepository;
import br.com.reservei.api.service.impl.EstadoServiceImpl;
import br.com.reservei.api.utils.EstadoHelper;
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

import static br.com.reservei.api.utils.EstadoHelper.gerarEstado;
import static br.com.reservei.api.utils.EstadoHelper.gerarEstadoDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EstadoServiceTest {

    @Mock
    private EstadoRepository estadoRepository;

    @Mock
    private EstadoMapper estadoMapper;

    @InjectMocks
    private EstadoServiceImpl estadoService;

    private Estado estado;
    private EstadoDTO estadoDTO;

    @BeforeEach
    void setUp(){
        this.estado = gerarEstado();
        this.estadoDTO = gerarEstadoDto(estado);
    }

    @DisplayName("Buscar Estado")
    @Nested
    class BuscarEstado {

        @DisplayName("Deve buscar um Estado pelo ID fornecido")
        @Test
        void deveBuscarEstadoPorId() {
            // Arrange
            when(estadoRepository.findById(estado.getId())).thenReturn(Optional.of(estado));
            when(estadoMapper.toDto(estado)).thenReturn(gerarEstadoDto(estado));

            // Act
            var estadoRecebido = estadoService.buscarPorId(estado.getId());

            // Assert
            verify(estadoRepository).findById(estado.getId());
            assertThat(estadoRecebido)
                    .usingRecursiveComparison()
                    .ignoringFields("cidades")
                    .isEqualTo(estado);
        }

        @DisplayName("Deve lançar exceção ao buscar estado com ID inexistente")
        @Test
        void deveGerarExcecao_QuandoBuscarEstado_PorIdInexistente() {
            // Arrange
            when(estadoRepository.findById(estado.getId())).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> estadoService.buscarPorId(estado.getId()))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Estado não encontrado com id: " + estado.getId());
            verify(estadoRepository).findById(estado.getId());
        }

        @DisplayName("Deve retornar uma lista de estados salvos")
        @Test
        void deveBuscarTodosOsEstado() {
            // Arrange
            var estados = List.of(gerarEstado(), gerarEstado(), gerarEstado());
            var estadosDto = estados.stream()
                    .map(EstadoHelper::gerarEstadoDto)
                    .toList();

            when(estadoRepository.findAll())
                    .thenReturn(estados);
            when(estadoMapper.toDto(any(Estado.class)))
                    .thenAnswer(invocation -> {
                        estado = invocation.getArgument(0);
                        return gerarEstadoDto(estado);
                    });

            // Act
            List<EstadoDTO> estadosRecebidos = estadoService.buscarTodos();

            // Assert
            assertThat(estadosRecebidos)
                    .isNotNull()
                    .isNotEmpty()
                    .hasSize(3)
                    .containsExactlyElementsOf(estadosDto);
            verify(estadoRepository).findAll();
            verify(estadoMapper, times(3)).toDto(any(Estado.class));
        }
    }

    @DisplayName("Cadastrar Estado")
    @Nested
    class CadastrarEstado {

        @DisplayName("Deve cadastrar cidade")
        @Test
        void deveCadastrarEstado() {
            // Arrange
            when(estadoMapper.toEntity(estadoDTO)).thenReturn(estado);
            when(estadoMapper.toDto(estado)).thenReturn(estadoDTO);
            when(estadoRepository.findByNomeOrSigla(estado.getNome(), estado.getSigla()))
                    .thenReturn(Optional.empty());
            when(estadoRepository.save(estado)).thenReturn(estado);

            // Act
            var estadoSalvo = estadoService.salvar(estadoDTO);

            // Assert
            assertThat(estadoSalvo)
                    .isNotNull()
                    .isInstanceOf(EstadoDTO.class)
                    .isEqualTo(estadoDTO);
            verify(estadoRepository).findByNomeOrSigla(estado.getNome(), estado.getSigla());
            verify(estadoRepository).save(estado);
            verify(estadoMapper).toDto(estado);
            verify(estadoMapper).toEntity(estadoDTO);
        }

        @DisplayName("Deve lançar exceção ao tentar salvar estado com sigla ou nome já existente")
        @Test
        void deveGerarExcecao_QuandoCadastrarEstado_ComNomeOuSiglaExistente() {
            // Arrange
            when(estadoRepository.findByNomeOrSigla(estado.getNome(), estado.getSigla()))
                    .thenReturn(Optional.of(estado));

            // Act & Assert
            assertThatThrownBy(() -> estadoService.salvar(estadoDTO))
                    .isInstanceOf(RecursoJaSalvoException.class)
                    .hasMessage("Um estado com sigla '" + estadoDTO.sigla() +
                            "' ou nome '" + estadoDTO.nome() + "' já existe no banco de dados.");
            verify(estadoRepository).findByNomeOrSigla(estado.getNome(), estado.getSigla());
        }
    }

    @DisplayName("Alterar Estado")
    @Nested
    class AlterarEstado{

        @DisplayName("Deve alterar estado cadastrada")
        @Test
        void deveAlterarEstadoPorId() {
            // Arrange
            when(estadoMapper.toEntity(estadoDTO)).thenReturn(estado);
            when(estadoMapper.toDto(estado)).thenReturn(estadoDTO);
            doNothing().when(estadoMapper).updateFromDto(estadoDTO, estado);
            when(estadoRepository.findByNomeOrSiglaAndIdNot(estado.getNome(), estado.getSigla(), estado.getId()))
                    .thenReturn(Optional.empty());
            when(estadoRepository.save(estado)).thenReturn(estado);
            when(estadoRepository.findById(estado.getId())).thenReturn(Optional.of(estado));

            // Act
            var estadoSalvo = estadoService.atualizar(estadoDTO.id(), estadoDTO);

            // Assert
            assertThat(estadoSalvo)
                    .isNotNull()
                    .isInstanceOf(EstadoDTO.class)
                    .isEqualTo(estadoDTO);
            verify(estadoRepository)
                    .findByNomeOrSiglaAndIdNot(estadoDTO.nome(), estadoDTO.sigla(), estadoDTO.id());
            verify(estadoRepository).findById(estadoDTO.id());
            verify(estadoRepository).save(estado);
            verify(estadoMapper).updateFromDto(estadoDTO, estado);
            verify(estadoMapper).toEntity(estadoDTO);
            verify(estadoMapper, times(2)).toDto(estado);
        }

        @DisplayName("Deve lançar exceção ao tentar alterar estado com id inexistente")
        @Test
        void deveGerarExcecao_QuandoAlterarEstado_PorIdInexistente() {
            // Arrange
            when(estadoRepository.findById(estado.getId())).thenReturn(Optional.empty());
            // Act & Assert
            assertThatThrownBy(() -> estadoService.atualizar(estadoDTO.id(), estadoDTO))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Estado não encontrado com id: " + estado.getId());

            verify(estadoRepository).findById(estadoDTO.id());
        }

        @DisplayName("Deve lançar exceção ao tentar alterar estado por estado existente")
        @Test
        void deveGerarExcecao_QuandoAlterarEstado_PorEstadoExistente() {
            // Arrange
            when(estadoMapper.toEntity(estadoDTO)).thenReturn(estado);
            when(estadoMapper.toDto(estado)).thenReturn(estadoDTO);
            when(estadoRepository.findById(estado.getId())).thenReturn(Optional.of(estado));
            when(estadoRepository.findByNomeOrSiglaAndIdNot(estado.getNome(), estado.getSigla(), estado.getId()))
                    .thenReturn(Optional.of(estado));

            // Act & Assert
            assertThatThrownBy(() -> estadoService.atualizar(estadoDTO.id(), estadoDTO))
                    .isInstanceOf(RecursoJaSalvoException.class)
                    .hasMessage("Um estado com sigla '" + estadoDTO.sigla() +
                            "' ou nome '" + estadoDTO.nome() + "' já existe no banco de dados.");

            verify(estadoRepository)
                    .findByNomeOrSiglaAndIdNot(estado.getNome(), estado.getSigla(), estado.getId());
            verify(estadoRepository).findById(estadoDTO.id());
            verifyNoMoreInteractions(estadoRepository);
        }
    }

    @DisplayName("Deletar Estado")
    @Nested
    class DeletarEstado{

        @DisplayName("Deve deletar estado")
        @Test
        void deveDeletarEstadoPorId(){
            // Arrange
            when(estadoRepository.findById(estado.getId()))
                    .thenReturn(Optional.of(estado));
            doNothing().when(estadoRepository).deleteById(estado.getId());

            // Act
            estadoService.deletarPorId(estado.getId());

            // Assert
            verify(estadoRepository).findById(estado.getId());
            verify(estadoRepository).deleteById(estado.getId());
        }

        @DisplayName("Deve lançar exceção ao tentar deletar estado por id inexistente")
        @Test
        void deveGerarExcecao_QuandoDeletarEstado_PorIdInexistente(){
            // Arrange
            when(estadoRepository.findById(estado.getId())).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> estadoService.deletarPorId(estado.getId()))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Estado não encontrado com id: " + estado.getId());

            verify(estadoRepository).findById(estado.getId());
        }
    }
}
