package br.com.reservei.api.application.usecases.reserva;

import br.com.reservei.api.application.dto.ReservaDTO;
import br.com.reservei.api.application.dto.RestauranteDTO;
import br.com.reservei.api.application.usecases.endereco.CidadeServiceImpl;
import br.com.reservei.api.application.usecases.endereco.EnderecoServiceImpl;
import br.com.reservei.api.application.usecases.endereco.EstadoServiceImpl;
import br.com.reservei.api.application.usecases.restaurante.RestauranteServiceImpl;
import br.com.reservei.api.domain.exceptions.RecursoNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static br.com.reservei.api.infrastructure.utils.CidadeHelper.gerarCidadeDtoSemId;
import static br.com.reservei.api.infrastructure.utils.EnderecoHelper.gerarEnderecoDtoSemId;
import static br.com.reservei.api.infrastructure.utils.EstadoHelper.gerarEstadoDto;
import static br.com.reservei.api.infrastructure.utils.EstadoHelper.gerarEstadoSemId;
import static br.com.reservei.api.infrastructure.utils.ReservaHelper.gerarReservaDtoSemId;
import static br.com.reservei.api.infrastructure.utils.RestauranteHelper.gerarRestauranteDtoSemId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
class ReservaServiceIT {

    @Autowired
    private EstadoServiceImpl estadoService;

    @Autowired
    private CidadeServiceImpl cidadeService;

    @Autowired
    private EnderecoServiceImpl enderecoService;

    @Autowired
    private RestauranteServiceImpl restauranteService;

    @Autowired
    private ReservaServiceImpl reservaService;

    private ReservaDTO reservaDTO;
    private RestauranteDTO restauranteDTO;

    @BeforeEach
    void setUp() {
        var estadoDTO = gerarEstadoDto(gerarEstadoSemId());
        estadoDTO = estadoService.salvar(estadoDTO);
        var cidadeDTO = gerarCidadeDtoSemId(estadoDTO.id());
        cidadeDTO = cidadeService.salvar(cidadeDTO);
        var enderecoDTO = gerarEnderecoDtoSemId(cidadeDTO.id());
        enderecoDTO = enderecoService.salvar(enderecoDTO);
        this.restauranteDTO = gerarRestauranteDtoSemId(enderecoDTO.id());
        this.restauranteDTO = restauranteService.salvar(restauranteDTO);
        this.reservaDTO = gerarReservaDtoSemId(restauranteDTO.id());
    }

    @DisplayName("Buscar Reserva")
    @Nested
    class BuscarReserva {

        @DisplayName("Deve buscar um Reserva pelo ID fornecido")
        @Test
        void deveBuscarReservaPorId() {
            reservaDTO = reservaService.salvar(reservaDTO);

            var reservaRecebido = reservaService.buscarPorId(reservaDTO.id());

            assertThat(reservaRecebido)
                    .usingRecursiveComparison()
                    .isEqualTo(reservaDTO);
        }

        @DisplayName("Deve lançar exceção ao buscar Reserva com ID inexistente")
        @Test
        void deveGerarExcecao_QuandoBuscarReserva_PorIdInexistente() {
            Long id = 1L;
            assertThatThrownBy(() -> reservaService.buscarPorId(id))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Reserva não encontrada com id: " + id);
        }

        @DisplayName("Deve retornar uma lista de reservas salvos")
        @Test
        void deveBuscarTodasAsReserva() {
            var reservaDTO1 = reservaService.salvar(reservaDTO);
            var reservaDTO2 = reservaService.salvar(reservaDTO);
            var reservaDTO3 = reservaService.salvar(reservaDTO);

            var reservasSalvas = List.of(reservaDTO1, reservaDTO2, reservaDTO3);

            List<ReservaDTO> reservasRecebidos = reservaService.buscarTodos();

            // Assert
            assertThat(reservasRecebidos)
                    .isNotNull()
                    .isNotEmpty()
                    .hasSize(3)
                    .containsExactlyElementsOf(reservasSalvas);
        }
    }

    @DisplayName("Cadastrar Reserva")
    @Nested
    class CadastrarReserva {

        @DisplayName("Deve cadastrar Reserva")
        @Test
        void deveCadastrarReserva() {
            var reservaSalva = reservaService.salvar(reservaDTO);

            // Assert
            assertThat(reservaSalva)
                    .isNotNull()
                    .isInstanceOf(ReservaDTO.class)
                    .usingRecursiveComparison()
                    .ignoringFields("id")
                    .isEqualTo(reservaDTO);

            assertThat(reservaSalva.id())
                    .isNotNull();
        }

        @DisplayName("Deve lançar exceção ao tentar salvar Reserva com restaurante inexistente")
        @Test
        void deveGerarExcecao_QuandoCadastrarReserva_ComRestauranteInexistente() {
            reservaDTO = new ReservaDTO(null, 2L, LocalDateTime.now().withHour(15));
            assertThatThrownBy(() -> reservaService.salvar(reservaDTO))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Restaurante não encontrado com id: " +
                            reservaDTO.restauranteId());
        }
    }

    @DisplayName("Alterar Reserva")
    @Nested
    class AlterarReserva {

        @DisplayName("Deve alterar Reserva cadastrado")
        @Test
        void deveAlterarReservaPorId() {
            reservaDTO = reservaService.salvar(reservaDTO);
            var reservaAtualizada = reservaService.atualizar(reservaDTO.id(),
                    new ReservaDTO(null, reservaDTO.restauranteId(), LocalDateTime.now().withHour(15)));

            assertThat(reservaAtualizada)
                    .isNotNull()
                    .isInstanceOf(ReservaDTO.class)
                    .isNotEqualTo(reservaDTO);
            assertThat(reservaAtualizada.id())
                    .isEqualTo(reservaDTO.id());
        }

        @DisplayName("Deve lançar exceção ao tentar alterar Reserva com id inexistente")
        @Test
        void deveGerarExcecao_QuandoAlterarReserva_PorIdInexistente() {
            Long id = 1L;
            assertThatThrownBy(() -> reservaService.atualizar(id, reservaDTO))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Reserva não encontrada com id: " + id);
        }

        @DisplayName("Deve lançar exceção ao tentar alterar Reserva por cidade inexistente")
        @Test
        void deveGerarExcecao_QuandoAlterarReserva_PorRestauranteInexistente() {
            Long id = 2L;
            reservaDTO = reservaService.salvar(reservaDTO);
            reservaDTO = new ReservaDTO(reservaDTO.id(), id, LocalDateTime.now().withHour(15));

            assertThatThrownBy(() -> reservaService.atualizar(reservaDTO.id(), reservaDTO))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Restaurante não encontrado com id: " +
                            id);
        }
    }

    @DisplayName("Deletar Reserva")
    @Nested
    class DeletarReserva {

        @DisplayName("Deve deletar Reserva")
        @Test
        void deveDeletarReservaPorId() {
            reservaDTO = reservaService.salvar(reservaDTO);
            reservaService.deletarPorId(reservaDTO.id());

            assertThatThrownBy(() -> reservaService.buscarPorId(reservaDTO.id()))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Reserva não encontrada com id: " + reservaDTO.id());
        }

        @DisplayName("Deve lançar exceção ao tentar deletar Reserva por id inexistente")
        @Test
        void deveGerarExcecao_QuandoDeletarReserva_PorIdInexistente() {
            Long id = 1L;
            assertThatThrownBy(() -> reservaService.deletarPorId(id))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Reserva não encontrada com id: " + id);
        }
    }
}

