package br.com.reservei.api.bdd.stepdefs;

import br.com.reservei.api.application.dto.RestauranteDTO;
import br.com.reservei.api.bdd.contexto.ContextoIds;
import br.com.reservei.api.infrastructure.utils.Cozinha;
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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static br.com.reservei.api.infrastructure.utils.RestauranteHelper.gerarRestauranteDtoSemId;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;


public class RestauranteSteps {

    private Response response;
    private RestauranteDTO restauranteDTO;

    @Autowired
    private ContextoIds contextoIds;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @LocalServerPort
    private int port;

    private String ENDPOINT_RESTAURANTE;

    @PostConstruct
    public void init() {
        ENDPOINT_RESTAURANTE = "http://localhost:" + port + "/restaurante";
    }

    @Before
    public void limparBanco() throws IOException {
        String caminhoDoScript = "src/test/resources/clean.sql";

        String scriptSql = new String(Files.readAllBytes(Paths.get(caminhoDoScript)));

        jdbcTemplate.execute(scriptSql);
    }

    @Quando("salvar um novo restaurante")
    public RestauranteDTO salvarRestaurante() {
        restauranteDTO = gerarRestauranteDtoSemId(contextoIds.getEnderecoId());

        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(restauranteDTO)
                .when().post(ENDPOINT_RESTAURANTE);

        return response.then().extract().as(RestauranteDTO.class);
    }

    @Então("o restaurante é salvo com sucesso")
    public void restauranteSalvoComSucesso() {
        response.then()
                .statusCode(HttpStatus.CREATED.value())
                .body("nome", equalTo(restauranteDTO.nome()))
                .body("cozinha", is(restauranteDTO.cozinha().toString()))
                .body("reservasPorHora", equalTo(restauranteDTO.reservasPorHora()))
                .body("inicioExpediente", equalTo(restauranteDTO.inicioExpediente()
                        .format(DateTimeFormatter.ofPattern("HH:mm:ss"))))
                .body("finalExpediente", equalTo(restauranteDTO.finalExpediente()
                        .format(DateTimeFormatter.ofPattern("HH:mm:ss"))))
                .body("enderecoId", is(restauranteDTO.enderecoId().intValue()))
                .extract().response();

        contextoIds.setRestauranteId(response.jsonPath().getLong("id"));
    }

    @Quando("buscar restaurante por nome")
    public void buscarRestaurantePorNome() {
        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get(ENDPOINT_RESTAURANTE + "/nome/" + restauranteDTO.nome());
    }

    @Quando("buscar restaurante por id")
    public void buscarRestaurantePorId() {
        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get(ENDPOINT_RESTAURANTE + "/" + contextoIds.getRestauranteId());
    }

    @Quando("buscar restaurantes por cozinha")
    public void buscarRestaurantePorCozinha() {
        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get(ENDPOINT_RESTAURANTE + "/cozinha/" + restauranteDTO.cozinha());
    }

    @Então("restaurante é retornado com sucesso")
    public void restauranteRetornadoComSucesso() {
        response.then()
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

    @Então("restaurantes são retornados com sucesso")
    public void restaurantesRetornadosComSucesso() {
        response.then()
                .statusCode(HttpStatus.OK.value());
    }

    @Quando("deletar o restaurante por id")
    public void deletarRestaurantePorId() {
        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .delete(ENDPOINT_RESTAURANTE + "/" + contextoIds.getRestauranteId());
    }

    @Então("o restaurante é deletado com sucesso")
    public void restauranteDeletadoComSucesso() {
        response.then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Quando("alterar restaurante")
    public void alterarRestaurante() {
        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(new RestauranteDTO(null, "Basilico", Cozinha.ITALIANA,
                        contextoIds.getEnderecoId(), 2, LocalTime.NOON, LocalTime.MIDNIGHT))
                .when()
                .put(ENDPOINT_RESTAURANTE + "/" + contextoIds.getRestauranteId());
    }

    @Então("o restaurante é alterado com sucesso")
    public void restauranteAlteradoComSucesso() {
        response.then()
                .statusCode(HttpStatus.OK.value())
                .body("nome", equalTo("Basilico"))
                .body("cozinha", is(restauranteDTO.cozinha().toString()))
                .body("reservasPorHora", equalTo(2))
                .body("inicioExpediente", equalTo(restauranteDTO.inicioExpediente()
                        .format(DateTimeFormatter.ofPattern("HH:mm:ss"))))
                .body("finalExpediente", equalTo(restauranteDTO.finalExpediente()
                        .format(DateTimeFormatter.ofPattern("HH:mm:ss"))))
                .body("enderecoId", is(contextoIds.getEnderecoId().intValue()))
                .extract().response();
    }

    @Dado("que restaurante já foi salvo")
    public void restauranteJaFoiSalva(){
        salvarRestaurante();
        restauranteSalvoComSucesso();
    }

}

