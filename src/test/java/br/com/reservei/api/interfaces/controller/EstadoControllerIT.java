package br.com.reservei.api.interfaces.controller;

import br.com.reservei.api.application.dto.EstadoDTO;
import br.com.reservei.api.application.usecases.endereco.EstadoServiceImpl;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static br.com.reservei.api.infrastructure.utils.EstadoHelper.*;
import static br.com.reservei.api.infrastructure.utils.GeneralHelper.asJsonString;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = {"/clean.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class EstadoControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private EstadoServiceImpl estadoService;

    private static RequestSpecification requestSpec;

    private EstadoDTO estadoDTO;

    @BeforeEach
    void setUp() {
        estadoDTO = gerarEstadoDto(gerarEstadoSemId());

        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        requestSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .build();
    }

    @DisplayName("Buscar Estado")
        @Nested
        class buscarEstado{

            @DisplayName("Deve buscar um Estado pelo ID fornecido")
            @Test
            void deveBuscarEstadoPorId() {
                var estadoSalvo = estadoService.salvar(estadoDTO);

                given()
                        .spec(requestSpec)
                .when()
                        .get("/estado/{idEstado}", estadoSalvo.id())
                .then()
                        .statusCode(HttpStatus.OK.value())
                        .body("nome", equalTo(estadoDTO.nome()))
                        .body("sigla", equalTo(estadoDTO.sigla()));
            }

            @DisplayName("Deve lançar exceção ao buscar Estado com ID inexistente")
            @Test
            void deveGerarExcecao_QuandoBuscarEstado_PorIdInexistente() {
                var id = 1L;

                given()
                        .spec(requestSpec)
                .when()
                        .get("/estado/{idEstado}", id)
                .then()
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .body("message", equalTo("Estado não encontrado com id: " + id));

            }

            @DisplayName("Deve retornar uma lista de estados salvos")
            @Test
            void deveBuscarTodosOsEstados() {
                var estados = List.of(estadoService.salvar(estadoDTO),
                        estadoService.salvar(new EstadoDTO(null, "São Paulo", "SP")));

                given()
                        .spec(requestSpec)
                .when()
                        .get("/estado")
                .then()
                        .statusCode(HttpStatus.OK.value())
                        .body(equalTo(asJsonString(estados)));
            }
        }

    @DisplayName("Salvar Estado")
    @Nested
    class SalvarEstado {

        @DisplayName("Deve salvar Estado")
        @Test
        void deveSalvarEstado() {

            given()
                    .spec(requestSpec)
                    .body(estadoDTO)
            .when()
                    .post("/estado")
            .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .body("id", is(notNullValue()))
                    .body("nome", equalTo(estadoDTO.nome()))
                    .body("sigla", equalTo(estadoDTO.sigla()));
        }

        @DisplayName("Deve lançar exceção ao tentar salvar Estado com sigla ou nome já existente")
        @Test
        void deveGerarExcecao_QuandoSalvarEstado_ComNomeOuSiglaExistente() {
            var estadoSalvo = estadoService.salvar(estadoDTO);

            given()
                    .spec(requestSpec)
                    .body(estadoDTO)
            .when()
                    .post("/estado")
            .then()
                    .statusCode(HttpStatus.CONFLICT.value())
                    .body("message", equalTo("Um estado com sigla '" + estadoSalvo.sigla() +
                            "' ou nome '" + estadoSalvo.nome() + "' já existe no banco de dados."));

        }
    }

    @DisplayName("Alterar Estado")
    @Nested
    class AlterarEstado {

        @DisplayName("Deve alterar Estado cadastrado")
        @Test
        void deveAtualizarEstado() {
            var estadoSalvo = estadoService.salvar(estadoDTO);

            var estadoNovo = new EstadoDTO(estadoSalvo.id(), "Paraíba", "PB");

            given()
                    .spec(requestSpec)
                    .body(estadoNovo)
            .when()
                    .put("/estado/{idEstado}", estadoSalvo.id())
            .then()
                    .statusCode(HttpStatus.OK.value())
                    .body(equalTo(asJsonString(estadoNovo)));
        }

        @DisplayName("Deve lançar exceção ao tentar alterar Estado com id inexistente")
        @Test
        void deveGerarExcecao_QuandoAlterarEstado_PorIdInexistente() {
            var id = 1L;

            given()
                    .spec(requestSpec)
                    .body(estadoDTO)
            .when()
                    .put("/estado/{idEstado}", id)
            .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("message", equalTo("Estado não encontrado com id: " + id));
        }

        @DisplayName("Deve lançar exceção ao tentar alterar Estado por Estado existente")
        @Test
        void deveGerarExcecao_QuandoAlterarEstado_PorEstadoExistente() {
            var estadoSalvo = estadoService.salvar(estadoDTO);
            var estadoSalvo2 = estadoService.salvar(new EstadoDTO(null, "Paraíba", "PB"));

            given()
                    .spec(requestSpec)
                    .body(estadoDTO)
            .when()
                    .put("/estado/{idEstado}", estadoSalvo2.id())
            .then()
                    .statusCode(HttpStatus.CONFLICT.value())
                    .body("message", equalTo("Um estado com sigla '" + estadoSalvo.sigla() +
                            "' ou nome '" + estadoSalvo.nome() + "' já existe no banco de dados."));
        }
    }

    @DisplayName("Deletar Estado")
    @Nested
    class DeletarEstado {

        @DisplayName("Deve deletar Estado")
        @Test
        void deveDeletarEstado() {
            var estadoSalvo = estadoService.salvar(estadoDTO);

            given()
                    .spec(requestSpec)
            .when()
                    .delete("/estado/{idEstado}", estadoSalvo.id())
            .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());
        }

        @DisplayName("Deve lançar exceção ao tentar deletar Estado por id inexistente")
        @Test
        void deveGerarExcecao_QuandoDeletarEstado_PorIdInexistente() {
            var id = 1L;

            given()
                    .spec(requestSpec)
            .when()
                    .delete("/estado/{idEstado}", id)
            .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("message", equalTo("Estado não encontrado com id: " + id));
        }
    }
}
