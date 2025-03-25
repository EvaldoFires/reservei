package br.com.reservei.api.bdd.stepdefs;

import br.com.reservei.api.application.dto.ReservaDTO;
import br.com.reservei.api.bdd.contexto.ContextoIds;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import io.restassured.response.Response;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static br.com.reservei.api.infrastructure.utils.ReservaHelper.gerarReservaDtoSemId;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class ReservaSteps {

    private Response response;
    private ReservaDTO reservaDTO;

    @Autowired
    private ContextoIds contextoIds;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @LocalServerPort
    private int port;

    private LocalDateTime localDateTime = LocalDateTime.of(
            LocalDateTime.now().plusDays(1).getYear(),
            LocalDateTime.now().plusDays(1).getMonthValue(),
            LocalDateTime.now().plusDays(1).getDayOfMonth(),
            15, 0, 0, 0);

    private String ENDPOINT_RESERVA;

    @PostConstruct
    public void init() {
        ENDPOINT_RESERVA = "http://localhost:" + port + "/reserva";
        System.out.println(ENDPOINT_RESERVA);
    }

    @Before
    public void limparBanco() throws IOException {
        String caminhoDoScript = "src/test/resources/clean.sql";

        String scriptSql = new String(Files.readAllBytes(Paths.get(caminhoDoScript)));

        jdbcTemplate.execute(scriptSql);
    }

    @Quando("salvar uma nova reserva")
    public ReservaDTO salvarReserva() {
        reservaDTO = gerarReservaDtoSemId(contextoIds.getEstadoId());

        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(reservaDTO)
                .when().post(ENDPOINT_RESERVA);

        return response.then().extract().as(ReservaDTO.class);
    }

    @Então("a reserva é salva com sucesso")
    public void reservaSalvaComSucesso() {
        response.then()
                .statusCode(HttpStatus.CREATED.value())
                .body("horaDaReserva", equalTo(reservaDTO.horaDaReserva()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
                .body("restauranteId", is(reservaDTO.restauranteId().intValue()))
                .extract().response();

        contextoIds.setReservaId(response.jsonPath().getLong("id"));
    }

    @Quando("buscar reserva por id")
    public void buscarReservaPorId() {
        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get(ENDPOINT_RESERVA + "/" + contextoIds.getReservaId());
    }

    @Então("a reserva é retornada com sucesso")
    public void reservaRetornadaComId() {
        response.then()
                .statusCode(HttpStatus.OK.value())
                .body("horaDaReserva", equalTo(reservaDTO.horaDaReserva()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
                .body("restauranteId", is(reservaDTO.restauranteId().intValue()));
    }


    @Quando("deletar a reserva por id")
    public void deletarReservaPorId() {
        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .delete(ENDPOINT_RESERVA + "/" + contextoIds.getReservaId());
    }

    @Então("a reserva é deletada com sucesso")
    public void reservaDeletadaComSucesso() {
        response.then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Quando("alterar reserva")
    public void alterarReserva() {


        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(new ReservaDTO(null, contextoIds.getRestauranteId(), localDateTime))
                .when()
                .put(ENDPOINT_RESERVA + "/" + contextoIds.getReservaId());
    }

    @Então("a reserva é alterada com sucesso")
    public void reservaAlteradaComSucesso() {
        response.then()
                .statusCode(HttpStatus.OK.value())
                .body("horaDaReserva", equalTo(localDateTime
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
                .body("restauranteId", is(reservaDTO.restauranteId().intValue()))
                .extract().response();
    }


    @Dado("que reserva já foi salva")
    public void reservaJaFoiSalva(){
        salvarReserva();
        reservaSalvaComSucesso();
    }
}
