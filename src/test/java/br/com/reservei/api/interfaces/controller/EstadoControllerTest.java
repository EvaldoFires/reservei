package br.com.reservei.api.interfaces.controller;

import br.com.reservei.api.application.dto.EstadoDTO;
import br.com.reservei.api.application.usecases.endereco.EstadoService;
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

import static br.com.reservei.api.infrastructure.utils.EstadoHelper.*;
import static br.com.reservei.api.infrastructure.utils.GeneralHelper.asJsonString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
 class EstadoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private EstadoService estadoService;

    private EstadoDTO estadoDTO;
    private EstadoDTO estadoDTOSemId;

        @BeforeEach
        void setUp() {
            estadoDTO = gerarEstadoDto(gerarEstado());
            estadoDTOSemId = gerarEstadoDto(gerarEstadoSemId());
            EstadoController estadoController = new EstadoController(estadoService);
            mockMvc = MockMvcBuilders.standaloneSetup(estadoController)
                    .setControllerAdvice(new GlobalExceptionHandler())
                    .addFilter((request, response, chain) -> {
                        response.setCharacterEncoding("UTF-8");
                        chain.doFilter(request, response);
                    }, "/*")
                    .build();
        }

        @DisplayName("Buscar Estado")
        @Nested
        class buscarEstado{

            @DisplayName("Deve buscar um Estado pelo ID fornecido")
            @Test
            void deveBuscarEstadoPorId() throws Exception {
                when(estadoService.buscarPorId(estadoDTO.id())).thenReturn(estadoDTO);

                mockMvc.perform(get("/estado/{idEstado}", estadoDTO.id()))
                        .andExpect(status().isOk())
                        .andExpect(content().json(asJsonString(estadoDTO)));
            }

            @DisplayName("Deve lançar exceção ao buscar Estado com ID inexistente")
            @Test
            void deveGerarExcecao_QuandoBuscarEstado_PorIdInexistente() throws Exception {
                doThrow(new RecursoNaoEncontradoException("Estado não encontrado com id: " + estadoDTO.id()))
                        .when(estadoService).buscarPorId(estadoDTO.id());

                mockMvc.perform(get("/estado/{idEstado}", estadoDTO.id()))
                        .andExpect(status().isNotFound())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.message")
                                .value("Estado não encontrado com id: " + estadoDTO.id()));

            }

            @DisplayName("Deve retornar uma lista de estados salvos")
            @Test
            void deveBuscarTodosOsEstados() throws Exception {
                var estados = List.of(estadoDTO,
                        new EstadoDTO(2L, "São Paulo", "SP"));

                when(estadoService.buscarTodos()).thenReturn(estados);

                mockMvc.perform(get("/estado"))
                        .andExpect(status().isOk())
                        .andExpect(content().json(asJsonString(estados)));
            }
        }

    @DisplayName("Salvar Estado")
    @Nested
    class SalvarEstado {

        @DisplayName("Deve salvar Estado")
        @Test
        void deveSalvarEstado() throws Exception {
            when(estadoService.salvar(estadoDTOSemId))
                    .thenReturn(estadoDTO);

            mockMvc.perform(post("/estado")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(estadoDTOSemId)))
                    .andExpect(status().isCreated())
                    .andExpect(content().json(asJsonString(estadoDTO)));

            verify(estadoService).salvar(estadoDTOSemId);
        }

        @DisplayName("Deve lançar exceção ao tentar salvar Estado com sigla ou nome já existente")
        @Test
        void deveGerarExcecao_QuandoSalvarEstado_ComNomeOuSiglaExistente() throws Exception {
            doThrow(new RecursoJaSalvoException("Um estado com sigla '" + estadoDTO.sigla() +
                    "' ou nome '" + estadoDTO.nome() + "' já existe no banco de dados."))
                    .when(estadoService).salvar(estadoDTOSemId);

            mockMvc.perform(post("/estado")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(estadoDTOSemId)))
                    .andExpect(status().isConflict())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message")
                            .value("Um estado com sigla '" + estadoDTO.sigla() +
                                    "' ou nome '" + estadoDTO.nome() + "' já existe no banco de dados."));
        }
    }

    @DisplayName("Alterar Estado")
    @Nested
    class AlterarEstado {

        @DisplayName("Deve alterar Estado cadastrado")
        @Test
        void deveAtualizarEstado() throws Exception {
            when(estadoService.atualizar(estadoDTO.id(), estadoDTOSemId)).thenReturn(estadoDTO);

            mockMvc.perform(put("/estado/{idEstado}", estadoDTO.id())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(estadoDTOSemId)))
                    .andExpect(status().isOk())
                    .andExpect(content().json(asJsonString(estadoDTO)));
        }

        @DisplayName("Deve lançar exceção ao tentar alterar Estado com id inexistente")
        @Test
        void deveGerarExcecao_QuandoAlterarEstado_PorIdInexistente() throws Exception {
            doThrow(new RecursoNaoEncontradoException("Estado não encontrado com id: " + estadoDTO.id()))
                    .when(estadoService).atualizar(estadoDTO.id(), estadoDTOSemId);

            mockMvc.perform(put("/estado/{idEstado}", estadoDTO.id())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(estadoDTOSemId)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message")
                            .value("Estado não encontrado com id: " + estadoDTO.id()));
        }

        @DisplayName("Deve lançar exceção ao tentar alterar Estado por Estado existente")
        @Test
        void deveGerarExcecao_QuandoAlterarEstado_PorEstadoExistente() throws Exception {
            doThrow(new RecursoJaSalvoException("Um estado com sigla '" + estadoDTO.sigla() +
                    "' ou nome '" + estadoDTO.nome() + "' já existe no banco de dados."))
                    .when(estadoService).atualizar(estadoDTO.id(), estadoDTOSemId);

            mockMvc.perform(put("/estado/{idEstado}", estadoDTO.id())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(estadoDTOSemId)))
                    .andExpect(status().isConflict())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message")
                            .value("Um estado com sigla '" + estadoDTO.sigla() +
                                    "' ou nome '" + estadoDTO.nome() + "' já existe no banco de dados."));
        }
    }

    @DisplayName("Deletar Estado")
    @Nested
    class DeletarEstado {

        @DisplayName("Deve deletar Estado")
        @Test
        void deveDeletarEstado() throws Exception {
            doNothing().when(estadoService).deletarPorId(1L);

            mockMvc.perform(delete("/estado/{idEstado}", 1L))
                    .andExpect(status().isNoContent());
        }

        @DisplayName("Deve lançar exceção ao tentar deletar Estado por id inexistente")
        @Test
        void deveGerarExcecao_QuandoDeletarEstado_PorIdInexistente() throws Exception {
            doThrow(new RecursoNaoEncontradoException("Estado não encontrado com id: " + estadoDTO.id()))
                    .when(estadoService).deletarPorId(estadoDTO.id());

            mockMvc.perform(delete("/estado/{idEstado}", estadoDTO.id()))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message")
                            .value("Estado não encontrado com id: " + estadoDTO.id()));
        }
    }
}
