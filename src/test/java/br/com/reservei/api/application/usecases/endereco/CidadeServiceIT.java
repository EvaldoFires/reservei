package br.com.reservei.api.application.usecases.endereco;

import br.com.reservei.api.application.dto.CidadeDTO;
import br.com.reservei.api.application.dto.EstadoDTO;
import br.com.reservei.api.domain.exceptions.RecursoJaSalvoException;
import br.com.reservei.api.domain.exceptions.RecursoNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static br.com.reservei.api.infrastructure.utils.CidadeHelper.*;
import static br.com.reservei.api.infrastructure.utils.EstadoHelper.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = {"/clean.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Transactional
class CidadeServiceIT {

    @Autowired
    private EstadoServiceImpl estadoService;

    @Autowired
    private CidadeServiceImpl cidadeService;

    private CidadeDTO cidadeDTO;
    private EstadoDTO estadoDTO;

    @BeforeEach
    void setUp() {
        this.estadoDTO = gerarEstadoDto(gerarEstadoSemId());
        estadoDTO = estadoService.salvar(estadoDTO);
        this.cidadeDTO = gerarCidadeDtoSemId(estadoDTO.id());
    }

    @DisplayName("Buscar Cidade")
    @Nested
    class BuscarCidade {

        @DisplayName("Deve buscar uma Cidade pelo ID fornecido")
        @Test
        void deveBuscarCidadePorId() {
            cidadeDTO = cidadeService.salvar(cidadeDTO);

            var cidadeRecebido = cidadeService.buscarPorId(cidadeDTO.id());

            assertThat(cidadeRecebido)
                    .usingRecursiveComparison()
                    .isEqualTo(cidadeDTO);
        }

        @DisplayName("Deve lançar exceção ao buscar cidade com ID inexistente")
        @Test
        void deveGerarExcecao_QuandoBuscarCidade_PorIdInexistente() {
            Long id = 1L;
            assertThatThrownBy(() -> cidadeService.buscarPorId(id))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Cidade não encontrada com id: " + id);
        }

        @DisplayName("Deve retornar uma lista de cidades salvas")
        @Test
        void deveBuscarTodasAsCidade() {
            var cidadeDTO1 = cidadeService.salvar(new CidadeDTO(null, "Camaçari", estadoDTO.id()));
            var cidadeDTO2 = cidadeService.salvar(new CidadeDTO(null, "Salvador", estadoDTO.id()));
            var cidadeDTO3 = cidadeService.salvar(new CidadeDTO(null, "Candeias", estadoDTO.id()));
            var cidadesSalvas = List.of(cidadeDTO1, cidadeDTO2, cidadeDTO3);

            List<CidadeDTO> cidadesRecebidos = cidadeService.buscarTodos();

            // Assert
            assertThat(cidadesRecebidos)
                    .isNotNull()
                    .isNotEmpty()
                    .hasSize(3)
                    .containsExactlyElementsOf(cidadesSalvas);
        }
    }

    @DisplayName("Salvar Cidade")
    @Nested
    class SalvarCidade {

        @DisplayName("Deve salvar Cidade")
        @Test
        void deveSalvarCidade() {
            var cidadeSalva = cidadeService.salvar(cidadeDTO);

            // Assert
            assertThat(cidadeSalva)
                    .isNotNull()
                    .isInstanceOf(CidadeDTO.class)
                    .usingRecursiveComparison()
                    .ignoringFields("id")
                    .isEqualTo(cidadeDTO);

            assertThat(cidadeSalva.id())
                    .isNotNull();
        }

        @DisplayName("Deve lançar exceção ao tentar salvar cidade com estado e nome já existentes")
        @Test
        void deveGerarExcecao_QuandoSalvarCidade_ComNomeEEstadoExistente() {
            cidadeService.salvar(cidadeDTO);

            assertThatThrownBy(() -> cidadeService.salvar(cidadeDTO))
                    .isInstanceOf(RecursoJaSalvoException.class)
                    .hasMessage("Uma cidade com nome '" + cidadeDTO.nome() +
                            "' do estado '" + estadoDTO.nome() + "' já existe no banco de dados.");
        }

        @DisplayName("Deve lançar exceção ao tentar salvar cidade com estado inexistente")
        @Test
        void deveGerarExcecao_QuandoSalvarCidade_ComEstadoInexistente() {
            cidadeDTO = new CidadeDTO(null, "Campinas", 2L);
            assertThatThrownBy(() -> cidadeService.salvar(cidadeDTO))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Estado não encontrado com id: " +
                            cidadeDTO.estadoId());
        }
    }

    @DisplayName("Alterar Cidade")
    @Nested
    class AlterarCidade {

        @DisplayName("Deve alterar Cidade cadastrada")
        @Test
        void deveAlterarCidadePorId() {
            cidadeDTO = cidadeService.salvar(cidadeDTO);
            var cidadeAtualizada = cidadeService.atualizar(cidadeDTO.id(),
                    new CidadeDTO(null, "Camaçari", cidadeDTO.estadoId()));

            assertThat(cidadeAtualizada)
                    .isNotNull()
                    .isInstanceOf(CidadeDTO.class)
                    .isNotEqualTo(cidadeDTO);
            assertThat(cidadeAtualizada.id())
                    .isEqualTo(cidadeDTO.id());
        }

        @DisplayName("Deve lançar exceção ao tentar alterar Cidade com id inexistente")
        @Test
        void deveGerarExcecao_QuandoAlterarCidade_PorIdInexistente() {
            Long id = 1L;
            assertThatThrownBy(() -> cidadeService.atualizar(id, cidadeDTO))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Cidade não encontrada com id: " + id);
        }

        @DisplayName("Deve lançar exceção ao tentar alterar Cidade por cidade existente")
        @Test
        void deveGerarExcecao_QuandoAlterarCidade_PorCidadeExistente() {
            cidadeDTO = cidadeService.salvar(cidadeDTO);
            var cidadeOutra = cidadeService.salvar(new CidadeDTO(null, "Lauro de Freitas", estadoDTO.id()));

            assertThatThrownBy(() -> cidadeService.atualizar(cidadeDTO.id(), cidadeOutra))
                    .isInstanceOf(RecursoJaSalvoException.class)
                    .hasMessage("Uma cidade com nome '" + cidadeOutra.nome() +
                            "' do estado '" + estadoDTO.nome() + "' já existe no banco de dados.");
        }

        @DisplayName("Deve lançar exceção ao tentar alterar Cidade por estado inexistente")
        @Test
        void deveGerarExcecao_QuandoAlterarCidade_PorEstadoInexistente() {
            Long id = 2L;
            cidadeDTO = cidadeService.salvar(cidadeDTO);
            cidadeDTO = new CidadeDTO(cidadeDTO.id(), "Lauro de Freitas", id);

            assertThatThrownBy(() -> cidadeService.atualizar(cidadeDTO.id(), cidadeDTO))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Estado não encontrado com id: " +
                            id);
        }
    }

    @DisplayName("Deletar Cidade")
    @Nested
    class DeletarCidade {

        @DisplayName("Deve deletar Cidade")
        @Test
        void deveDeletarCidadePorId() {
            cidadeDTO = cidadeService.salvar(cidadeDTO);
            cidadeService.deletarPorId(cidadeDTO.id());

            assertThatThrownBy(() -> cidadeService.buscarPorId(cidadeDTO.id()))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Cidade não encontrada com id: " + cidadeDTO.id());
        }

        @DisplayName("Deve lançar exceção ao tentar deletar Cidade por id inexistente")
        @Test
        void deveGerarExcecao_QuandoDeletarCidade_PorIdInexistente() {
            Long id = 1L;
            assertThatThrownBy(() -> cidadeService.deletarPorId(id))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Cidade não encontrada com id: " + id);
        }
    }
}

