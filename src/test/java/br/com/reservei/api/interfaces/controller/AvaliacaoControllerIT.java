package br.com.reservei.api.interfaces.controller;

import br.com.reservei.api.application.dto.AvaliacaoDTO;
import br.com.reservei.api.application.dto.RestauranteDTO;
import br.com.reservei.api.application.usecases.endereco.CidadeServiceImpl;
import br.com.reservei.api.application.usecases.endereco.EnderecoServiceImpl;
import br.com.reservei.api.application.usecases.endereco.EstadoServiceImpl;
import br.com.reservei.api.application.usecases.avaliacao.AvaliacaoServiceImpl;
import br.com.reservei.api.application.usecases.restaurante.RestauranteServiceImpl;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static br.com.reservei.api.infrastructure.utils.CidadeHelper.gerarCidadeDtoSemId;
import static br.com.reservei.api.infrastructure.utils.EnderecoHelper.gerarEnderecoDtoSemId;
import static br.com.reservei.api.infrastructure.utils.EstadoHelper.gerarEstadoDto;
import static br.com.reservei.api.infrastructure.utils.EstadoHelper.gerarEstadoSemId;
import static br.com.reservei.api.infrastructure.utils.GeneralHelper.asJsonString;
import static br.com.reservei.api.infrastructure.utils.AvaliacaoHelper.gerarAvaliacaoDtoSemId;
import static br.com.reservei.api.infrastructure.utils.RestauranteHelper.gerarRestauranteDtoSemId;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = {"/clean.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AvaliacaoControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private AvaliacaoServiceImpl avaliacaoService;
    @Autowired
    private CidadeServiceImpl cidadeService;
    @Autowired
    private EstadoServiceImpl estadoService;
    @Autowired
    private EnderecoServiceImpl enderecoService;
    @Autowired
    private RestauranteServiceImpl restauranteService;

    private static RequestSpecification requestSpec;

    private AvaliacaoDTO avaliacaoDTO;
    private RestauranteDTO restauranteDTO;

    @BeforeEach
    void setUp() {
        var estadoDTO = estadoService.salvar(gerarEstadoDto(gerarEstadoSemId()));
        var cidadeDTO = cidadeService.salvar(gerarCidadeDtoSemId(estadoDTO.id()));
        var enderecoDTO = enderecoService.salvar(gerarEnderecoDtoSemId(cidadeDTO.id()));
        this.restauranteDTO = restauranteService.salvar(gerarRestauranteDtoSemId(enderecoDTO.id()));

        this.avaliacaoDTO = gerarAvaliacaoDtoSemId(restauranteDTO.id());

        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        requestSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .build();
    }

    @DisplayName("Buscar Avaliacao")
    @Nested
    class buscarAvaliacao{

        @DisplayName("Deve buscar um Avaliacao pelo ID fornecido")
        @Test
        void deveBuscarAvaliacaoPorId() {
            var avaliacaoSalvo = avaliacaoService.salvar(avaliacaoDTO);

            given()
                    .spec(requestSpec)
            .when()
                    .get("/avaliacao/{idAvaliacao}", avaliacaoSalvo.id())
            .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("nota", equalTo(avaliacaoDTO.nota()))
                    .body("comentario", equalTo(avaliacaoDTO.comentario()))
                    .body("restauranteId", is(avaliacaoDTO.restauranteId().intValue()));
        }

        @DisplayName("Deve lançar exceção ao buscar Avaliação com ID inexistente")
        @Test
        void deveGerarExcecao_QuandoBuscarAvaliacao_PorIdInexistente() {
            var id = 1L;

            given()
                    .spec(requestSpec)
            .when()
                    .get("/avaliacao/{idAvaliacao}", id)
            .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("message", equalTo("Avaliação não encontrada com id: " + id));

        }

        @DisplayName("Deve retornar uma lista de avaliações salvos")
        @Test
        void deveBuscarTodosOsAvaliacaos() {
            var avaliacaos = List.of(avaliacaoService.salvar(gerarAvaliacaoDtoSemId(avaliacaoDTO.restauranteId())),
                    avaliacaoService.salvar(new AvaliacaoDTO(null, 5, "otimo",
                            null, avaliacaoDTO.restauranteId())));

            given()
                    .spec(requestSpec)
            .when()
                    .get("/avaliacao")
            .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("$.size()", is(avaliacaos.size()))
                    .body("[0].id", is(avaliacaos.get(0).id().intValue()))
                    .body("[0].nota", equalTo(avaliacaos.get(0).nota()))
                    .body("[0].comentario", equalTo(avaliacaos.get(0).comentario()))
                    .body("[0].restauranteId", is(avaliacaos.get(0).restauranteId().intValue()))
                    .body("[1].id", is(avaliacaos.get(1).id().intValue()))
                    .body("[1].nota", equalTo(avaliacaos.get(1).nota()))
                    .body("[1].comentario", equalTo(avaliacaos.get(1).comentario()))
                    .body("[1].restauranteId", is(avaliacaos.get(1).restauranteId().intValue()));
        }
    }

    @DisplayName("Salvar Avaliacao")
    @Nested
    class SalvarAvaliacao {

        @DisplayName("Deve salvar Avaliação")
        @Test
        void deveSalvarAvaliacao() {

            given()
                    .spec(requestSpec)
                    .body(avaliacaoDTO)
            .when()
                    .post("/avaliacao")
            .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .body("id", is(notNullValue()))
                    .body("nota", equalTo(avaliacaoDTO.nota()))
                    .body("comentario", equalTo(avaliacaoDTO.comentario()))
                    .body("restauranteId", is(avaliacaoDTO.restauranteId().intValue()));
        }

        @DisplayName("Deve lançar exceção ao tentar salvar Avaliação com restaurante inexistente")
        @Test
        void deveGerarExcecao_QuandoSalvarAvaliacao_ComRestauranteInexistente() {
            avaliacaoDTO = new AvaliacaoDTO(null, 2, "Zoado", null,
                    2L);

            given()
                    .spec(requestSpec)
                    .body(avaliacaoDTO)
            .when()
                    .post("/avaliacao")
            .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("message", equalTo("Restaurante não encontrado com id: " +
                            avaliacaoDTO.restauranteId()));
        }
    }

    @DisplayName("Alterar Avaliação")
    @Nested
    class AlterarAvaliacao {

        @DisplayName("Deve alterar Avaliação cadastrada")
        @Test
        void deveAtualizarAvaliacao() {
            var avaliacaoSalvo = avaliacaoService.salvar(avaliacaoDTO);

            var avaliacaoNovo = new AvaliacaoDTO(avaliacaoSalvo.id(), 2, "Zoado", null,
                    avaliacaoSalvo.restauranteId());

            given()
                    .spec(requestSpec)
                    .body(avaliacaoNovo)
            .when()
                    .put("/avaliacao/{idAvaliacao}", avaliacaoSalvo.id())
            .then()
                    .statusCode(HttpStatus.OK.value())
                    .body(equalTo(asJsonString(avaliacaoNovo)));
        }

        @DisplayName("Deve lançar exceção ao tentar alterar Avaliação com id inexistente")
        @Test
        void deveGerarExcecao_QuandoAlterarAvaliacao_PorIdInexistente() {
            var id = 1L;

            given()
                    .spec(requestSpec)
                    .body(avaliacaoDTO)
            .when()
                    .put("/avaliacao/{idAvaliacao}", id)
            .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("message", equalTo("Avaliação não encontrada com id: " + id));
        }

        @DisplayName("Deve lançar exceção ao tentar alterar Avaliação com cidade inexistente")
        @Test
        void deveGerarExcecao_QuandoAlterarAvaliacao_ComRestauranteInexistente() {
            var avaliacaoSalvo = avaliacaoService.salvar(avaliacaoDTO);

            var avaliacaoNovo = new AvaliacaoDTO(avaliacaoSalvo.id(), 2, "Zoado", null,
                    2L);

            given()
                    .spec(requestSpec)
                    .body(avaliacaoNovo)
            .when()
                    .put("/avaliacao/{idEndereco}", avaliacaoSalvo.id())
            .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("message", equalTo("Restaurante não encontrado com id: "
                            + avaliacaoNovo.restauranteId()));
        }
    }

    @DisplayName("Deletar Avaliação")
    @Nested
    class DeletarAvaliacao {

        @DisplayName("Deve deletar Avaliação")
        @Test
        void deveDeletarAvaliacao() {
            var avaliacaoSalvo = avaliacaoService.salvar(avaliacaoDTO);

            given()
                    .spec(requestSpec)
            .when()
                    .delete("/avaliacao/{idAvaliacao}", avaliacaoSalvo.id())
            .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());
        }

        @DisplayName("Deve lançar exceção ao tentar deletar Avaliação por id inexistente")
        @Test
        void deveGerarExcecao_QuandoDeletarAvaliacao_PorIdInexistente() {
            var id = 1L;

            given()
                    .spec(requestSpec)
            .when()
                    .delete("/avaliacao/{idAvaliacao}", id)
            .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("message", equalTo("Avaliação não encontrada com id: " + id));
        }
    }
}
