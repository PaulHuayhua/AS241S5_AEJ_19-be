package ap1.paul.huayhua.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
public class OpenApiConfig implements WebFluxConfigurer {

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("api-public")
                .pathsToMatch("/api/**")
                .build();
    }

    @Bean
    public OpenAPI apiInfo(@Value("${server.url:http://localhost:8080}") String serverUrl) {
        return new OpenAPI()
                .addServersItem(new Server().url(serverUrl))
                .info(new Info()
                        .title("API WebFlux con MongoDB y APIs IA")
                        .description("REST API reactiva con MongoDB y servicios de IA (Scraper y Weather)")
                        .license(new License().name("Paul Huayhua").url("https://github.com/paulhuayhua"))
                        .version("1.0.0")
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
