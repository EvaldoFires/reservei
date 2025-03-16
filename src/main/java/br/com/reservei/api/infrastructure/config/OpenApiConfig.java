package br.com.reservei.api.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Reservei API",
                version = "1.0.0",
                description = "API para Gestão de Reservas e Avaliações em Restaurantes"
        )
)
public class OpenApiConfig {
}
