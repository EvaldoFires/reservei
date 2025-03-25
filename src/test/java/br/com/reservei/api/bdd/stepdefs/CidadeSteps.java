package br.com.reservei.api.bdd.stepdefs;

import br.com.reservei.api.application.dto.CidadeDTO;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static br.com.reservei.api.infrastructure.utils.CidadeHelper.gerarCidadeDtoSemId;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class CidadeSteps {

    private Response response;
    private CidadeDTO cidadeDTO;

    @Autowired
    private ContextoIds contextoIds;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @LocalServerPort
    private int port;

    private String ENDPOINT_CIDADE;

    @PostConstruct
    public void init() {
        ENDPOINT_CIDADE = "http://localhost:" + port + "/cidade";
        System.out.println(ENDPOINT_CIDADE);
    }

    @Before
    public void limparBanco() throws IOException {
        String caminhoDoScript = "src/test/resources/clean.sql";

        String scriptSql = new String(Files.readAllBytes(Paths.get(caminhoDoScript)));

        jdbcTemplate.execute(scriptSql);
    }

    @Quando("salvar uma nova cidade")
    public CidadeDTO salvarCidade() {
        cidadeDTO = gerarCidadeDtoSemId(contextoIds.getEstadoId());

        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(cidadeDTO)
                .when().post(ENDPOINT_CIDADE);

        return response.then().extract().as(CidadeDTO.class);
    }

    @Então("a cidade é salva com sucesso")
    public void cidadeSalvaComSucesso() {
        response.then()
                .statusCode(HttpStatus.CREATED.value())
                .body("nome", equalTo(cidadeDTO.nome()))
                .body("estadoId", is(cidadeDTO.estadoId().intValue()))
                .extract().response();

        contextoIds.setCidadeId(response.jsonPath().getLong("id"));
    }

    @Quando("buscar cidade por id")
    public void buscarCidadePorId() {
        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get(ENDPOINT_CIDADE + "/" + contextoIds.getCidadeId());
    }

    @Então("a cidade é retornada com id")
    public void cidadeRetornadaComId() {
        response.then()
                .statusCode(HttpStatus.OK.value())
                .body("nome", equalTo(cidadeDTO.nome()))
                .body("estadoId", is(cidadeDTO.estadoId().intValue()));
    }


    @Quando("deletar a cidade por id")
    public void deletarCidadePorId() {
        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .delete(ENDPOINT_CIDADE + "/" + contextoIds.getCidadeId());
    }

    @Então("a cidade é deletada com sucesso")
    public void cidadeDeletadaComSucesso() {
        response.then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Quando("alterar cidade")
    public void alterarCidade() {
        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(new CidadeDTO(null, "São Paulo", contextoIds.getEstadoId()))
                .when()
                .put(ENDPOINT_CIDADE + "/" + contextoIds.getCidadeId());
    }

    @Então("a cidade é alterada com sucesso")
    public void cidadeAlteradaComSucesso() {
        response.then()
                .statusCode(HttpStatus.OK.value())
                .body("nome", equalTo("São Paulo"))
                .body("estadoId", is(cidadeDTO.estadoId().intValue()))
                .extract().response();
    }


    @Dado("que cidade já foi salva")
    public void cidadeJaFoiSalva(){
        salvarCidade();
        cidadeSalvaComSucesso();
    }
}
