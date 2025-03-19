package br.com.reservei.api.interfaces.controller;

import br.com.reservei.api.application.dto.EnderecoDTO;
import br.com.reservei.api.application.dto.RestauranteDTO;
import br.com.reservei.api.application.usecases.restaurante.RestauranteService;
import br.com.reservei.api.domain.exceptions.GlobalExceptionHandler;
import br.com.reservei.api.domain.exceptions.RecursoNaoEncontradoException;
import br.com.reservei.api.infrastructure.utils.Cozinha;
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

import java.time.LocalTime;
import java.util.List;

import static br.com.reservei.api.infrastructure.utils.EnderecoHelper.gerarEndereco;
import static br.com.reservei.api.infrastructure.utils.EnderecoHelper.gerarEnderecoDto;
import static br.com.reservei.api.infrastructure.utils.RestauranteHelper.*;
import static br.com.reservei.api.infrastructure.utils.GeneralHelper.asJsonString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
 class RestauranteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private RestauranteService restauranteService;

    private EnderecoDTO enderecoDTO;
    private RestauranteDTO restauranteDTO;
    private RestauranteDTO restauranteDTOSemId;

        @BeforeEach
        void setUp() {
            this.enderecoDTO = gerarEnderecoDto(gerarEndereco());
            restauranteDTO = gerarRestauranteDto(gerarRestaurante());
            restauranteDTOSemId = gerarRestauranteDtoSemId(enderecoDTO.id());
            RestauranteController restauranteController = new RestauranteController(restauranteService);

            mockMvc = MockMvcBuilders.standaloneSetup(restauranteController)
                    .setControllerAdvice(new GlobalExceptionHandler())
                    .addFilter((request, response, chain) -> {
                        response.setCharacterEncoding("UTF-8");
                        chain.doFilter(request, response);
                    }, "/*")
                    .build();
        }

        @DisplayName("Buscar Restaurante")
        @Nested
        class buscarRestaurante{

            @DisplayName("Deve buscar um Restaurante pelo ID fornecido")
            @Test
            void deveBuscarRestaurantePorId() throws Exception {
                when(restauranteService.buscarPorId(restauranteDTO.id())).thenReturn(restauranteDTO);

                mockMvc.perform(get("/restaurante/{idRestaurante}", restauranteDTO.id()))
                        .andExpect(status().isOk())
                        .andExpect(content().json(asJsonString(restauranteDTO)));
            }

            @DisplayName("Deve lançar exceção ao buscar Restaurante com ID inexistente")
            @Test
            void deveGerarExcecao_QuandoBuscarRestaurante_PorIdInexistente() throws Exception {
                doThrow(new RecursoNaoEncontradoException("Restaurante não encontrado com id: " + restauranteDTO.id()))
                        .when(restauranteService).buscarPorId(restauranteDTO.id());

                mockMvc.perform(get("/restaurante/{idRestaurante}", restauranteDTO.id()))
                        .andExpect(status().isNotFound())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.message")
                                .value("Restaurante não encontrado com id: " + restauranteDTO.id()));

            }

            @DisplayName("Deve retornar uma lista de restaurantes salvos")
            @Test
            void deveBuscarTodosOsRestaurantes() throws Exception {
                var restaurantes = List.of(restauranteDTO,
                        new RestauranteDTO(2L, "Di Basilico", Cozinha.ITALIANA, enderecoDTO.id(),
                                2, LocalTime.NOON, LocalTime.MIDNIGHT));

                when(restauranteService.buscarTodos()).thenReturn(restaurantes);

                mockMvc.perform(get("/restaurante"))
                        .andExpect(status().isOk())
                        .andExpect(content().json(asJsonString(restaurantes)));
            }
        }

    @DisplayName("Salvar Restaurante")
    @Nested
    class SalvarRestaurante {

        @DisplayName("Deve salvar Restaurante")
        @Test
        void deveSalvarRestaurante() throws Exception {
            when(restauranteService.salvar(restauranteDTOSemId))
                    .thenReturn(restauranteDTO);

            mockMvc.perform(post("/restaurante")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(restauranteDTOSemId)))
                    .andExpect(status().isCreated())
                    .andExpect(content().json(asJsonString(restauranteDTO)));

            verify(restauranteService).salvar(restauranteDTOSemId);
        }

        @DisplayName("Deve lançar exceção ao tentar salvar Restaurante com endereço inexistente")
        @Test
        void deveGerarExcecao_QuandoSalvarRestaurante_ComEnderecoInexistente() throws Exception {
            doThrow(new RecursoNaoEncontradoException("Endereço não encontrado com id: " +
                            restauranteDTO.enderecoId()))
                    .when(restauranteService).salvar(restauranteDTOSemId);

            mockMvc.perform(post("/restaurante")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(restauranteDTOSemId)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message")
                            .value("Endereço não encontrado com id: " +
                                    restauranteDTO.enderecoId()));
        }
    }

    @DisplayName("Alterar Restaurante")
    @Nested
    class AlterarRestaurante {

        @DisplayName("Deve alterar Restaurante cadastrado")
        @Test
        void deveAtualizarRestaurante() throws Exception {
            when(restauranteService.atualizar(restauranteDTO.id(), restauranteDTOSemId)).thenReturn(restauranteDTO);

            mockMvc.perform(put("/restaurante/{idRestaurante}", restauranteDTO.id())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(restauranteDTOSemId)))
                    .andExpect(status().isOk())
                    .andExpect(content().json(asJsonString(restauranteDTO)));
        }

        @DisplayName("Deve lançar exceção ao tentar alterar Restaurante com id inexistente")
        @Test
        void deveGerarExcecao_QuandoAlterarRestaurante_PorIdInexistente() throws Exception {
            doThrow(new RecursoNaoEncontradoException("Restaurante não encontrado com id: " + restauranteDTO.id()))
                    .when(restauranteService).atualizar(restauranteDTO.id(), restauranteDTOSemId);

            mockMvc.perform(put("/restaurante/{idRestaurante}", restauranteDTO.id())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(restauranteDTOSemId)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message")
                            .value("Restaurante não encontrado com id: " + restauranteDTO.id()));
        }

        @DisplayName("Deve lançar exceção ao tentar salvar Restaurante com endereço inexistente")
        @Test
        void deveGerarExcecao_QuandoAlterarRestaurante_ComEstadoInexistente() throws Exception {
            doThrow(new RecursoNaoEncontradoException("Endereço não encontrado com id: " +
                    restauranteDTO.enderecoId()))
                    .when(restauranteService).atualizar(restauranteDTO.id(), restauranteDTOSemId);

            mockMvc.perform(put("/restaurante/{idRestaurante}", restauranteDTO.id())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(restauranteDTOSemId)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message")
                            .value("Endereço não encontrado com id: " +
                                    restauranteDTO.enderecoId()));
        }
    }

    @DisplayName("Deletar Restaurante")
    @Nested
    class DeletarRestaurante {

        @DisplayName("Deve deletar Restaurante")
        @Test
        void deveDeletarRestaurante() throws Exception {
            doNothing().when(restauranteService).deletarPorId(1L);

            mockMvc.perform(delete("/restaurante/{idRestaurante}", 1L))
                    .andExpect(status().isNoContent());
        }

        @DisplayName("Deve lançar exceção ao tentar deletar Restaurante por id inexistente")
        @Test
        void deveGerarExcecao_QuandoDeletarRestaurante_PorIdInexistente() throws Exception {
            doThrow(new RecursoNaoEncontradoException("Restaurante não encontrado com id: " + restauranteDTO.id()))
                    .when(restauranteService).deletarPorId(restauranteDTO.id());

            mockMvc.perform(delete("/restaurante/{idRestaurante}", restauranteDTO.id()))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message")
                            .value("Restaurante não encontrado com id: " + restauranteDTO.id()));
        }
    }
}
