package br.com.reservei.api.application.usecases.restaurante;

import br.com.reservei.api.application.dto.EnderecoDTO;
import br.com.reservei.api.application.dto.RestauranteDTO;
import br.com.reservei.api.application.usecases.endereco.CidadeServiceImpl;
import br.com.reservei.api.application.usecases.endereco.EnderecoServiceImpl;
import br.com.reservei.api.application.usecases.endereco.EstadoServiceImpl;
import br.com.reservei.api.domain.exceptions.RecursoNaoEncontradoException;
import br.com.reservei.api.infrastructure.utils.Cozinha;
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

import java.time.LocalTime;
import java.util.List;

import static br.com.reservei.api.infrastructure.utils.CidadeHelper.gerarCidadeDtoSemId;
import static br.com.reservei.api.infrastructure.utils.EnderecoHelper.gerarEnderecoDtoSemId;
import static br.com.reservei.api.infrastructure.utils.RestauranteHelper.gerarRestauranteDtoSemId;
import static br.com.reservei.api.infrastructure.utils.EstadoHelper.gerarEstadoDto;
import static br.com.reservei.api.infrastructure.utils.EstadoHelper.gerarEstadoSemId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = {"/clean.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Transactional
class RestauranteServiceIT {

    @Autowired
    private EstadoServiceImpl estadoService;

    @Autowired
    private CidadeServiceImpl cidadeService;

    @Autowired
    private EnderecoServiceImpl enderecoService;

    @Autowired
    private RestauranteServiceImpl restauranteService;

    private RestauranteDTO restauranteDTO;
    private EnderecoDTO enderecoDTO;

    @BeforeEach
    void setUp() {
        var estadoDTO = gerarEstadoDto(gerarEstadoSemId());
        estadoDTO = estadoService.salvar(estadoDTO);
        var cidadeDTO = gerarCidadeDtoSemId(estadoDTO.id());
        cidadeDTO = cidadeService.salvar(cidadeDTO);
        this.enderecoDTO = gerarEnderecoDtoSemId(cidadeDTO.id());
        this.enderecoDTO = enderecoService.salvar(enderecoDTO);
        this.restauranteDTO = gerarRestauranteDtoSemId(enderecoDTO.id());
    }

    @DisplayName("Buscar Restaurante")
    @Nested
    class BuscarRestaurante {

        @DisplayName("Deve buscar um Restaurante pelo ID fornecido")
        @Test
        void deveBuscarRestaurantePorId() {
            restauranteDTO = restauranteService.salvar(restauranteDTO);

            var restauranteRecebido = restauranteService.buscarPorId(restauranteDTO.id());

            assertThat(restauranteRecebido)
                    .usingRecursiveComparison()
                    .isEqualTo(restauranteDTO);
        }

        @DisplayName("Deve lançar exceção ao buscar Restaurante com ID inexistente")
        @Test
        void deveGerarExcecao_QuandoBuscarRestaurante_PorIdInexistente() {
            Long id = 1L;
            assertThatThrownBy(() -> restauranteService.buscarPorId(id))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Restaurante não encontrado com id: " + id);
        }

        @DisplayName("Deve buscar um Restaurante pelo Nome fornecido")
        @Test
        void deveBuscarRestaurantePorNome() {
            restauranteDTO = restauranteService.salvar(restauranteDTO);

            var restauranteRecebido = restauranteService.buscarPorNome(restauranteDTO.nome());

            assertThat(restauranteRecebido)
                    .usingRecursiveComparison()
                    .isEqualTo(restauranteDTO);
        }

        @DisplayName("Deve lançar exceção ao buscar Restaurante com Nome inexistente")
        @Test
        void deveGerarExcecao_QuandoBuscarRestaurante_PorNomeInexistente() {
            String nome = "Fiap Bistrô";
            assertThatThrownBy(() -> restauranteService.buscarPorNome(nome))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Restaurante não encontrado com nome: " + nome);
        }

        @DisplayName("Deve retornar uma lista de restaurantes salvos")
        @Test
        void deveBuscarTodasOsRestaurante() {
            var restauranteDTO1 = restauranteService.salvar(restauranteDTO);
            enderecoDTO = enderecoService.salvar( new EnderecoDTO(null, enderecoDTO.cidadeId(),
                    "Bairro2", "rua2", "2", "42600-000"));
            var restauranteDTO2 = restauranteService.salvar(gerarRestauranteDtoSemId(enderecoDTO.id()));
            enderecoDTO = enderecoService.salvar( new EnderecoDTO(null, enderecoDTO.cidadeId(),
                    "Bairro3", "rua3", "3", "42600-000"));
            var restauranteDTO3 = restauranteService.salvar(gerarRestauranteDtoSemId(enderecoDTO.id()));

            var restaurantesSalvas = List.of(restauranteDTO1, restauranteDTO2, restauranteDTO3);

            List<RestauranteDTO> restaurantesRecebidos = restauranteService.buscarTodos();

            // Assert
            assertThat(restaurantesRecebidos)
                    .isNotNull()
                    .isNotEmpty()
                    .hasSize(3)
                    .containsExactlyElementsOf(restaurantesSalvas);
        }

        @DisplayName("Deve retornar uma lista de restaurantes salvos com a cozinha dada")
        @Test
        void deveBuscarTodasOsRestaurantePorCozinha() {
            var restauranteDTO1 = restauranteService.salvar(restauranteDTO);
            enderecoDTO = enderecoService.salvar( new EnderecoDTO(null, enderecoDTO.cidadeId(),
                    "Bairro2", "rua2", "2", "42600-000"));
            var restauranteDTO2 = restauranteService.salvar(gerarRestauranteDtoSemId(enderecoDTO.id()));
            enderecoDTO = enderecoService.salvar( new EnderecoDTO(null, enderecoDTO.cidadeId(),
                    "Bairro3", "rua3", "3", "42600-000"));
            var restauranteDTO3 = restauranteService.salvar(gerarRestauranteDtoSemId(enderecoDTO.id()));

            var restaurantesSalvas = List.of(restauranteDTO1, restauranteDTO2, restauranteDTO3);

            List<RestauranteDTO> restaurantesRecebidos = restauranteService.buscarPorCozinha(restauranteDTO.cozinha());

            // Assert
            assertThat(restaurantesRecebidos)
                    .isNotNull()
                    .isNotEmpty()
                    .hasSize(3)
                    .containsExactlyElementsOf(restaurantesSalvas);
        }
    }

    @DisplayName("Salvar Restaurante")
    @Nested
    class SalvarRestaurante {

        @DisplayName("Deve salvar Restaurante")
        @Test
        void deveSalvarRestaurante() {
            var restauranteSalva = restauranteService.salvar(restauranteDTO);

            // Assert
            assertThat(restauranteSalva)
                    .isNotNull()
                    .isInstanceOf(RestauranteDTO.class)
                    .usingRecursiveComparison()
                    .ignoringFields("id")
                    .isEqualTo(restauranteDTO);

            assertThat(restauranteSalva.id())
                    .isNotNull();
        }

        @DisplayName("Deve lançar exceção ao tentar salvar Restaurante com cidade inexistente")
        @Test
        void deveGerarExcecao_QuandoSalvarRestaurante_ComEnderecoInexistente() {
            restauranteDTO = new RestauranteDTO(null, "Paris 6", Cozinha.FRANCESA,
                    2L, 10, LocalTime.NOON, LocalTime.MIDNIGHT);
            assertThatThrownBy(() -> restauranteService.salvar(restauranteDTO))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Endereço não encontrado com id: " +
                            restauranteDTO.enderecoId());
        }
    }

    @DisplayName("Alterar Restaurante")
    @Nested
    class AlterarRestaurante {

        @DisplayName("Deve alterar Restaurante cadastrado")
        @Test
        void deveAlterarRestaurantePorId() {
            restauranteDTO = restauranteService.salvar(restauranteDTO);
            var restauranteAtualizada = restauranteService.atualizar(restauranteDTO.id(),
                    new RestauranteDTO(null, "Paris 6", Cozinha.FRANCESA,
                            enderecoDTO.id(), 10, LocalTime.NOON, LocalTime.MIDNIGHT));

            assertThat(restauranteAtualizada)
                    .isNotNull()
                    .isInstanceOf(RestauranteDTO.class)
                    .isNotEqualTo(restauranteDTO);
            assertThat(restauranteAtualizada.id())
                    .isEqualTo(restauranteDTO.id());
        }

        @DisplayName("Deve lançar exceção ao tentar alterar Restaurante com id inexistente")
        @Test
        void deveGerarExcecao_QuandoAlterarRestaurante_PorIdInexistente() {
            Long id = 1L;
            assertThatThrownBy(() -> restauranteService.atualizar(id, restauranteDTO))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Restaurante não encontrado com id: " + id);
        }

        @DisplayName("Deve lançar exceção ao tentar alterar Restaurante por cidade inexistente")
        @Test
        void deveGerarExcecao_QuandoAlterarRestaurante_PorEnderecoInexistente() {
            Long id = 2L;
            restauranteDTO = restauranteService.salvar(restauranteDTO);
            restauranteDTO = new RestauranteDTO(restauranteDTO.id(), "Paris 6", Cozinha.FRANCESA,
                    id, 10, LocalTime.NOON, LocalTime.MIDNIGHT);

            assertThatThrownBy(() -> restauranteService.atualizar(restauranteDTO.id(), restauranteDTO))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Endereço não encontrado com id: " +
                            id);
        }
    }

    @DisplayName("Deletar Restaurante")
    @Nested
    class DeletarRestaurante {

        @DisplayName("Deve deletar Restaurante")
        @Test
        void deveDeletarRestaurantePorId() {
            restauranteDTO = restauranteService.salvar(restauranteDTO);
            restauranteService.deletarPorId(restauranteDTO.id());

            assertThatThrownBy(() -> restauranteService.buscarPorId(restauranteDTO.id()))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Restaurante não encontrado com id: " + restauranteDTO.id());
        }

        @DisplayName("Deve lançar exceção ao tentar deletar Restaurante por id inexistente")
        @Test
        void deveGerarExcecao_QuandoDeletarRestaurante_PorIdInexistente() {
            Long id = 1L;
            assertThatThrownBy(() -> restauranteService.deletarPorId(id))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessage("Restaurante não encontrado com id: " + id);
        }
    }
}

