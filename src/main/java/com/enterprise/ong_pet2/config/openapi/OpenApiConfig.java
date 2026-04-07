package com.enterprise.ong_pet2.config.openapi;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "ONG Pet API",
                version = "2.0",
                description = "API de gestão para ONG de proteção animal. " +
                        "Permite o gerenciamento de animais, adoções, doações, " +
                        "estoque e muito mais.",
                contact = @Contact(
                        name = "ONG Pet",
                        email = "contato@ongpet.com.br"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Ambiente local")
        },
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Insira o token JWT obtido em POST /auth/login"
)
public class OpenApiConfig {}