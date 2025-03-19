package br.com.reservei.api.interfaces.controller;

import br.com.reservei.api.application.dto.CidadeDTO;
import br.com.reservei.api.application.dto.EstadoDTO;
import br.com.reservei.api.application.usecases.endereco.CidadeService;
import br.com.reservei.api.domain.exceptions.GlobalExceptionHandler;
import br.com.reservei.api.domain.exceptions.RecursoJaSalvoException;
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

import static br.com.reservei.api.infrastructure.utils.CidadeHelper.*;
import static br.com.reservei.api.infrastructure.utils.EstadoHelper.gerarEstado;
import static br.com.reservei.api.infrastructure.utils.EstadoHelper.gerarEstadoDto;
import static br.com.reservei.api.infrastructure.utils.GeneralHelper.asJsonString;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
 class CidadeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private CidadeService cidadeService;

    private EstadoDTO estadoDTO;
    private CidadeDTO cidadeDTO;
    private CidadeDTO cidadeDTOSemId;

        @BeforeEach
        void setUp() {
            estadoDTO = gerarEstadoDto(gerarEstado());
            cidadeDTO = gerarCidadeDto(gerarCidade());
            cidadeDTOSemId = gerarCidadeDtoSemId(estadoDTO.id());
            CidadeController cidadeController = new CidadeController(cidadeService);
            mockMvc = MockMvcBuilders.standaloneSetup(cidadeController)
                    .setControllerAdvice(new GlobalExceptionHandler())
                    .addFilter((request, response, chain) -> {
                        response.setCharacterEncoding("UTF-8");
                        chain.doFilter(request, response);
                    }, "/*")
                    .build();
        }

        @DisplayName("Buscar Cidade")
        @Nested
        class buscarCidade{

            @DisplayName("Deve buscar uma Cidade pelo ID fornecido")
            @Test
            void deveBuscarCidadePorId() throws Exception {
                when(cidadeService.buscarPorId(cidadeDTO.id())).thenReturn(cidadeDTO);

                mockMvc.perform(get("/cidade/{idCidade}", cidadeDTO.id()))
                        .andExpect(status().isOk())
                        .andExpect(content().json(asJsonString(cidadeDTO)));
            }

            @DisplayName("Deve lançar exceção ao buscar Cidade com ID inexistente")
            @Test
            void deveGerarExcecao_QuandoBuscarCidade_PorIdInexistente() throws Exception {
                doThrow(new RecursoNaoEncontradoException("Cidade não encontrada com id: " + cidadeDTO.id()))
                        .when(cidadeService).buscarPorId(cidadeDTO.id());

                mockMvc.perform(get("/cidade/{idCidade}", cidadeDTO.id()))
                        .andExpect(status().isNotFound())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.message")
                                .value("Cidade não encontrada com id: " + cidadeDTO.id()));

            }

            @DisplayName("Deve retornar uma lista de cidades salvos")
            @Test
            void deveBuscarTodosOsCidades() throws Exception {
                var cidades = List.of(cidadeDTO,
                        new CidadeDTO(2L, "Camaçari", cidadeDTO.estadoId()));

                when(cidadeService.buscarTodos()).thenReturn(cidades);

                mockMvc.perform(get("/cidade"))
                        .andExpect(status().isOk())
                        .andExpect(content().json(asJsonString(cidades)));
            }
        }

    @DisplayName("Salvar Cidade")
    @Nested
    class SalvarCidade {

        @DisplayName("Deve salvar Cidade")
        @Test
        void deveSalvarCidade() throws Exception {
            when(cidadeService.salvar(cidadeDTOSemId))
                    .thenReturn(cidadeDTO);

            mockMvc.perform(post("/cidade")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(cidadeDTOSemId)))
                    .andExpect(status().isCreated())
                    .andExpect(content().json(asJsonString(cidadeDTO)));

            verify(cidadeService).salvar(cidadeDTOSemId);
        }

        @DisplayName("Deve lançar exceção ao tentar salvar Cidade com nome e estado já existente")
        @Test
        void deveGerarExcecao_QuandoSalvarCidade_ComNomeEEstadoExistente() throws Exception {
            doThrow(new RecursoJaSalvoException("Uma cidade com nome '" + cidadeDTO.nome() +
                    "' do estado '" + estadoDTO.nome() + "' já existe no banco de dados."))
                    .when(cidadeService).salvar(cidadeDTOSemId);

            mockMvc.perform(post("/cidade")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(cidadeDTOSemId)))
                    .andExpect(status().isConflict())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message")
                            .value("Uma cidade com nome '" + cidadeDTO.nome() +
                                    "' do estado '" + estadoDTO.nome() + "' já existe no banco de dados."));
        }

        @DisplayName("Deve lançar exceção ao tentar salvar cidade com estado inexistente")
        @Test
        void deveGerarExcecao_QuandoSalvarCidade_ComEstadoInexistente() throws Exception {
            doThrow(new RecursoNaoEncontradoException("Estado não encontrada com id: " +
                            cidadeDTO.estadoId()))
                    .when(cidadeService).salvar(cidadeDTOSemId);

            mockMvc.perform(post("/cidade")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(cidadeDTOSemId)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message")
                            .value("Estado não encontrada com id: " +
                                    cidadeDTO.estadoId()));
        }
    }

    @DisplayName("Alterar Cidade")
    @Nested
    class AlterarCidade {

        @DisplayName("Deve alterar Cidade cadastrada")
        @Test
        void deveAtualizarCidade() throws Exception {
            when(cidadeService.atualizar(cidadeDTO.id(), cidadeDTOSemId)).thenReturn(cidadeDTO);

            mockMvc.perform(put("/cidade/{idCidade}", cidadeDTO.id())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(cidadeDTOSemId)))
                    .andExpect(status().isOk())
                    .andExpect(content().json(asJsonString(cidadeDTO)));
        }

        @DisplayName("Deve lançar exceção ao tentar alterar Cidade com id inexistente")
        @Test
        void deveGerarExcecao_QuandoAlterarCidade_PorIdInexistente() throws Exception {
            doThrow(new RecursoNaoEncontradoException("Cidade não encontrada com id: " + cidadeDTO.id()))
                    .when(cidadeService).atualizar(cidadeDTO.id(), cidadeDTOSemId);

            mockMvc.perform(put("/cidade/{idCidade}", cidadeDTO.id())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(cidadeDTOSemId)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message")
                            .value("Cidade não encontrada com id: " + cidadeDTO.id()));
        }

        @DisplayName("Deve lançar exceção ao tentar alterar Cidade por Cidade existente")
        @Test
        void deveGerarExcecao_QuandoAlterarCidade_PorCidadeExistente() throws Exception {
            doThrow(new RecursoJaSalvoException("Uma cidade com nome '" + cidadeDTO.nome() +
                    "' do estado '" + estadoDTO.nome() + "' já existe no banco de dados."))
                    .when(cidadeService).atualizar(cidadeDTO.id(), cidadeDTOSemId);

            mockMvc.perform(put("/cidade/{idCidade}", cidadeDTO.id())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(cidadeDTOSemId)))
                    .andExpect(status().isConflict())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message")
                            .value("Uma cidade com nome '" + cidadeDTO.nome() +
                                    "' do estado '" + estadoDTO.nome() + "' já existe no banco de dados."));
        }

        @DisplayName("Deve lançar exceção ao tentar salvar cidade com estado inexistente")
        @Test
        void deveGerarExcecao_QuandoAlterarCidade_ComEstadoInexistente() throws Exception {
            doThrow(new RecursoNaoEncontradoException("Estado não encontrado com id: " +
                    cidadeDTO.estadoId()))
                    .when(cidadeService).atualizar(cidadeDTO.id(), cidadeDTOSemId);

            mockMvc.perform(put("/cidade/{idCidade}", cidadeDTO.id())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(cidadeDTOSemId)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message")
                            .value("Estado não encontrado com id: " +
                                    cidadeDTO.estadoId()));
        }
    }

    @DisplayName("Deletar Cidade")
    @Nested
    class DeletarCidade {

        @DisplayName("Deve deletar Cidade")
        @Test
        void deveDeletarCidade() throws Exception {
            doNothing().when(cidadeService).deletarPorId(1L);

            mockMvc.perform(delete("/cidade/{idCidade}", 1L))
                    .andExpect(status().isNoContent());
        }

        @DisplayName("Deve lançar exceção ao tentar deletar Cidade por id inexistente")
        @Test
        void deveGerarExcecao_QuandoDeletarCidade_PorIdInexistente() throws Exception {
            doThrow(new RecursoNaoEncontradoException("Cidade não encontrada com id: " + cidadeDTO.id()))
                    .when(cidadeService).deletarPorId(cidadeDTO.id());

            mockMvc.perform(delete("/cidade/{idCidade}", cidadeDTO.id()))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message")
                            .value("Cidade não encontrada com id: " + cidadeDTO.id()));
        }
    }
}
