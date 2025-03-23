package br.com.reservei.api.interfaces.controller;

import br.com.reservei.api.application.dto.CidadeDTO;
import br.com.reservei.api.application.dto.EnderecoDTO;
import br.com.reservei.api.application.usecases.endereco.CidadeServiceImpl;
import br.com.reservei.api.application.usecases.endereco.EnderecoServiceImpl;
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

import static br.com.reservei.api.infrastructure.utils.CidadeHelper.gerarCidadeDtoSemId;
import static br.com.reservei.api.infrastructure.utils.EnderecoHelper.gerarEnderecoDtoSemId;
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
class EnderecoControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private EnderecoServiceImpl enderecoService;
    @Autowired
    private CidadeServiceImpl cidadeService;
    @Autowired
    private EstadoServiceImpl estadoService;

    private static RequestSpecification requestSpec;

    private EnderecoDTO enderecoDTO;
    private CidadeDTO cidadeDTO;

    @BeforeEach
    void setUp() {
        var estadoDTO = estadoService.salvar(gerarEstadoDto(gerarEstadoSemId()));
        this.cidadeDTO = cidadeService.salvar(gerarCidadeDtoSemId(estadoDTO.id()));

        this.enderecoDTO = gerarEnderecoDtoSemId(cidadeDTO.id());

        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        requestSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .build();
    }

    @DisplayName("Buscar Endereço")
    @Nested
    class buscarEndereco{

        @DisplayName("Deve buscar um Endereço pelo ID fornecido")
        @Test
        void deveBuscarEnderecoPorId() {
            var enderecoSalvo = enderecoService.salvar(enderecoDTO);

            given()
                    .spec(requestSpec)
            .when()
                    .get("/endereco/{idEndereco}", enderecoSalvo.id())
            .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("bairro", equalTo(enderecoDTO.bairro()))
                    .body("rua", equalTo(enderecoDTO.rua()))
                    .body("numero", equalTo(enderecoDTO.numero()))
                    .body("cidadeId", is(enderecoDTO.cidadeId().intValue()));
        }

        @DisplayName("Deve lançar exceção ao buscar Endereço com ID inexistente")
        @Test
        void deveGerarExcecao_QuandoBuscarEndereco_PorIdInexistente() {
            var id = 1L;

            given()
                    .spec(requestSpec)
            .when()
                    .get("/endereco/{idEndereco}", id)
            .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("message", equalTo("Endereço não encontrado com id: " + id));

        }

        @DisplayName("Deve retornar uma lista de endereços salvos")
        @Test
        void deveBuscarTodosOsEnderecos() {
            var enderecos = List.of(enderecoService.salvar(enderecoDTO),
                    enderecoService.salvar(new EnderecoDTO(null, enderecoDTO.cidadeId(), "Outro bairro",
                            "Outra rua", "2")));

            given()
                    .spec(requestSpec)
            .when()
                    .get("/endereco")
            .then()
                    .statusCode(HttpStatus.OK.value())
                    .body(equalTo(asJsonString(enderecos)));
        }
    }

    @DisplayName("Salvar Endereço")
    @Nested
    class SalvarEndereco {

        @DisplayName("Deve salvar Endereço")
        @Test
        void deveSalvarEndereco() {

            given()
                    .spec(requestSpec)
                    .body(enderecoDTO)
            .when()
                    .post("/endereco")
            .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .body("id", is(notNullValue()))
                    .body("bairro", equalTo(enderecoDTO.bairro()))
                    .body("rua", equalTo(enderecoDTO.rua()))
                    .body("numero", equalTo(enderecoDTO.numero()))
                    .body("cidadeId", is(enderecoDTO.cidadeId().intValue()));
        }

    }

    @DisplayName("Alterar Endereço")
    @Nested
    class AlterarEndereco {

        @DisplayName("Deve alterar Endereço cadastrada")
        @Test
        void deveAtualizarEndereco() {
            var enderecoSalvo = enderecoService.salvar(enderecoDTO);

            var enderecoNovo = new EnderecoDTO(enderecoSalvo.id(), enderecoDTO.cidadeId(), "Outro bairro",
                    "Outra rua", "2");

            given()
                    .spec(requestSpec)
                    .body(enderecoNovo)
            .when()
                    .put("/endereco/{idEndereco}", enderecoSalvo.id())
            .then()
                    .statusCode(HttpStatus.OK.value())
                    .body(equalTo(asJsonString(enderecoNovo)));
        }

        @DisplayName("Deve lançar exceção ao tentar alterar Endereço com id inexistente")
        @Test
        void deveGerarExcecao_QuandoAlterarEndereco_PorIdInexistente() {
            var id = 1L;

            given()
                    .spec(requestSpec)
                    .body(enderecoDTO)
            .when()
                    .put("/endereco/{idEndereco}", id)
            .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("message", equalTo("Endereço não encontrado com id: " + id));
        }

    }

    @DisplayName("Deletar Endereço")
    @Nested
    class DeletarEndereco {

        @DisplayName("Deve deletar Endereço")
        @Test
        void deveDeletarEndereco() {
            var enderecoSalvo = enderecoService.salvar(enderecoDTO);

            given()
                    .spec(requestSpec)
            .when()
                    .delete("/endereco/{idEndereco}", enderecoSalvo.id())
            .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());
        }

        @DisplayName("Deve lançar exceção ao tentar deletar Endereço por id inexistente")
        @Test
        void deveGerarExcecao_QuandoDeletarEndereco_PorIdInexistente() {
            var id = 1L;

            given()
                    .spec(requestSpec)
            .when()
                    .delete("/endereco/{idEndereco}", id)
            .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("message", equalTo("Endereço não encontrado com id: " + id));
        }
    }
}
