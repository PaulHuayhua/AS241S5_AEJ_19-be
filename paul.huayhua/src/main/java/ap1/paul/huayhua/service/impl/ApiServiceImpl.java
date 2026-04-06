package ap1.paul.huayhua.service.impl;

import ap1.paul.huayhua.model.ApiResult;
import ap1.paul.huayhua.repository.ApiResultRepository;
import ap1.paul.huayhua.service.ApiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class ApiServiceImpl implements ApiService {

    @Value("${rapidapi.key}")
    private String apiKey;

    @Value("${rapidapi.host-scraper}")
    private String scraperHost;

    @Value("${rapidapi.host-weather}")
    private String weatherHost;

    private final ApiResultRepository repository;
    private final WebClient webClient;

    public ApiServiceImpl(ApiResultRepository repository) {
        this.repository = repository;
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024)) // 10 MB
                .build();
    }

    @Override
    public Mono<String> scrape(String url) {
        return webClient.post()
                .uri("https://ai-content-scraper.p.rapidapi.com/scrape")
                .header("Content-Type", "application/json")
                .header("x-rapidapi-key", apiKey)
                .header("x-rapidapi-host", scraperHost)
                .bodyValue(Map.of("url", url))
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(response -> {
                    ApiResult result = new ApiResult();
                    result.setType("scraper");
                    result.setQuery(url);
                    result.setResponse(response);
                    return repository.save(result).thenReturn(response);
                })
                .onErrorResume(e -> Mono.just("Error Scraper: " + e.getMessage()));
    }

    @Override
    public Mono<String> getWeather(double lat, double lon) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("weather-intelligence1.p.rapidapi.com")
                        .path("/current")
                        .queryParam("lat", lat)
                        .queryParam("lon", lon)
                        .build())
                .header("x-rapidapi-key", apiKey)
                .header("x-rapidapi-host", weatherHost)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(response -> {
                    ApiResult result = new ApiResult();
                    result.setType("weather");
                    result.setQuery("lat=" + lat + ", lon=" + lon);
                    result.setResponse(response);
                    return repository.save(result).thenReturn(response);
                })
                .onErrorResume(e -> Mono.just("Error Weather: " + e.getMessage()));
    }
}