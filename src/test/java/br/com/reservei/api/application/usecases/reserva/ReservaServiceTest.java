package br.com.reservei.api.application.usecases.reserva;

import br.com.reservei.api.application.dto.ReservaDTO;
import br.com.reservei.api.application.dto.RestauranteDTO;
import br.com.reservei.api.application.usecases.restaurante.RestauranteService;
import br.com.reservei.api.domain.exceptions.RecursoNaoEncontradoException;
import br.com.reservei.api.interfaces.mapper.ReservaMapper;
import br.com.reservei.api.domain.model.Reserva;
import br.com.reservei.api.domain.repository.ReservaRepository;
import br.com.reservei.api.infrastructure.utils.ReservaHelper;
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

import static br.com.reservei.api.infrastructure.utils.ReservaHelper.gerarReserva;
import static br.com.reservei.api.infrastructure.utils.ReservaHelper.gerarReservaDto;
import static br.com.reservei.api.infrastructure.utils.RestauranteHelper.gerarRestaurante;
import static br.com.reservei.api.infrastructure.utils.RestauranteHelper.gerarRestauranteDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private ReservaMapper reservaMapper;

    @Mock
    private RestauranteService restauranteService;

    @InjectMocks
    private ReservaServiceImpl reservaService;

    private Reserva reserva;
    private ReservaDTO reservaDTO;
    private RestauranteDTO restauranteDTO;
    @BeforeEach
    void setUp(){
        this.reserva = gerarReserva();
        this.reservaDTO = gerarReservaDto(reserva);
        this.restauranteDTO = gerarRestauranteDto(gerarRestaurante());
    }

    @DisplayName("Buscar Reserva")
    @Nested
    class BuscarReserva {

        @DisplayName("Deve buscar uma Reserva pelo ID fornecido")
        @Test
        void deveBuscarReservaPorId() {
            // Arrange
            when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));
            when(reservaMapper.toDto(reserva)).thenReturn(gerarReservaDto(reserva));

            // Act
            var reservaRecebido = reservaService.buscarPorId(reserva.getId());

            // Assert
            assertThat(reservaRecebido)
                    .usingRecursiveComparison()
                    .ignoringFields("restauranteId")
                    .isEqualTo(reserva);
            assertThat(reservaRecebido.restauranteId()).isEqualTo(reserva.getRestaurante().getId());

            verify(reservaRepository).findById(reserva.getId());
        }

        @DisplayName("Deve lançar exceção ao buscar Reserva com ID inexistente")
        @Test
        void deveGerarExcecao_QuandoBuscarReserva_PorIdInexistente() {
            // Arrange
            when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> reservaService.buscarPorId(reserva.getId()))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Reserva não encontrada com id: " + reserva.getId());
            verify(reservaRepository).findById(reserva.getId());
        }

        @DisplayName("Deve retornar uma lista de reservas salvos")
        @Test
        void deveBuscarTodosOsReserva() {
            // Arrange
            var reservas = List.of(gerarReserva(), gerarReserva(), gerarReserva());
            var reservasDto = reservas.stream()
                    .map(ReservaHelper::gerarReservaDto)
                    .toList();

            when(reservaRepository.findAll()).thenReturn(reservas);
            when(reservaMapper.toDto(any(Reserva.class)))
                    .thenAnswer(invocation -> {
                        reserva = invocation.getArgument(0);
                        return gerarReservaDto(reserva);
                    });

            // Act
            List<ReservaDTO> reservasRecebidos = reservaService.buscarTodos();

            // Assert
            assertThat(reservasRecebidos)
                    .isNotNull()
                    .isNotEmpty()
                    .hasSize(3)
                    .containsExactlyElementsOf(reservasDto);
            verify(reservaRepository).findAll();
            verify(reservaMapper, times(3)).toDto(any(Reserva.class));
        }
    }

    @DisplayName("Salvar Reserva")
    @Nested
    class SalvarReserva {

        @DisplayName("Deve salvar Reserva")
        @Test
        void deveSalvarReserva() {
            // Arrange
            when(reservaMapper.toEntity(reservaDTO)).thenReturn(reserva);
            when(reservaMapper.toDto(reserva)).thenReturn(reservaDTO);
            when(restauranteService.buscarPorId(reservaDTO.restauranteId())).thenReturn(restauranteDTO);
            when(reservaRepository.save(reserva)).thenReturn(reserva);

            // Act
            var reservaSalvo = reservaService.salvar(reservaDTO);

            // Assert
            assertThat(reservaSalvo)
                    .isNotNull()
                    .isInstanceOf(ReservaDTO.class)
                    .isEqualTo(reservaDTO);
            verify(restauranteService).buscarPorId(reservaDTO.restauranteId());
            verify(reservaRepository).save(reserva);
            verify(reservaMapper).toDto(reserva);
            verify(reservaMapper).toEntity(reservaDTO);
        }

        @DisplayName("Deve lançar exceção ao tentar salvar Reserva com restaurante inexistente")
        @Test
        void deveGerarExcecao_QuandoSalvarReserva_ComRestauranteInexistente() {
            // Arrange
            when(restauranteService.buscarPorId(reservaDTO.restauranteId())).thenThrow(new
                    RecursoNaoEncontradoException("Restaurante não encontrado com id: " + reservaDTO.restauranteId()));

            // Act & Assert
            assertThatThrownBy(() -> reservaService.salvar(reservaDTO))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Restaurante não encontrado com id: " + reservaDTO.restauranteId());
            verify(restauranteService).buscarPorId(reservaDTO.restauranteId());
        }
    }

    @DisplayName("Alterar Reserva")
    @Nested
    class AlterarReserva{

        @DisplayName("Deve alterar Reserva cadastrada")
        @Test
        void deveAlterarReservaPorId() {
            // Arrange
            when(reservaMapper.toEntity(reservaDTO)).thenReturn(reserva);
            when(reservaMapper.toDto(reserva)).thenReturn(reservaDTO);
            doNothing().when(reservaMapper).updateFromDto(reservaDTO, reserva);
            when(restauranteService.buscarPorId(reservaDTO.restauranteId())).thenReturn(restauranteDTO);
            when(reservaRepository.save(reserva)).thenReturn(reserva);
            when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));

            // Act
            var reservaSalvo = reservaService.atualizar(reservaDTO.id(), reservaDTO);

            // Assert
            assertThat(reservaSalvo)
                    .isNotNull()
                    .isInstanceOf(ReservaDTO.class)
                    .isEqualTo(reservaDTO);
            verify(restauranteService).buscarPorId(reservaDTO.restauranteId());
            verify(reservaRepository).findById(reservaDTO.id());
            verify(reservaRepository).save(reserva);
            verify(reservaMapper).updateFromDto(reservaDTO, reserva);
            verify(reservaMapper).toEntity(reservaDTO);
            verify(reservaMapper, times(2)).toDto(reserva);
        }

        @DisplayName("Deve lançar exceção ao tentar alterar Reserva com id inexistente")
        @Test
        void deveGerarExcecao_QuandoAlterarReserva_PorIdInexistente() {
            // Arrange
            when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.empty());
            // Act & Assert
            assertThatThrownBy(() -> reservaService.atualizar(reservaDTO.id(), reservaDTO))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Reserva não encontrada com id: " + reserva.getId());

            verify(reservaRepository).findById(reservaDTO.id());
        }

        @DisplayName("Deve lançar exceção ao tentar alterar Reserva por restaurante inexistente")
        @Test
        void deveGerarExcecao_QuandoAlterarReserva_PorRestauranteInexistente() {
            // Arrange
            when(reservaMapper.toEntity(reservaDTO)).thenReturn(reserva);
            when(reservaMapper.toDto(reserva)).thenReturn(reservaDTO);
            when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));
            when(restauranteService.buscarPorId(reservaDTO.restauranteId())).thenThrow(new
                    RecursoNaoEncontradoException("Restaurante não encontrado com id: " + reservaDTO.restauranteId()));

            // Act & Assert
            assertThatThrownBy(() -> reservaService.atualizar(reservaDTO.id(), reservaDTO))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Restaurante não encontrado com id: " + reservaDTO.restauranteId());

            verify(restauranteService).buscarPorId(reservaDTO.restauranteId());
            verify(reservaRepository).findById(reservaDTO.id());
            verifyNoMoreInteractions(reservaRepository);
        }
    }

    @DisplayName("Deletar Reserva")
    @Nested
    class DeletarReserva{

        @DisplayName("Deve deletar Reserva")
        @Test
        void deveDeletarReservaPorId(){
            // Arrange
            when(reservaRepository.findById(reserva.getId()))
                    .thenReturn(Optional.of(reserva));
            doNothing().when(reservaRepository).deleteById(reserva.getId());

            // Act
            reservaService.deletarPorId(reserva.getId());

            // Assert
            verify(reservaRepository).findById(reserva.getId());
            verify(reservaRepository).deleteById(reserva.getId());
        }

        @DisplayName("Deve lançar exceção ao tentar deletar Reserva por id inexistente")
        @Test
        void deveGerarExcecao_QuandoDeletarReserva_PorIdInexistente(){
            // Arrange
            when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> reservaService.deletarPorId(reserva.getId()))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Reserva não encontrada com id: " + reserva.getId());

            verify(reservaRepository).findById(reserva.getId());
        }
    }
}
