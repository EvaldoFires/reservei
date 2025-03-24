package br.com.reservei.api.interfaces.controller;

import br.com.reservei.api.application.dto.CidadeDTO;
import br.com.reservei.api.application.dto.EnderecoDTO;
import br.com.reservei.api.application.usecases.endereco.EnderecoService;
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

import static br.com.reservei.api.infrastructure.utils.CidadeHelper.gerarCidade;
import static br.com.reservei.api.infrastructure.utils.CidadeHelper.gerarCidadeDto;
import static br.com.reservei.api.infrastructure.utils.EnderecoHelper.*;
import static br.com.reservei.api.infrastructure.utils.GeneralHelper.asJsonString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
 class EnderecoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private EnderecoService enderecoService;

    private CidadeDTO cidadeDTO;
    private EnderecoDTO enderecoDTO;
    private EnderecoDTO enderecoDTOSemId;

        @BeforeEach
        void setUp() {
            this.cidadeDTO = gerarCidadeDto(gerarCidade());
            enderecoDTO = gerarEnderecoDto(gerarEndereco());
            enderecoDTOSemId = gerarEnderecoDtoSemId(cidadeDTO.id());
            EnderecoController enderecoController = new EnderecoController(enderecoService);

            mockMvc = MockMvcBuilders.standaloneSetup(enderecoController)
                    .setControllerAdvice(new GlobalExceptionHandler())
                    .addFilter((request, response, chain) -> {
                        response.setCharacterEncoding("UTF-8");
                        chain.doFilter(request, response);
                    }, "/*")
                    .build();
        }

        @DisplayName("Buscar Endereço")
        @Nested
        class buscarEndereco{

            @DisplayName("Deve buscar um Endereço pelo ID fornecido")
            @Test
            void deveBuscarEnderecoPorId() throws Exception {
                when(enderecoService.buscarPorId(enderecoDTO.id())).thenReturn(enderecoDTO);

                mockMvc.perform(get("/endereco/{idEndereco}", enderecoDTO.id()))
                        .andExpect(status().isOk())
                        .andExpect(content().json(asJsonString(enderecoDTO)));
            }

            @DisplayName("Deve lançar exceção ao buscar Endereço com ID inexistente")
            @Test
            void deveGerarExcecao_QuandoBuscarEndereco_PorIdInexistente() throws Exception {
                doThrow(new RecursoNaoEncontradoException("Endereço não encontrado com id: " + enderecoDTO.id()))
                        .when(enderecoService).buscarPorId(enderecoDTO.id());

                mockMvc.perform(get("/endereco/{idEndereco}", enderecoDTO.id()))
                        .andExpect(status().isNotFound())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.message")
                                .value("Endereço não encontrado com id: " + enderecoDTO.id()));

            }

            @DisplayName("Deve retornar uma lista de enderecos salvos")
            @Test
            void deveBuscarTodosOsEnderecos() throws Exception {
                var enderecos = List.of(enderecoDTO,
                        new EnderecoDTO(2L, cidadeDTO.id(), "bairro2", "rua2", "2", "42600-000"));

                when(enderecoService.buscarTodos()).thenReturn(enderecos);

                mockMvc.perform(get("/endereco"))
                        .andExpect(status().isOk())
                        .andExpect(content().json(asJsonString(enderecos)));
            }
        }

    @DisplayName("Salvar Endereço")
    @Nested
    class SalvarEndereco {

        @DisplayName("Deve salvar Endereço")
        @Test
        void deveSalvarEndereco() throws Exception {
            when(enderecoService.salvar(enderecoDTOSemId))
                    .thenReturn(enderecoDTO);

            mockMvc.perform(post("/endereco")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(enderecoDTOSemId)))
                    .andExpect(status().isCreated())
                    .andExpect(content().json(asJsonString(enderecoDTO)));

            verify(enderecoService).salvar(enderecoDTOSemId);
        }

        @DisplayName("Deve lançar exceção ao tentar salvar Endereço com cidade inexistente")
        @Test
        void deveGerarExcecao_QuandoSalvarEndereco_ComCidadeInexistente() throws Exception {
            doThrow(new RecursoNaoEncontradoException("Cidade não encontrada com id: " +
                            enderecoDTO.cidadeId()))
                    .when(enderecoService).salvar(enderecoDTOSemId);

            mockMvc.perform(post("/endereco")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(enderecoDTOSemId)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message")
                            .value("Cidade não encontrada com id: " +
                                    enderecoDTO.cidadeId()));
        }
    }

    @DisplayName("Alterar Endereço")
    @Nested
    class AlterarEndereco {

        @DisplayName("Deve alterar Endereço cadastrada")
        @Test
        void deveAtualizarEndereco() throws Exception {
            when(enderecoService.atualizar(enderecoDTO.id(), enderecoDTOSemId)).thenReturn(enderecoDTO);

            mockMvc.perform(put("/endereco/{idEndereco}", enderecoDTO.id())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(enderecoDTOSemId)))
                    .andExpect(status().isOk())
                    .andExpect(content().json(asJsonString(enderecoDTO)));
        }

        @DisplayName("Deve lançar exceção ao tentar alterar Endereco com id inexistente")
        @Test
        void deveGerarExcecao_QuandoAlterarEndereco_PorIdInexistente() throws Exception {
            doThrow(new RecursoNaoEncontradoException("Endereço não encontrado com id: " + enderecoDTO.id()))
                    .when(enderecoService).atualizar(enderecoDTO.id(), enderecoDTOSemId);

            mockMvc.perform(put("/endereco/{idEndereco}", enderecoDTO.id())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(enderecoDTOSemId)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message")
                            .value("Endereço não encontrado com id: " + enderecoDTO.id()));
        }

        @DisplayName("Deve lançar exceção ao tentar salvar Endereço com cidade inexistente")
        @Test
        void deveGerarExcecao_QuandoAlterarEndereco_ComEstadoInexistente() throws Exception {
            doThrow(new RecursoNaoEncontradoException("Cidade não encontrada com id: " +
                    enderecoDTO.cidadeId()))
                    .when(enderecoService).atualizar(enderecoDTO.id(), enderecoDTOSemId);

            mockMvc.perform(put("/endereco/{idEndereco}", enderecoDTO.id())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(enderecoDTOSemId)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message")
                            .value("Cidade não encontrada com id: " +
                                    enderecoDTO.cidadeId()));
        }
    }

    @DisplayName("Deletar Endereço")
    @Nested
    class DeletarEndereco {

        @DisplayName("Deve deletar Endereço")
        @Test
        void deveDeletarEndereco() throws Exception {
            doNothing().when(enderecoService).deletarPorId(1L);

            mockMvc.perform(delete("/endereco/{idEndereco}", 1L))
                    .andExpect(status().isNoContent());
        }

        @DisplayName("Deve lançar exceção ao tentar deletar Endereço por id inexistente")
        @Test
        void deveGerarExcecao_QuandoDeletarEndereco_PorIdInexistente() throws Exception {
            doThrow(new RecursoNaoEncontradoException("Endereço não encontrado com id: " + enderecoDTO.id()))
                    .when(enderecoService).deletarPorId(enderecoDTO.id());

            mockMvc.perform(delete("/endereco/{idEndereco}", enderecoDTO.id()))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message")
                            .value("Endereço não encontrado com id: " + enderecoDTO.id()));
        }
    }
}
