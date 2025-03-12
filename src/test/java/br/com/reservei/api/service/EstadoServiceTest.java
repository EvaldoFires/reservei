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

    @Nested
    class BuscarEstado {
        @Test
        void deveBuscarEstadoPorId() {
            // Arrange
            when(estadoRepository.findById(anyLong()))
                    .thenReturn(Optional.of(estado));
            when(estadoMapper.toDto(any()))
                    .thenReturn(gerarEstadoDto(estado));

            // Act
            var estadoRecebido = estadoService.buscarPorId(estado.getId());

            // Assert
            verify(estadoRepository, times(1)).findById(estado.getId());
            assertThat(estadoRecebido)
                    .usingRecursiveComparison()
                    .ignoringFields("cidades")
                    .isEqualTo(estado);
        }

        @Test
        void deveGerarExcecao_QuandoBuscarEstado_PorIdInexistente() {
            // Arrange
            when(estadoRepository.findById(anyLong()))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> estadoService.buscarPorId(estado.getId()))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Estado não encontrado com id: " + estado.getId());
            verify(estadoRepository, times(1)).findById(estado.getId());
        }

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
            verify(estadoRepository, times(1)).findAll();
            verify(estadoMapper, times(3)).toDto(any(Estado.class));
        }
    }

    @Nested
    class CadastrarEstado {

        @Test
        void deveCadastrarEstado() {
            // Arrange
            when(estadoMapper.toEntity(estadoDTO)).thenReturn(estado);
            when(estadoMapper.toDto(estado)).thenReturn(estadoDTO);
            when(estadoRepository.findByNomeOrSigla(anyString(), anyString())).thenReturn(Optional.empty());
            when(estadoRepository.save(estado)).thenReturn(estado);

            // Act
            var estadoSalvo = estadoService.salvar(estadoDTO);

            // Assert
            assertThat(estadoSalvo)
                    .isNotNull()
                    .isInstanceOf(EstadoDTO.class)
                    .isEqualTo(estadoDTO);
            verify(estadoRepository, times(1)).findByNomeOrSigla(estado.getNome(), estado.getSigla());
            verify(estadoRepository, times(1)).save(estado);
            verify(estadoMapper, times(1)).toDto(estado);
            verify(estadoMapper, times(1)).toEntity(estadoDTO);
        }

        @Test
        void deveGerarExcecao_QuandoCadastrarEstado_ComNomeOuSiglaExistente() {
            // Arrange
            when(estadoRepository.findByNomeOrSigla(anyString(), anyString())).thenReturn(Optional.of(estado));

            // Act & Assert
            assertThatThrownBy(() -> estadoService.salvar(estadoDTO))
                    .isInstanceOf(RecursoJaSalvoException.class)
                    .hasMessage("Um estado com sigla '" + estadoDTO.sigla() +
                            "' ou nome '" + estadoDTO.nome() + "' já existe no banco de dados.");
            verify(estadoRepository, times(1)).findByNomeOrSigla(estado.getNome(), estado.getSigla());
        }
    }
    @Nested
    class AlterarEstado{
        @Test
        void deveAlterarEstadoPorId() {
            // Arrange
            when(estadoMapper.toEntity(estadoDTO)).thenReturn(estado);
            when(estadoMapper.toDto(estado)).thenReturn(estadoDTO);
            doNothing().when(estadoMapper).updateFromDto(estadoDTO, estado);
            when(estadoRepository.findByNomeOrSiglaAndIdNot(anyString(), anyString(), anyLong()))
                    .thenReturn(Optional.empty());
            when(estadoRepository.save(estado)).thenReturn(estado);
            when(estadoRepository.findById(anyLong())).thenReturn(Optional.of(estado));

            // Act
            var estadoSalvo = estadoService.atualizar(estadoDTO.id(), estadoDTO);

            // Assert
            assertThat(estadoSalvo)
                    .isNotNull()
                    .isInstanceOf(EstadoDTO.class)
                    .isEqualTo(estadoDTO);
            verify(estadoRepository, times(1))
                    .findByNomeOrSiglaAndIdNot(estadoDTO.nome(), estadoDTO.sigla(), estadoDTO.id());
            verify(estadoRepository, times(1)).findById(estadoDTO.id());
            verify(estadoRepository, times(1)).save(estado);
            verify(estadoMapper, times(2)).toDto(estado);
            verify(estadoMapper, times(1)).updateFromDto(estadoDTO, estado);
            verify(estadoMapper, times(1)).toEntity(estadoDTO);
        }

        @Test
        void deveGerarExcecao_QuandoTentarAlterarEstado_PorIdInexistente() {
            // Arrange
            when(estadoRepository.findById(anyLong())).thenReturn(Optional.empty());
            // Act & Assert
            assertThatThrownBy(() -> estadoService.atualizar(estadoDTO.id(), estadoDTO))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Estado não encontrado com id: " + estado.getId());

            verify(estadoRepository, times(1)).findById(estadoDTO.id());
            verifyNoMoreInteractions(estadoRepository);
        }

        @Test
        void deveGerarExcecao_QuandoTentarAlterarEstado_PorEstadoExistente() {
            // Arrange
            when(estadoMapper.toEntity(estadoDTO)).thenReturn(estado);
            when(estadoMapper.toDto(estado)).thenReturn(estadoDTO);
            when(estadoRepository.findById(anyLong())).thenReturn(Optional.of(estado));
            when(estadoRepository.findByNomeOrSiglaAndIdNot(anyString(), anyString(), anyLong()))
                    .thenReturn(Optional.of(estado));

            // Act & Assert
            assertThatThrownBy(() -> estadoService.atualizar(estadoDTO.id(), estadoDTO))
                    .isInstanceOf(RecursoJaSalvoException.class)
                    .hasMessage("Um estado com sigla '" + estadoDTO.sigla() +
                            "' ou nome '" + estadoDTO.nome() + "' já existe no banco de dados.");

            verify(estadoRepository, times(1))
                    .findByNomeOrSiglaAndIdNot(estado.getNome(), estado.getSigla(), estado.getId());
            verify(estadoRepository, times(1)).findById(estadoDTO.id());
            verifyNoMoreInteractions(estadoRepository);
        }
    }

    @Nested
    class DeletarEstado{

        @Test
        void deveDeletarEstadoPorId(){
            // Arrange
            when(estadoRepository.findById(anyLong()))
                    .thenReturn(Optional.of(estado));
            doNothing().when(estadoRepository).deleteById(estado.getId());

            // Act
            estadoService.deletarPorId(estado.getId());

            // Assert
            verify(estadoRepository, times(1)).findById(estado.getId());
            verify(estadoRepository, times(1)).deleteById(estado.getId());
        }

        @Test
        void deveGerarExcecao_QuandoTentarDeletarEstado_PorIdInexistente(){
            // Arrange
            when(estadoRepository.findById(anyLong()))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> estadoService.deletarPorId(estado.getId()))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Estado não encontrado com id: " + estado.getId());
            verify(estadoRepository, times(1)).findById(estado.getId());
        }
    }
}
