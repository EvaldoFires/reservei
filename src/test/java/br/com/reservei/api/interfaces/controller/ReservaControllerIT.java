package br.com.reservei.api.interfaces.controller;

import br.com.reservei.api.application.dto.ReservaDTO;
import br.com.reservei.api.application.dto.RestauranteDTO;
import br.com.reservei.api.application.usecases.endereco.CidadeServiceImpl;
import br.com.reservei.api.application.usecases.endereco.EnderecoServiceImpl;
import br.com.reservei.api.application.usecases.endereco.EstadoServiceImpl;
import br.com.reservei.api.application.usecases.reserva.ReservaServiceImpl;
import br.com.reservei.api.application.usecases.restaurante.RestauranteServiceImpl;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static br.com.reservei.api.infrastructure.utils.CidadeHelper.gerarCidadeDtoSemId;
import static br.com.reservei.api.infrastructure.utils.EnderecoHelper.gerarEnderecoDtoSemId;
import static br.com.reservei.api.infrastructure.utils.EstadoHelper.gerarEstadoDto;
import static br.com.reservei.api.infrastructure.utils.EstadoHelper.gerarEstadoSemId;
import static br.com.reservei.api.infrastructure.utils.GeneralHelper.asJsonString;
import static br.com.reservei.api.infrastructure.utils.ReservaHelper.gerarReservaDtoSemId;
import static br.com.reservei.api.infrastructure.utils.RestauranteHelper.gerarRestauranteDtoSemId;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = {"/clean.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ReservaControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private ReservaServiceImpl reservaService;
    @Autowired
    private CidadeServiceImpl cidadeService;
    @Autowired
    private EstadoServiceImpl estadoService;
    @Autowired
    private EnderecoServiceImpl enderecoService;
    @Autowired
    private RestauranteServiceImpl restauranteService;

    @Autowired
    private EntityManager entityManager;

    private static RequestSpecification requestSpec;

    private ReservaDTO reservaDTO;
    private RestauranteDTO restauranteDTO;

    @BeforeEach
    void setUp() {
        var estadoDTO = estadoService.salvar(gerarEstadoDto(gerarEstadoSemId()));
        var cidadeDTO = cidadeService.salvar(gerarCidadeDtoSemId(estadoDTO.id()));
        var enderecoDTO = enderecoService.salvar(gerarEnderecoDtoSemId(cidadeDTO.id()));
        this.restauranteDTO = restauranteService.salvar(gerarRestauranteDtoSemId(enderecoDTO.id()));

        this.reservaDTO = gerarReservaDtoSemId(restauranteDTO.id());

        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        requestSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .build();
    }

    @DisplayName("Buscar Reserva")
    @Nested
    class buscarReserva{

        @DisplayName("Deve buscar um Reserva pelo ID fornecido")
        @Test
        void deveBuscarReservaPorId() {
            var reservaSalvo = reservaService.salvar(reservaDTO);

            given()
                    .spec(requestSpec)
            .when()
                    .get("/reserva/{idReserva}", reservaSalvo.id())
            .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("horaDaReserva", equalTo(reservaDTO.horaDaReserva()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
                    .body("restauranteId", is(reservaDTO.restauranteId().intValue()));
        }

        @DisplayName("Deve lançar exceção ao buscar Reserva com ID inexistente")
        @Test
        void deveGerarExcecao_QuandoBuscarReserva_PorIdInexistente() {
            var id = 1L;

            given()
                    .spec(requestSpec)
            .when()
                    .get("/reserva/{idReserva}", id)
            .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("message", equalTo("Reserva não encontrada com id: " + id));

        }

        @DisplayName("Deve retornar uma lista de reservas salvos")
        @Test
        void deveBuscarTodosOsReservas() {
            var reservas = List.of(reservaService.salvar(gerarReservaDtoSemId(reservaDTO.restauranteId())),
                    reservaService.salvar(new ReservaDTO(null, reservaDTO.restauranteId(),
                            LocalDateTime.now())));

            given()
                    .spec(requestSpec)
            .when()
                    .get("/reserva")
            .then()
                    .statusCode(HttpStatus.OK.value())
                    .body(equalTo(asJsonString(reservas)));
        }
    }

    @DisplayName("Salvar Reserva")
    @Nested
    class SalvarReserva {

        @DisplayName("Deve salvar Reserva")
        @Test
        void deveSalvarReserva() {

            given()
                    .spec(requestSpec)
                    .body(reservaDTO)
            .when()
                    .post("/reserva")
            .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .body("id", is(notNullValue()))
                    .body("horaDaReserva", equalTo(reservaDTO.horaDaReserva()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
                    .body("restauranteId", is(reservaDTO.restauranteId().intValue()));
        }

        @DisplayName("Deve lançar exceção ao tentar salvar Reserva com restaurante inexistente")
        @Test
        void deveGerarExcecao_QuandoSalvarReserva_ComRestauranteInexistente() {
            reservaDTO = new ReservaDTO(null, 2L, LocalDateTime.now());

            given()
                    .spec(requestSpec)
                    .body(reservaDTO)
            .when()
                    .post("/reserva")
            .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("message", equalTo("Restaurante não encontrado com id: " +
                            reservaDTO.restauranteId()));
        }
    }

    @DisplayName("Alterar Reserva")
    @Nested
    class AlterarReserva {

        @DisplayName("Deve alterar Reserva cadastrada")
        @Test
        void deveAtualizarReserva() {
            var reservaSalvo = reservaService.salvar(reservaDTO);

            var reservaNovo = new ReservaDTO(reservaSalvo.id(), reservaSalvo.restauranteId(),
                    LocalDateTime.now().plusHours(2));

            given()
                    .spec(requestSpec)
                    .body(reservaNovo)
            .when()
                    .put("/reserva/{idReserva}", reservaSalvo.id())
            .then()
                    .statusCode(HttpStatus.OK.value())
                    .body(equalTo(asJsonString(reservaNovo)));
        }

        @DisplayName("Deve lançar exceção ao tentar alterar Reserva com id inexistente")
        @Test
        void deveGerarExcecao_QuandoAlterarReserva_PorIdInexistente() {
            var id = 1L;

            given()
                    .spec(requestSpec)
                    .body(reservaDTO)
            .when()
                    .put("/reserva/{idReserva}", id)
            .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("message", equalTo("Reserva não encontrada com id: " + id));
        }

        @DisplayName("Deve lançar exceção ao tentar alterar Reserva com cidade inexistente")
        @Test
        void deveGerarExcecao_QuandoAlterarReserva_ComRestauranteInexistente() {
            var reservaSalvo = reservaService.salvar(reservaDTO);

            var reservaNovo = new ReservaDTO(reservaSalvo.id(), 2L, LocalDateTime.now());

            given()
                    .spec(requestSpec)
                    .body(reservaNovo)
            .when()
                    .put("/reserva/{idEndereco}", reservaSalvo.id())
            .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("message", equalTo("Restaurante não encontrado com id: "
                            + reservaNovo.restauranteId()));
        }
    }

    @DisplayName("Deletar Reserva")
    @Nested
    class DeletarReserva {

        @DisplayName("Deve deletar Reserva")
        @Test
        void deveDeletarReserva() {
            var reservaSalvo = reservaService.salvar(reservaDTO);

            given()
                    .spec(requestSpec)
            .when()
                    .delete("/reserva/{idReserva}", reservaSalvo.id())
            .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());
        }

        @DisplayName("Deve lançar exceção ao tentar deletar Reserva por id inexistente")
        @Test
        void deveGerarExcecao_QuandoDeletarReserva_PorIdInexistente() {
            var id = 1L;

            given()
                    .spec(requestSpec)
            .when()
                    .delete("/reserva/{idReserva}", id)
            .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("message", equalTo("Reserva não encontrada com id: " + id));
        }
    }
}
