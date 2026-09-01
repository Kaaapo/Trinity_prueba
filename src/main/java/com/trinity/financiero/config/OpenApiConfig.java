package com.trinity.financiero.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Trinity Financiero API")
                        .version("1.0.0")
                        .description("API RESTful para la administración de clientes, cuentas bancarias (ahorros y corriente) y transacciones financieras (consignaciones, retiros y transferencias).")
                        .contact(new Contact()
                                .name("Trinity")
                                .email("soporte@trinity.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor Local de Desarrollo")
                ));
    }
}
