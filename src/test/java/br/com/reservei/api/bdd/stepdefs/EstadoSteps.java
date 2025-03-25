package br.com.reservei.api.bdd.stepdefs;

import br.com.reservei.api.application.dto.EstadoDTO;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static br.com.reservei.api.infrastructure.utils.EstadoHelper.gerarEstadoDto;
import static br.com.reservei.api.infrastructure.utils.EstadoHelper.gerarEstadoSemId;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class EstadoSteps {

    private Response response;
    private EstadoDTO estadoDTO;

    @Autowired
    private ContextoIds contextoIds;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @LocalServerPort
    private int port;

    private String ENDPOINT_ESTADO;

    @PostConstruct
    public void init() {
        ENDPOINT_ESTADO = "http://localhost:" + port + "/estado";
        System.out.println(ENDPOINT_ESTADO);
    }

    @Before
    public void limparBanco() throws IOException {
        String caminhoDoScript = "src/test/resources/clean.sql";

        String scriptSql = new String(Files.readAllBytes(Paths.get(caminhoDoScript)));

        jdbcTemplate.execute(scriptSql);
    }

    @Quando("salvar um novo estado")
    public EstadoDTO salvarEstado() {
        estadoDTO = gerarEstadoDto(gerarEstadoSemId());

        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(estadoDTO)
                .when()
                .post(ENDPOINT_ESTADO);

        return response.then().extract().as(EstadoDTO.class);
    }

    @Então("o estado é salvo com sucesso")
    public void estadoSalvoComSucesso() {
         response.then()
                 .statusCode(HttpStatus.CREATED.value())
                 .body("nome", equalTo(estadoDTO.nome()))
                 .body("sigla", equalTo(estadoDTO.sigla()))
                 .extract().response();

         contextoIds.setEstadoId(response.jsonPath().getLong("id"));
    }

    @Quando("buscar estado por id")
    public void buscarEstadoPorId() {
        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get(ENDPOINT_ESTADO + "/" + contextoIds.getEstadoId());
    }

    @Então("o estado é retornado com id")
    public void estadoRetornadoComId() {
        response.then()
                .statusCode(HttpStatus.OK.value())
                .body("nome", equalTo(estadoDTO.nome()))
                .body("sigla", equalTo(estadoDTO.sigla()));
    }


    @Quando("deletar o estado por id")
    public void deletarOEstadoPorId() {
        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .delete(ENDPOINT_ESTADO + "/" + contextoIds.getEstadoId());
    }

    @Então("o estado é deletado com sucesso")
    public void estadoDeletadoComSucesso() {
        response.then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Quando("alterar estado")
    public void alterarEstado() {
        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(new EstadoDTO(null, "São Paulo", "SP"))
                .when()
                .put(ENDPOINT_ESTADO + "/" + contextoIds.getEstadoId());
    }

    @Então("o estado é alterado com sucesso")
    public void estadoAlteradoComSucesso() {
        response.then()
                .statusCode(HttpStatus.OK.value())
                .body("nome", equalTo("São Paulo"))
                .body("sigla", equalTo("SP"))
                .extract().response();
    }

    @Dado("que estado já foi salvo")
    public void estadoJaFoiSalvo(){
        salvarEstado();
        estadoSalvoComSucesso();
    }
}
