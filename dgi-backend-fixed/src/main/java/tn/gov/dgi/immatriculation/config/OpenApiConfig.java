package tn.gov.dgi.immatriculation.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI dgiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API DGI - Immatriculation Fiscale")
                        .description("Backend pour l'immatriculation fiscale en ligne des contribuables")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Direction Générale des Impôts")
                                .email("contact@dgi.gov.tn"))
                        .license(new License().name("Usage interne DGI")));
    }
}