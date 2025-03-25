package br.com.reservei.api.bdd.stepdefs;

import br.com.reservei.api.application.dto.EnderecoDTO;
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

import static br.com.reservei.api.infrastructure.utils.EnderecoHelper.gerarEnderecoDtoSemId;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;


public class EnderecoSteps {

    private Response response;
    private EnderecoDTO enderecoDTO;

    @Autowired
    private ContextoIds contextoIds;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @LocalServerPort
    private int port;

    private String ENDPOINT_ENDERECO;

    @PostConstruct
    public void init() {
        ENDPOINT_ENDERECO = "http://localhost:" + port + "/endereco";
        System.out.println(ENDPOINT_ENDERECO);
    }

    @Before
    public void limparBanco() throws IOException {
        String caminhoDoScript = "src/test/resources/clean.sql";

        String scriptSql = new String(Files.readAllBytes(Paths.get(caminhoDoScript)));

        jdbcTemplate.execute(scriptSql);
    }

    @Quando("salvar um novo endereço")
    public EnderecoDTO salvarEndereco() {
        enderecoDTO = gerarEnderecoDtoSemId(contextoIds.getCidadeId());

        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(enderecoDTO)
                .when().post(ENDPOINT_ENDERECO);

        return response.then().extract().as(EnderecoDTO.class);
    }

    @Então("o endereço é salvo com sucesso")
    public void enderecoSalvaComSucesso() {
        response.then()
                .statusCode(HttpStatus.CREATED.value())
                .body("bairro", equalTo(enderecoDTO.bairro()))
                .body("rua", equalTo(enderecoDTO.rua()))
                .body("numero", equalTo(enderecoDTO.numero()))
                .body("cidadeId", is(contextoIds.getCidadeId().intValue()))
                .body("cep", equalTo(enderecoDTO.cep()))
                .extract().response();

        contextoIds.setEnderecoId(response.jsonPath().getLong("id"));
    }

    @Quando("buscar endereço por id")
    public void buscarEnderecoPorId() {
        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get(ENDPOINT_ENDERECO + "/" + contextoIds.getEnderecoId());
    }

    @Então("o endereço é retornado com id")
    public void enderecoRetornadoComId() {
        response.then()
                .statusCode(HttpStatus.OK.value())
                .body("bairro", equalTo(enderecoDTO.bairro()))
                .body("rua", equalTo(enderecoDTO.rua()))
                .body("numero", equalTo(enderecoDTO.numero()))
                .body("cidadeId", is(contextoIds.getCidadeId().intValue()))
                .body("cep", equalTo(enderecoDTO.cep()));
    }


    @Quando("deletar o endereço por id")
    public void deletarEnderecoPorId() {
        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .delete(ENDPOINT_ENDERECO + "/" + contextoIds.getEnderecoId());
    }

    @Então("o endereço é deletado com sucesso")
    public void enderecoDeletadoComSucesso() {
        response.then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Quando("alterar endereço")
    public void alterarEndereco() {
        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(new EnderecoDTO(null, contextoIds.getCidadeId(), "Bairro Novo",
                        "Rua Nova", "2", "42600-000"))
                .when()
                .put(ENDPOINT_ENDERECO + "/" + contextoIds.getEnderecoId());
    }

    @Então("o endereço é alterado com sucesso")
    public void enderecoAlteradoComSucesso() {
        response.then()
                .statusCode(HttpStatus.OK.value())
                .body("bairro", equalTo("Bairro Novo"))
                .body("rua", equalTo("Rua Nova"))
                .body("numero", equalTo("2"))
                .body("cidadeId", is(contextoIds.getCidadeId().intValue()))
                .body("cep", equalTo("42600-000"))
                .extract().response();
    }

    @Dado("que endereço já foi salvo")
    public void enderecoJaFoiSalvo(){
        salvarEndereco();
        enderecoSalvaComSucesso();
    }
}
