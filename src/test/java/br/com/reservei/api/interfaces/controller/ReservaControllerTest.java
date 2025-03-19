package br.com.reservei.api.interfaces.controller;

import br.com.reservei.api.application.dto.RestauranteDTO;
import br.com.reservei.api.application.dto.ReservaDTO;
import br.com.reservei.api.application.usecases.reserva.ReservaService;
import br.com.reservei.api.domain.exceptions.GlobalExceptionHandler;
import br.com.reservei.api.domain.exceptions.RecursoNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static br.com.reservei.api.infrastructure.utils.RestauranteHelper.gerarRestaurante;
import static br.com.reservei.api.infrastructure.utils.RestauranteHelper.gerarRestauranteDto;
import static br.com.reservei.api.infrastructure.utils.GeneralHelper.asJsonString;
import static br.com.reservei.api.infrastructure.utils.ReservaHelper.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
 class ReservaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private ReservaService reservaService;

    private RestauranteDTO restauranteDTO;
    private ReservaDTO reservaDTO;
    private ReservaDTO reservaDTOSemId;

        @BeforeEach
        void setUp() {
            this.restauranteDTO = gerarRestauranteDto(gerarRestaurante());
            reservaDTO = gerarReservaDto(gerarReserva());
            reservaDTOSemId = gerarReservaDtoSemId(restauranteDTO.id());
            ReservaController reservaController = new ReservaController(reservaService);

            mockMvc = MockMvcBuilders.standaloneSetup(reservaController)
                    .setControllerAdvice(new GlobalExceptionHandler())
                    .addFilter((request, response, chain) -> {
                        response.setCharacterEncoding("UTF-8");
                        chain.doFilter(request, response);
                    }, "/*")
                    .build();
        }

        @DisplayName("Buscar Reserva")
        @Nested
        class buscarReserva{

            @DisplayName("Deve buscar um Reserva pelo ID fornecido")
            @Test
            void deveBuscarReservaPorId() throws Exception {
                when(reservaService.buscarPorId(reservaDTO.id())).thenReturn(reservaDTO);

                mockMvc.perform(get("/reserva/{idReserva}", reservaDTO.id()))
                        .andExpect(status().isOk())
                        .andExpect(content().json(asJsonString(reservaDTO)));
            }

            @DisplayName("Deve lançar exceção ao buscar Reserva com ID inexistente")
            @Test
            void deveGerarExcecao_QuandoBuscarReserva_PorIdInexistente() throws Exception {
                doThrow(new RecursoNaoEncontradoException("Reserva não encontrado com id: " + reservaDTO.id()))
                        .when(reservaService).buscarPorId(reservaDTO.id());

                mockMvc.perform(get("/reserva/{idReserva}", reservaDTO.id()))
                        .andExpect(status().isNotFound())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.message")
                                .value("Reserva não encontrado com id: " + reservaDTO.id()));

            }

            @DisplayName("Deve retornar uma lista de reservas salvos")
            @Test
            void deveBuscarTodosOsReservas() throws Exception {
                var reservas = List.of(reservaDTO,
                        new ReservaDTO(2L, restauranteDTO.id(), LocalDateTime.now()));

                when(reservaService.buscarTodos()).thenReturn(reservas);

                mockMvc.perform(get("/reserva"))
                        .andExpect(status().isOk())
                        .andExpect(content().json(asJsonString(reservas)));
            }
        }

    @DisplayName("Salvar Reserva")
    @Nested
    class SalvarReserva {

        @DisplayName("Deve salvar Reserva")
        @Test
        void deveSalvarReserva() throws Exception {
            when(reservaService.salvar(reservaDTOSemId))
                    .thenReturn(reservaDTO);

            mockMvc.perform(post("/reserva")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(reservaDTOSemId)))
                    .andExpect(status().isCreated())
                    .andExpect(content().json(asJsonString(reservaDTO)));

            verify(reservaService).salvar(reservaDTOSemId);
        }

        @DisplayName("Deve lançar exceção ao tentar salvar Reserva com restaurante inexistente")
        @Test
        void deveGerarExcecao_QuandoSalvarReserva_ComRestauranteInexistente() throws Exception {
            doThrow(new RecursoNaoEncontradoException("Restaurante não encontrado com id: " +
                            reservaDTO.restauranteId()))
                    .when(reservaService).salvar(reservaDTOSemId);

            mockMvc.perform(post("/reserva")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(reservaDTOSemId)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message")
                            .value("Restaurante não encontrado com id: " +
                                    reservaDTO.restauranteId()));
        }
    }

    @DisplayName("Alterar Reserva")
    @Nested
    class AlterarReserva {

        @DisplayName("Deve alterar Reserva cadastrada")
        @Test
        void deveAtualizarReserva() throws Exception {
            when(reservaService.atualizar(reservaDTO.id(), reservaDTOSemId)).thenReturn(reservaDTO);

            mockMvc.perform(put("/reserva/{idReserva}", reservaDTO.id())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(reservaDTOSemId)))
                    .andExpect(status().isOk())
                    .andExpect(content().json(asJsonString(reservaDTO)));
        }

        @DisplayName("Deve lançar exceção ao tentar alterar Reserva com id inexistente")
        @Test
        void deveGerarExcecao_QuandoAlterarReserva_PorIdInexistente() throws Exception {
            doThrow(new RecursoNaoEncontradoException("Reserva não encontrada com id: " + reservaDTO.id()))
                    .when(reservaService).atualizar(reservaDTO.id(), reservaDTOSemId);

            mockMvc.perform(put("/reserva/{idReserva}", reservaDTO.id())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(reservaDTOSemId)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message")
                            .value("Reserva não encontrada com id: " + reservaDTO.id()));
        }

        @DisplayName("Deve lançar exceção ao tentar salvar Reserva com restaurante inexistente")
        @Test
        void deveGerarExcecao_QuandoAlterarReserva_ComEstadoInexistente() throws Exception {
            doThrow(new RecursoNaoEncontradoException("Restaurante não encontrado com id: " +
                    reservaDTO.restauranteId()))
                    .when(reservaService).atualizar(reservaDTO.id(), reservaDTOSemId);

            mockMvc.perform(put("/reserva/{idReserva}", reservaDTO.id())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(reservaDTOSemId)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message")
                            .value("Restaurante não encontrado com id: " +
                                    reservaDTO.restauranteId()));
        }
    }

    @DisplayName("Deletar Reserva")
    @Nested
    class DeletarReserva {

        @DisplayName("Deve deletar Reserva")
        @Test
        void deveDeletarReserva() throws Exception {
            doNothing().when(reservaService).deletarPorId(1L);

            mockMvc.perform(delete("/reserva/{idReserva}", 1L))
                    .andExpect(status().isNoContent());
        }

        @DisplayName("Deve lançar exceção ao tentar deletar Reserva por id inexistente")
        @Test
        void deveGerarExcecao_QuandoDeletarReserva_PorIdInexistente() throws Exception {
            doThrow(new RecursoNaoEncontradoException("Reserva não encontrado com id: " + reservaDTO.id()))
                    .when(reservaService).deletarPorId(reservaDTO.id());

            mockMvc.perform(delete("/reserva/{idReserva}", reservaDTO.id()))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message")
                            .value("Reserva não encontrado com id: " + reservaDTO.id()));
        }
    }
}
