package br.com.reservei.api.application.usecases.avaliacao;

import br.com.reservei.api.application.dto.AvaliacaoDTO;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static br.com.reservei.api.infrastructure.utils.CidadeHelper.gerarCidadeDtoSemId;
import static br.com.reservei.api.infrastructure.utils.EnderecoHelper.gerarEnderecoDtoSemId;
import static br.com.reservei.api.infrastructure.utils.EstadoHelper.gerarEstadoDto;
import static br.com.reservei.api.infrastructure.utils.EstadoHelper.gerarEstadoSemId;
import static br.com.reservei.api.infrastructure.utils.AvaliacaoHelper.gerarAvaliacaoDtoSemId;
import static br.com.reservei.api.infrastructure.utils.RestauranteHelper.gerarRestauranteDtoSemId;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = {"/clean.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Transactional
class AvaliacaoServiceIT {

    @Autowired
    private EstadoServiceImpl estadoService;

    @Autowired
    private CidadeServiceImpl cidadeService;

    @Autowired
    private EnderecoServiceImpl enderecoService;

    @Autowired
    private RestauranteServiceImpl restauranteService;

    @Autowired
    private AvaliacaoServiceImpl avaliacaoService;

    private AvaliacaoDTO avaliacaoDTO;
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
        this.avaliacaoDTO = gerarAvaliacaoDtoSemId(restauranteDTO.id());
    }

    @DisplayName("Buscar Avaliação")
    @Nested
    class BuscarAvaliacao {

        @DisplayName("Deve buscar um Avaliação pelo ID fornecido")
        @Test
        void deveBuscarAvaliacaoPorId() {
            avaliacaoDTO = avaliacaoService.salvar(avaliacaoDTO);

            var avaliacaoRecebido = avaliacaoService.buscarPorId(avaliacaoDTO.id());

            assertThat(avaliacaoRecebido)
                    .usingRecursiveComparison()
                    .isEqualTo(avaliacaoDTO);
        }

        @DisplayName("Deve lançar exceção ao buscar Avaliação com ID inexistente")
        @Test
        void deveGerarExcecao_QuandoBuscarAvaliacao_PorIdInexistente() {
            Long id = 1L;
            assertThatThrownBy(() -> avaliacaoService.buscarPorId(id))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Avaliação não encontrada com id: " + id);
        }

        @DisplayName("Deve retornar uma lista de avaliações salvos")
        @Test
        void deveBuscarTodasAsAvaliacao() {
            var avaliacaoDTO1 = avaliacaoService.salvar(avaliacaoDTO);
            var avaliacaoDTO2 = avaliacaoService.salvar(avaliacaoDTO);
            var avaliacaoDTO3 = avaliacaoService.salvar(avaliacaoDTO);

            var avaliacaosSalvas = List.of(avaliacaoDTO1, avaliacaoDTO2, avaliacaoDTO3);

            List<AvaliacaoDTO> avaliacaosRecebidos = avaliacaoService.buscarTodos();

            // Assert
            assertThat(avaliacaosRecebidos)
                    .isNotNull()
                    .isNotEmpty()
                    .hasSize(3)
                    .containsExactlyElementsOf(avaliacaosSalvas);
        }
    }

    @DisplayName("Salvar Avaliação")
    @Nested
    class SalvarAvaliacao {

        @DisplayName("Deve salvar Avaliação")
        @Test
        void deveSalvarAvaliacao() {
            var avaliacaoSalva = avaliacaoService.salvar(avaliacaoDTO);

            // Assert
            assertThat(avaliacaoSalva)
                    .isNotNull()
                    .isInstanceOf(AvaliacaoDTO.class)
                    .usingRecursiveComparison()
                    .ignoringFields("id", "dataCriacao")
                    .isEqualTo(avaliacaoDTO);

            assertThat(avaliacaoSalva.id())
                    .isNotNull();
            assertThat(avaliacaoSalva.dataCriacao())
                    .isCloseTo(LocalDateTime.now(), within(1000, ChronoUnit.MILLIS));

        }

        @DisplayName("Deve lançar exceção ao tentar salvar Avaliação com restaurante inexistente")
        @Test
        void deveGerarExcecao_QuandoSalvarAvaliacao_ComRestauranteInexistente() {
            avaliacaoDTO = new AvaliacaoDTO(null, 1, "Muito ruim", null, 2L);
            assertThatThrownBy(() -> avaliacaoService.salvar(avaliacaoDTO))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Restaurante não encontrado com id: " +
                            avaliacaoDTO.restauranteId());
        }
    }

    @DisplayName("Alterar Avaliação")
    @Nested
    class AlterarAvaliacao {

        @DisplayName("Deve alterar Avaliação cadastrado")
        @Test
        void deveAlterarAvaliacaoPorId() {
            avaliacaoDTO = avaliacaoService.salvar(avaliacaoDTO);
            var avaliacaoAtualizada = avaliacaoService.atualizar(avaliacaoDTO.id(),
                    new AvaliacaoDTO(null, 1, "Muito ruim", null, avaliacaoDTO.restauranteId()));

            assertThat(avaliacaoAtualizada)
                    .isNotNull()
                    .isInstanceOf(AvaliacaoDTO.class)
                    .isNotEqualTo(avaliacaoDTO);
            assertThat(avaliacaoAtualizada.id())
                    .isEqualTo(avaliacaoDTO.id());
        }

        @DisplayName("Deve lançar exceção ao tentar alterar Avaliação com id inexistente")
        @Test
        void deveGerarExcecao_QuandoAlterarAvaliacao_PorIdInexistente() {
            Long id = 1L;
            assertThatThrownBy(() -> avaliacaoService.atualizar(id, avaliacaoDTO))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Avaliação não encontrada com id: " + id);
        }

        @DisplayName("Deve lançar exceção ao tentar alterar Avaliação por cidade inexistente")
        @Test
        void deveGerarExcecao_QuandoAlterarAvaliacao_PorRestauranteInexistente() {
            Long id = 2L;
            avaliacaoDTO = avaliacaoService.salvar(avaliacaoDTO);
            avaliacaoDTO = new AvaliacaoDTO(avaliacaoDTO.id(), 1, "Muito ruim", null, 2L);

            assertThatThrownBy(() -> avaliacaoService.atualizar(avaliacaoDTO.id(), avaliacaoDTO))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Restaurante não encontrado com id: " +
                            id);
        }
    }

    @DisplayName("Deletar Avaliação")
    @Nested
    class DeletarAvaliacao {

        @DisplayName("Deve deletar Avaliação")
        @Test
        void deveDeletarAvaliacaoPorId() {
            avaliacaoDTO = avaliacaoService.salvar(avaliacaoDTO);
            avaliacaoService.deletarPorId(avaliacaoDTO.id());

            assertThatThrownBy(() -> avaliacaoService.buscarPorId(avaliacaoDTO.id()))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Avaliação não encontrada com id: " + avaliacaoDTO.id());
        }

        @DisplayName("Deve lançar exceção ao tentar deletar Avaliação por id inexistente")
        @Test
        void deveGerarExcecao_QuandoDeletarAvaliacao_PorIdInexistente() {
            Long id = 1L;
            assertThatThrownBy(() -> avaliacaoService.deletarPorId(id))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Avaliação não encontrada com id: " + id);
        }
    }
}

