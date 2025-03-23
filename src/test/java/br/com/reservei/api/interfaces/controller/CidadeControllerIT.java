package br.com.reservei.api.interfaces.controller;

import br.com.reservei.api.application.dto.CidadeDTO;
import br.com.reservei.api.application.dto.EstadoDTO;
import br.com.reservei.api.application.usecases.endereco.CidadeServiceImpl;
import br.com.reservei.api.application.usecases.endereco.EstadoServiceImpl;
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

import static br.com.reservei.api.infrastructure.utils.CidadeHelper.*;
import static br.com.reservei.api.infrastructure.utils.EstadoHelper.gerarEstadoDto;
import static br.com.reservei.api.infrastructure.utils.EstadoHelper.gerarEstadoSemId;
import static br.com.reservei.api.infrastructure.utils.GeneralHelper.asJsonString;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = {"/clean.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class CidadeControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private CidadeServiceImpl cidadeService;

    @Autowired
    private EstadoServiceImpl estadoService;

    private static RequestSpecification requestSpec;

    private CidadeDTO cidadeDTO;
    private EstadoDTO estadoDTO;

    @BeforeEach
    void setUp() {
        this.estadoDTO = gerarEstadoDto(gerarEstadoSemId());
        estadoDTO = estadoService.salvar(estadoDTO);
        this.cidadeDTO = gerarCidadeDtoSemId(estadoDTO.id());

        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        requestSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .build();
    }

    @DisplayName("Buscar Cidade")
    @Nested
    class buscarCidade{

        @DisplayName("Deve buscar uma Cidade pelo ID fornecido")
        @Test
        void deveBuscarCidadePorId() {
            var cidadeSalvo = cidadeService.salvar(cidadeDTO);

            given()
                    .spec(requestSpec)
            .when()
                    .get("/cidade/{idCidade}", cidadeSalvo.id())
            .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("nome", equalTo(cidadeDTO.nome()))
                    .body("estadoId", is(cidadeDTO.estadoId().intValue()));
        }

        @DisplayName("Deve lançar exceção ao buscar Cidade com ID inexistente")
        @Test
        void deveGerarExcecao_QuandoBuscarCidade_PorIdInexistente() {
            var id = 1L;

            given()
                    .spec(requestSpec)
            .when()
                    .get("/cidade/{idCidade}", id)
            .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("message", equalTo("Cidade não encontrada com id: " + id));

        }

        @DisplayName("Deve retornar uma lista de cidades salvos")
        @Test
        void deveBuscarTodosOsCidades() {
            var cidades = List.of(cidadeService.salvar(cidadeDTO),
                    cidadeService.salvar(new CidadeDTO(null, "Camaçari", cidadeDTO.estadoId())));

            given()
                    .spec(requestSpec)
            .when()
                    .get("/cidade")
            .then()
                    .statusCode(HttpStatus.OK.value())
                    .body(equalTo(asJsonString(cidades)));
        }
    }

    @DisplayName("Salvar Cidade")
    @Nested
    class SalvarCidade {

        @DisplayName("Deve salvar Cidade")
        @Test
        void deveSalvarCidade() {

            given()
                    .spec(requestSpec)
                    .body(cidadeDTO)
            .when()
                    .post("/cidade")
            .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .body("id", is(notNullValue()))
                    .body("nome", equalTo(cidadeDTO.nome()))
                    .body("estadoId", is(cidadeDTO.estadoId().intValue()));
        }

        @DisplayName("Deve lançar exceção ao tentar salvar Cidade com estado já existente")
        @Test
        void deveGerarExcecao_QuandoSalvarCidade_ComEstadoInexistente() {
            cidadeDTO = new CidadeDTO(null, "Camaçari", 2L);

            given()
                    .spec(requestSpec)
                    .body(cidadeDTO)
            .when()
                    .post("/cidade")
            .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("message", equalTo("Estado não encontrado com id: " + cidadeDTO.estadoId()));

        }
    }

    @DisplayName("Alterar Cidade")
    @Nested
    class AlterarCidade {

        @DisplayName("Deve alterar Cidade cadastrada")
        @Test
        void deveAtualizarCidade() {
            var cidadeSalvo = cidadeService.salvar(cidadeDTO);

            var cidadeNovo = new CidadeDTO(cidadeSalvo.id(), "Camaçari", cidadeSalvo.estadoId());

            given()
                    .spec(requestSpec)
                    .body(cidadeNovo)
            .when()
                    .put("/cidade/{idCidade}", cidadeSalvo.id())
            .then()
                    .statusCode(HttpStatus.OK.value())
                    .body(equalTo(asJsonString(cidadeNovo)));
        }

        @DisplayName("Deve lançar exceção ao tentar alterar Cidade com id inexistente")
        @Test
        void deveGerarExcecao_QuandoAlterarCidade_PorIdInexistente() {
            var id = 1L;

            given()
                    .spec(requestSpec)
                    .body(cidadeDTO)
            .when()
                    .put("/cidade/{idCidade}", id)
            .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("message", equalTo("Cidade não encontrada com id: " + id));
        }

        @DisplayName("Deve lançar exceção ao tentar alterar Cidade por Cidade existente")
        @Test
        void deveGerarExcecao_QuandoAlterarCidade_PorCidadeExistente() {
            cidadeService.salvar(cidadeDTO);
            var cidadeOutra = cidadeService.salvar(
                    new CidadeDTO(null, "Paraíba", cidadeDTO.estadoId()));

            given()
                    .spec(requestSpec)
                    .body(cidadeDTO)
            .when()
                    .put("/cidade/{idCidade}", cidadeOutra.id())
            .then()
                    .statusCode(HttpStatus.CONFLICT.value())
                    .body("message", equalTo("Uma cidade com nome '" + cidadeDTO.nome() +
                            "' do estado '" + estadoDTO.nome() + "' já existe no banco de dados."));
        }
    }

    @DisplayName("Deletar Cidade")
    @Nested
    class DeletarCidade {

        @DisplayName("Deve deletar Cidade")
        @Test
        void deveDeletarCidade() {
            var cidadeSalvo = cidadeService.salvar(cidadeDTO);

            given()
                    .spec(requestSpec)
            .when()
                    .delete("/cidade/{idCidade}", cidadeSalvo.id())
            .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());
        }

        @DisplayName("Deve lançar exceção ao tentar deletar Cidade por id inexistente")
        @Test
        void deveGerarExcecao_QuandoDeletarCidade_PorIdInexistente() {
            var id = 1L;

            given()
                    .spec(requestSpec)
            .when()
                    .delete("/cidade/{idCidade}", id)
            .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("message", equalTo("Cidade não encontrada com id: " + id));
        }
    }
}
