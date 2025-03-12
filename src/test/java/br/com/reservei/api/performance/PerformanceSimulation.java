package br.com.reservei.api.performance;

import io.gatling.javaapi.core.ActionBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class PerformanceSimulation extends Simulation {

    private final HttpProtocolBuilder httpProtocol =
            http.baseUrl("http://localhost:8080")
                    .header("Content-Type", "application/json");

    ActionBuilder adicionarEstadoRequest = http("request: adicionar estado")
            .post("/estado")
            .body(StringBody("{\"nome\": \"nome\", \"sigla\":\"sigla\"}"))
            .check(status().is(201))
            .check(jsonPath("$.id").saveAs("estadoId"));

    ActionBuilder buscarEstadoRequest = http("request: buscar estado")
            .get("/estado/#{estadoId}")
            .check(status().is(200));

    ScenarioBuilder cenarioAdicionarEstado = scenario("adicionar estado")
            .exec(adicionarEstadoRequest);

    ScenarioBuilder cenarioBuscarEstado = scenario("buscar estado")
            .exec(adicionarEstadoRequest)
            .exec(buscarEstadoRequest);

    {
        setUp(
                cenarioAdicionarEstado.injectOpen(
                        rampUsersPerSec(1)
                                .to(2)
                                .during(Duration.ofSeconds(10)),
                        constantUsersPerSec(2)
                                .during(Duration.ofSeconds(20)),
                        rampUsersPerSec(2)
                                .to(1)
                                .during(Duration.ofSeconds(10))
                ),
                cenarioBuscarEstado.injectOpen(
                        rampUsersPerSec(1)
                                .to(10)
                                .during(Duration.ofSeconds(10)),
                        constantUsersPerSec(10)
                                .during(Duration.ofSeconds(20)),
                        rampUsersPerSec(10)
                                .to(1)
                                .during(Duration.ofSeconds(10))
                )
        )
                .protocols(httpProtocol)
                .assertions(
                        global().responseTime().max().lt(50)
                );
    }
}
