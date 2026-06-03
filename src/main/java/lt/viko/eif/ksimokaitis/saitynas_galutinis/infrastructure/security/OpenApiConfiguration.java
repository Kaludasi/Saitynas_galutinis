package lt.viko.eif.ksimokaitis.saitynas_galutinis.infrastructure.security;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Bank API",
                version = "v1",
                description = "REST API for account management, payments, authentication, and currency exchange.",
                contact = @Contact(name = "Saitynas Team")
        ),
        servers = @Server(url = "/", description = "Current environment"),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "API token",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfiguration {

    @Bean
    OpenAPI customOpenApi() {
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("Bank API")
                        .version("v1")
                        .description("REST API for account management, payments, authentication, and currency exchange.")
                        .license(new License().name("Academic use")));
    }
}
