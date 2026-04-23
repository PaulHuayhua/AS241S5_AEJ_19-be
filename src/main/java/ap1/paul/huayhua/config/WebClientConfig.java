package ap1.paul.huayhua.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Value("${rapidapi.key}")
    private String rapidApiKey;

    @Value("${rapidapi.host-weather}")
    private String weatherHost;

    @Bean
    public WebClient groqWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .defaultHeader("Authorization", "Bearer " + groqApiKey)
                .defaultHeader("Content-Type", "application/json")
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024)) // 10 MB
                .build();
    }

    @Bean
    public WebClient weatherWebClient() {
        return WebClient.builder()
                .baseUrl("https://weather-intelligence1.p.rapidapi.com")
                .defaultHeader("x-rapidapi-host", weatherHost)
                .defaultHeader("x-rapidapi-key", rapidApiKey)
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024)) // 10 MB
                .build();
    }
}
