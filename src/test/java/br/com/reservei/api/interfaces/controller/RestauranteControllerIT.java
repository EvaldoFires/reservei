package br.com.reservei.api.interfaces.controller;

import br.com.reservei.api.application.dto.EnderecoDTO;
import br.com.reservei.api.application.dto.RestauranteDTO;
import br.com.reservei.api.application.usecases.endereco.CidadeServiceImpl;
import br.com.reservei.api.application.usecases.endereco.EnderecoServiceImpl;
import br.com.reservei.api.application.usecases.restaurante.RestauranteServiceImpl;
import br.com.reservei.api.application.usecases.endereco.EstadoServiceImpl;
import br.com.reservei.api.infrastructure.utils.Cozinha;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import jakarta.persistence.EntityManager;
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

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static br.com.reservei.api.infrastructure.utils.CidadeHelper.gerarCidadeDtoSemId;
import static br.com.reservei.api.infrastructure.utils.EnderecoHelper.gerarEnderecoDtoSemId;
import static br.com.reservei.api.infrastructure.utils.RestauranteHelper.gerarRestauranteDtoSemId;
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
class RestauranteControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private RestauranteServiceImpl restauranteService;
    @Autowired
    private CidadeServiceImpl cidadeService;
    @Autowired
    private EstadoServiceImpl estadoService;
    @Autowired
    private EnderecoServiceImpl enderecoService;

    @Autowired
    private EntityManager entityManager;

    private static RequestSpecification requestSpec;

    private RestauranteDTO restauranteDTO;
    private EnderecoDTO enderecoDTO;

    @BeforeEach
    void setUp() {
        var estadoDTO = estadoService.salvar(gerarEstadoDto(gerarEstadoSemId()));
        var cidadeDTO = cidadeService.salvar(gerarCidadeDtoSemId(estadoDTO.id()));
        this.enderecoDTO = enderecoService.salvar(gerarEnderecoDtoSemId(cidadeDTO.id()));

        this.restauranteDTO = gerarRestauranteDtoSemId(enderecoDTO.id());

        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        requestSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .build();
    }

    @DisplayName("Buscar Restaurante")
    @Nested
    class buscarRestaurante{

        @DisplayName("Deve buscar um Restaurante pelo ID fornecido")
        @Test
        void deveBuscarRestaurantePorId() {
            var restauranteSalvo = restauranteService.salvar(restauranteDTO);

            given()
                    .spec(requestSpec)
            .when()
                    .get("/restaurante/{idRestaurante}", restauranteSalvo.id())
            .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("nome", equalTo(restauranteDTO.nome()))
                    .body("cozinha", is(restauranteDTO.cozinha().toString()))
                    .body("reservasPorHora", equalTo(restauranteDTO.reservasPorHora()))
                    .body("inicioExpediente", equalTo(restauranteDTO.inicioExpediente()
                            .format(DateTimeFormatter.ofPattern("HH:mm:ss"))))
                    .body("finalExpediente", equalTo(restauranteDTO.finalExpediente()
                            .format(DateTimeFormatter.ofPattern("HH:mm:ss"))))
                    .body("enderecoId", is(restauranteDTO.enderecoId().intValue()));
        }

        @DisplayName("Deve lançar exceção ao buscar Restaurante com ID inexistente")
        @Test
        void deveGerarExcecao_QuandoBuscarRestaurante_PorIdInexistente() {
            var id = 1L;

            given()
                    .spec(requestSpec)
            .when()
                    .get("/restaurante/{idRestaurante}", id)
            .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("message", equalTo("Restaurante não encontrado com id: " + id));

        }

        @DisplayName("Deve retornar uma lista de restaurantes salvos")
        @Test
        void deveBuscarTodosOsRestaurantes() {
            var restauranteDTO1 = restauranteService.salvar(restauranteDTO);
            enderecoDTO = enderecoService.salvar( new EnderecoDTO(null, enderecoDTO.cidadeId(),
                    "Bairro2", "rua2", "2", "42600-000"));
            var restauranteDTO2 = restauranteService.salvar(gerarRestauranteDtoSemId(enderecoDTO.id()));

            var restaurantes = List.of(restauranteDTO1,restauranteDTO2);

            given()
                    .spec(requestSpec)
            .when()
                    .get("/restaurante")
            .then()
                    .statusCode(HttpStatus.OK.value())
                    .body(equalTo(asJsonString(restaurantes)));
        }
    }

    @DisplayName("Salvar Restaurante")
    @Nested
    class SalvarRestaurante {

        @DisplayName("Deve salvar Restaurante")
        @Test
        void deveSalvarRestaurante() {

            given()
                    .spec(requestSpec)
                    .body(restauranteDTO)
            .when()
                    .post("/restaurante")
            .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .body("id", is(notNullValue()))
                    .body("nome", equalTo(restauranteDTO.nome()))
                    .body("cozinha", is(restauranteDTO.cozinha().toString()))
                    .body("reservasPorHora", equalTo(restauranteDTO.reservasPorHora()))
                    .body("inicioExpediente", equalTo(restauranteDTO.inicioExpediente()
                            .format(DateTimeFormatter.ofPattern("HH:mm:ss"))))
                    .body("finalExpediente", equalTo(restauranteDTO.finalExpediente()
                            .format(DateTimeFormatter.ofPattern("HH:mm:ss"))))
                    .body("enderecoId", is(restauranteDTO.enderecoId().intValue()));
        }

        @DisplayName("Deve lançar exceção ao tentar salvar Restaurante com cidade inexistente")
        @Test
        void deveGerarExcecao_QuandoSalvarRestaurante_ComEnderecoInexistente() {
            restauranteDTO = new RestauranteDTO(null, "Paris 6", Cozinha.FRANCESA,
                    2L, 10, LocalTime.NOON, LocalTime.MIDNIGHT);

            given()
                    .spec(requestSpec)
                    .body(restauranteDTO)
            .when()
                    .post("/restaurante")
            .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("message", equalTo("Endereço não encontrado com id: " + restauranteDTO.enderecoId()));
        }
    }

    @DisplayName("Alterar Restaurante")
    @Nested
    class AlterarRestaurante {

        @DisplayName("Deve alterar Restaurante cadastrado")
        @Test
        void deveAtualizarRestaurante() {
            var restauranteSalvo = restauranteService.salvar(restauranteDTO);

            var restauranteNovo = new RestauranteDTO(restauranteSalvo.id(), "Paris 6", Cozinha.FRANCESA,
                    enderecoDTO.id(), 10, LocalTime.NOON, LocalTime.MIDNIGHT);

            given()
                    .spec(requestSpec)
                    .body(restauranteNovo)
            .when()
                    .put("/restaurante/{idRestaurante}", restauranteSalvo.id())
            .then()
                    .statusCode(HttpStatus.OK.value())
                    .body(equalTo(asJsonString(restauranteNovo)));
        }

        @DisplayName("Deve lançar exceção ao tentar alterar Restaurante com id inexistente")
        @Test
        void deveGerarExcecao_QuandoAlterarRestaurante_PorIdInexistente() {
            var id = 1L;

            given()
                    .spec(requestSpec)
                    .body(restauranteDTO)
            .when()
                    .put("/restaurante/{idRestaurante}", id)
            .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("message", equalTo("Restaurante não encontrado com id: " + id));
        }

        @DisplayName("Deve lançar exceção ao tentar alterar Restaurante com cidade inexistente")
        @Test
        void deveGerarExcecao_QuandoAlterarRestaurante_ComEnderecoInexistente() {
            var restauranteSalvo = restauranteService.salvar(restauranteDTO);

            var restauranteNovo = new RestauranteDTO(restauranteSalvo.id(), "Paris 6", Cozinha.FRANCESA,
                    2L, 10, LocalTime.NOON, LocalTime.MIDNIGHT);

            given()
                    .spec(requestSpec)
                    .body(restauranteNovo)
            .when()
                    .put("/restaurante/{idEndereco}", restauranteSalvo.id())
            .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("message", equalTo("Endereço não encontrado com id: " + restauranteNovo.enderecoId()));
        }
    }

    @DisplayName("Deletar Restaurante")
    @Nested
    class DeletarRestaurante {

        @DisplayName("Deve deletar Restaurante")
        @Test
        void deveDeletarRestaurante() {
            var restauranteSalvo = restauranteService.salvar(restauranteDTO);

            given()
                    .spec(requestSpec)
            .when()
                    .delete("/restaurante/{idRestaurante}", restauranteSalvo.id())
            .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());
        }

        @DisplayName("Deve lançar exceção ao tentar deletar Restaurante por id inexistente")
        @Test
        void deveGerarExcecao_QuandoDeletarRestaurante_PorIdInexistente() {
            var id = 1L;

            given()
                    .spec(requestSpec)
            .when()
                    .delete("/restaurante/{idRestaurante}", id)
            .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("message", equalTo("Restaurante não encontrado com id: " + id));
        }
    }
}
