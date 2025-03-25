package br.com.reservei.api.bdd.stepdefs;

import br.com.reservei.api.application.dto.AvaliacaoDTO;
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

import static br.com.reservei.api.infrastructure.utils.AvaliacaoHelper.gerarAvaliacaoDtoSemId;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class AvaliacaoSteps {

    private Response response;
    private AvaliacaoDTO avaliacaoDTO;

    @Autowired
    private ContextoIds contextoIds;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @LocalServerPort
    private int port;

    private String ENDPOINT_AVALIACAO;

    @PostConstruct
    public void init() {
        ENDPOINT_AVALIACAO = "http://localhost:" + port + "/avaliacao";
        System.out.println(ENDPOINT_AVALIACAO);
    }

    @Before
    public void limparBanco() throws IOException {
        String caminhoDoScript = "src/test/resources/clean.sql";

        String scriptSql = new String(Files.readAllBytes(Paths.get(caminhoDoScript)));

        jdbcTemplate.execute(scriptSql);
    }

    @Quando("salvar uma nova avaliação")
    public AvaliacaoDTO salvarAvaliacao() {
        avaliacaoDTO = gerarAvaliacaoDtoSemId(contextoIds.getEstadoId());

        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(avaliacaoDTO)
                .when().post(ENDPOINT_AVALIACAO);

        return response.then().extract().as(AvaliacaoDTO.class);
    }

    @Então("a avaliação é salva com sucesso")
    public void avaliacaoSalvaComSucesso() {
        response.then()
                .statusCode(HttpStatus.CREATED.value())
                .body("nota", equalTo(avaliacaoDTO.nota()))
                .body("comentario", equalTo(avaliacaoDTO.comentario()))
                .body("restauranteId", is(contextoIds.getRestauranteId().intValue()))
                .extract().response();

        contextoIds.setAvaliacaoId(response.jsonPath().getLong("id"));
    }

    @Quando("buscar avaliação por id")
    public void buscarAvaliaçãoPorId() {
        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get(ENDPOINT_AVALIACAO + "/" + contextoIds.getAvaliacaoId());
    }

    @Então("a avaliação é retornada com sucesso")
    public void avaliaçãoRetornadaComId() {
        response.then()
                .statusCode(HttpStatus.OK.value())
                .body("nota", equalTo(avaliacaoDTO.nota()))
                .body("comentario", equalTo(avaliacaoDTO.comentario()))
                .body("restauranteId", is(contextoIds.getRestauranteId().intValue()));
    }


    @Quando("deletar a avaliação por id")
    public void deletarAvaliaçãoPorId() {
        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .delete(ENDPOINT_AVALIACAO + "/" + contextoIds.getAvaliacaoId());
    }

    @Então("a avaliação é deletada com sucesso")
    public void avaliacaoDeletadaComSucesso() {
        response.then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Quando("alterar avaliação")
    public void alterarAvaliacao() {


        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(new AvaliacaoDTO(null, 5, "otimo",
                        null, contextoIds.getRestauranteId()))
                .when()
                .put(ENDPOINT_AVALIACAO + "/" + contextoIds.getAvaliacaoId());
    }

    @Então("a avaliação é alterada com sucesso")
    public void avaliacaoAlteradaComSucesso() {
        response.then()
                .statusCode(HttpStatus.OK.value())
                .body("nota", equalTo(5))
                .body("comentario", equalTo("otimo"))
                .body("restauranteId", is(contextoIds.getRestauranteId().intValue()))
                .extract().response();
    }


    @Dado("que avaliação já foi salva")
    public void avaliacaoJaFoiSalva(){
        salvarAvaliacao();
        avaliacaoSalvaComSucesso();
    }
}
