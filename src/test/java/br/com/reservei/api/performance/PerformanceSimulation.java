package br.com.reservei.api.performance;



import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class PerformanceSimulation extends Simulation {

    private final HttpProtocolBuilder httpProtocol =
            http.baseUrl("http://localhost:8080")
                    .header("Content-Type", "application/json");

    /* ESTADO INSERT */
    private static final List<String> idsInseridos = Collections.synchronizedList(new ArrayList<>());

    Map<String, String> estadosBrasil = Map.ofEntries(
            Map.entry("Acre", "AC"),
            Map.entry("Alagoas", "AL"),
            Map.entry("Amapa", "AP"),
            Map.entry("Amazonas", "AM"),
            Map.entry("Bahia", "BA"),
            Map.entry("Ceara", "CE"),
            Map.entry("Distrito Federal", "DF"),
            Map.entry("Espirito Santo", "ES"),
            Map.entry("Goias", "GO"),
            Map.entry("Maranhao", "MA"),
            Map.entry("Mato Grosso", "MT"),
            Map.entry("Mato Grosso do Sul", "MS"),
            Map.entry("Minas Gerais", "MG"),
            Map.entry("Para", "PA"),
            Map.entry("Paraiba", "PB"),
            Map.entry("Parana", "PR"),
            Map.entry("Pernambuco", "PE"),
            Map.entry("Piaui", "PI"),
            Map.entry("Rio de Janeiro", "RJ"),
            Map.entry("Rio Grande do Norte", "RN"),
            Map.entry("Rio Grande do Sul", "RS"),
            Map.entry("Rondonia", "RO"),
            Map.entry("Roraima", "RR"),
            Map.entry("Santa Catarina", "SC"),
            Map.entry("Sao Paulo", "SP"),
            Map.entry("Sergipe", "SE"),
            Map.entry("Tocantins", "TO")
    );

    ActionBuilder adicionarEstadoRequest = http("request: adicionar estado")
            .post("/estado")
            .body(StringBody("{\"nome\": \"Minas Gerais\", \"sigla\":\"MG\"}"))
            .check(status().is(201))
            .check(jsonPath("$.id").saveAs("estadoId"));

    AtomicInteger contador = new AtomicInteger(1);

    ScenarioBuilder cenarioAdicionarEstado = scenario("adicionar estado")
            .feed(
                    () -> Stream.generate(contador::getAndIncrement)
                            .map(i -> {
                                Map.Entry<String, String> e = estadosBrasil.entrySet().stream().skip(i % 26).findFirst().orElseThrow();
                                Map<String, Object> map = new HashMap<>();
                                map.put("nome", e.getKey() + i);
                                map.put("sigla", e.getValue() + i);
                                return map;
                            })
                            .limit(10000) // quantas repeticoes quiser
                            .toList()
                            .iterator()
            )
            .exec(adicionarEstadoRequest)
            .exec(session -> {
                String id = session.getString("estadoId");
                //System.out.println("ID do Insert=" + id);
                idsInseridos.add(id);
                return session;
            });

    /* ESTADO BUSCA */
    private static final AtomicInteger index = new AtomicInteger(0);

    ActionBuilder buscarEstadoRequest = http("request: buscar estado")
            .get("/estado/#{estadoId}")
            .check(status().is(200));

    ScenarioBuilder cenarioBuscarEstado = scenario("buscar estado")
            .exec(session -> {
                int i = index.getAndUpdate(val -> (val + 1) % idsInseridos.size());
                String id = idsInseridos.get(i);
                return session.set("estadoId", id);
            })
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
                        nothingFor(Duration.ofSeconds(55)),
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
                        global().responseTime().max().lt(500)
                );
    }





}
