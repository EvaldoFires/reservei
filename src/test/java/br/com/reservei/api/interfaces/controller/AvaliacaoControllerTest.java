package br.com.reservei.api.interfaces.controller;

import br.com.reservei.api.application.dto.AvaliacaoDTO;
import br.com.reservei.api.application.dto.RestauranteDTO;
import br.com.reservei.api.application.usecases.avaliacao.AvaliacaoService;
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

import java.util.List;

import static br.com.reservei.api.infrastructure.utils.GeneralHelper.asJsonString;
import static br.com.reservei.api.infrastructure.utils.AvaliacaoHelper.*;
import static br.com.reservei.api.infrastructure.utils.RestauranteHelper.gerarRestaurante;
import static br.com.reservei.api.infrastructure.utils.RestauranteHelper.gerarRestauranteDto;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
 class AvaliacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private AvaliacaoService avaliacaoService;

    private RestauranteDTO restauranteDTO;
    private AvaliacaoDTO avaliacaoDTO;
    private AvaliacaoDTO avaliacaoDTOSemId;

        @BeforeEach
        void setUp() {
            this.restauranteDTO = gerarRestauranteDto(gerarRestaurante());
            avaliacaoDTO = gerarAvaliacaoDto(gerarAvaliacao());
            avaliacaoDTOSemId = gerarAvaliacaoDtoSemId(restauranteDTO.id());
            AvaliacaoController avaliacaoController = new AvaliacaoController(avaliacaoService);

            mockMvc = MockMvcBuilders.standaloneSetup(avaliacaoController)
                    .setControllerAdvice(new GlobalExceptionHandler())
                    .addFilter((request, response, chain) -> {
                        response.setCharacterEncoding("UTF-8");
                        chain.doFilter(request, response);
                    }, "/*")
                    .build();
        }

        @DisplayName("Buscar Avaliação")
        @Nested
        class buscarAvaliacao{

            @DisplayName("Deve buscar um Avaliação pelo ID fornecido")
            @Test
            void deveBuscarAvaliacaoPorId() throws Exception {
                when(avaliacaoService.buscarPorId(avaliacaoDTO.id())).thenReturn(avaliacaoDTO);

                mockMvc.perform(get("/avaliacao/{idAvaliacao}", avaliacaoDTO.id()))
                        .andExpect(status().isOk())
                        .andExpect(content().json(asJsonString(avaliacaoDTO)));
            }

            @DisplayName("Deve lançar exceção ao buscar Avaliação com ID inexistente")
            @Test
            void deveGerarExcecao_QuandoBuscarAvaliacao_PorIdInexistente() throws Exception {
                doThrow(new RecursoNaoEncontradoException("Avaliação não encontrada com id: " + avaliacaoDTO.id()))
                        .when(avaliacaoService).buscarPorId(avaliacaoDTO.id());

                mockMvc.perform(get("/avaliacao/{idAvaliacao}", avaliacaoDTO.id()))
                        .andExpect(status().isNotFound())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.message")
                                .value("Avaliação não encontrada com id: " + avaliacaoDTO.id()));

            }

            @DisplayName("Deve retornar uma lista de avaliações salvas")
            @Test
            void deveBuscarTodosOsAvaliacaos() throws Exception {
                var avaliacaos = List.of(avaliacaoDTO,
                        new AvaliacaoDTO(2L, 5, "Muito bom", null, avaliacaoDTO.restauranteId()));

                when(avaliacaoService.buscarTodos()).thenReturn(avaliacaos);

                mockMvc.perform(get("/avaliacao"))
                        .andExpect(status().isOk())
                        .andExpect(content().json(asJsonString(avaliacaos)));
            }
        }

    @DisplayName("Salvar Avaliação")
    @Nested
    class SalvarAvaliacao {

        @DisplayName("Deve salvar Avaliação")
        @Test
        void deveSalvarAvaliacao() throws Exception {
            when(avaliacaoService.salvar(avaliacaoDTOSemId))
                    .thenReturn(avaliacaoDTO);

            mockMvc.perform(post("/avaliacao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(avaliacaoDTOSemId)))
                    .andExpect(status().isCreated())
                    .andExpect(content().json(asJsonString(avaliacaoDTO)));

            verify(avaliacaoService).salvar(avaliacaoDTOSemId);
        }

        @DisplayName("Deve lançar exceção ao tentar salvar Avaliação com restaurante inexistente")
        @Test
        void deveGerarExcecao_QuandoSalvarAvaliacao_ComRestauranteInexistente() throws Exception {
            doThrow(new RecursoNaoEncontradoException("Restaurante não encontrado com id: " +
                            avaliacaoDTO.restauranteId()))
                    .when(avaliacaoService).salvar(avaliacaoDTOSemId);

            mockMvc.perform(post("/avaliacao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(avaliacaoDTOSemId)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message")
                            .value("Restaurante não encontrado com id: " +
                                    avaliacaoDTO.restauranteId()));
        }
    }

    @DisplayName("Alterar Avaliação")
    @Nested
    class AlterarAvaliacao {

        @DisplayName("Deve alterar Avaliação cadastrada")
        @Test
        void deveAtualizarAvaliacao() throws Exception {
            when(avaliacaoService.atualizar(avaliacaoDTO.id(), avaliacaoDTOSemId)).thenReturn(avaliacaoDTO);

            mockMvc.perform(put("/avaliacao/{idAvaliacao}", avaliacaoDTO.id())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(avaliacaoDTOSemId)))
                    .andExpect(status().isOk())
                    .andExpect(content().json(asJsonString(avaliacaoDTO)));
        }

        @DisplayName("Deve lançar exceção ao tentar alterar Avaliação com id inexistente")
        @Test
        void deveGerarExcecao_QuandoAlterarAvaliacao_PorIdInexistente() throws Exception {
            doThrow(new RecursoNaoEncontradoException("Avaliação não encontrada com id: " + avaliacaoDTO.id()))
                    .when(avaliacaoService).atualizar(avaliacaoDTO.id(), avaliacaoDTOSemId);

            mockMvc.perform(put("/avaliacao/{idAvaliacao}", avaliacaoDTO.id())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(avaliacaoDTOSemId)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message")
                            .value("Avaliação não encontrada com id: " + avaliacaoDTO.id()));
        }

        @DisplayName("Deve lançar exceção ao tentar salvar Avaliação com restaurante inexistente")
        @Test
        void deveGerarExcecao_QuandoAlterarAvaliacao_ComEstadoInexistente() throws Exception {
            doThrow(new RecursoNaoEncontradoException("Restaurante não encontrado com id: " +
                    avaliacaoDTO.restauranteId()))
                    .when(avaliacaoService).atualizar(avaliacaoDTO.id(), avaliacaoDTOSemId);

            mockMvc.perform(put("/avaliacao/{idAvaliacao}", avaliacaoDTO.id())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(avaliacaoDTOSemId)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message")
                            .value("Restaurante não encontrado com id: " +
                                    avaliacaoDTO.restauranteId()));
        }
    }

    @DisplayName("Deletar Avaliação")
    @Nested
    class DeletarAvaliacao {

        @DisplayName("Deve deletar Avaliação")
        @Test
        void deveDeletarAvaliacao() throws Exception {
            doNothing().when(avaliacaoService).deletarPorId(1L);

            mockMvc.perform(delete("/avaliacao/{idAvaliacao}", 1L))
                    .andExpect(status().isNoContent());
        }

        @DisplayName("Deve lançar exceção ao tentar deletar Avaliação por id inexistente")
        @Test
        void deveGerarExcecao_QuandoDeletarAvaliacao_PorIdInexistente() throws Exception {
            doThrow(new RecursoNaoEncontradoException("Avaliação não encontrada com id: " + avaliacaoDTO.id()))
                    .when(avaliacaoService).deletarPorId(avaliacaoDTO.id());

            mockMvc.perform(delete("/avaliacao/{idAvaliacao}", avaliacaoDTO.id()))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message")
                            .value("Avaliação não encontrada com id: " + avaliacaoDTO.id()));
        }
    }
}
