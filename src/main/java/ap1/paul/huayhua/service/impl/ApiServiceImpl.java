package ap1.paul.huayhua.service.impl;

import ap1.paul.huayhua.model.ApiResult;
import ap1.paul.huayhua.repository.ApiResultRepository;
import ap1.paul.huayhua.service.ApiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ApiServiceImpl implements ApiService {

    @Value("${groq.api.model}")
    private String groqModel;

    private final ApiResultRepository repository;
    private final WebClient groqWebClient;
    private final WebClient weatherWebClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public ApiServiceImpl(
            ApiResultRepository repository,
            @Qualifier("groqWebClient") WebClient groqWebClient,
            @Qualifier("weatherWebClient") WebClient weatherWebClient,
            ObjectMapper objectMapper) {
        this.repository = repository;
        this.groqWebClient = groqWebClient;
        this.weatherWebClient = weatherWebClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<String> chat(String message) {
        log.info("Groq Chat - Message: {}", message);
        
        Map<String, Object> requestBody = Map.of(
            "model", groqModel,
            "messages", List.of(
                Map.of(
                    "role", "user",
                    "content", message
                )
            ),
            "temperature", 1,
            "max_tokens", 1024,
            "top_p", 1,
            "stream", false
        );

        return groqWebClient.post()
                .uri("/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .flatMap(response -> {
                    log.info("Groq response received");
                    
                    String content = extractContent(response);
                    Integer tokensUsed = extractTokensUsed(response);
                    
                    Map<String, Object> chatResponse = new HashMap<>();
                    chatResponse.put("content", content);
                    chatResponse.put("model", groqModel);
                    chatResponse.put("tokens_used", tokensUsed);
                    
                    Map<String, Object> queryMap = new HashMap<>();
                    queryMap.put("message", message);
                    
                    ApiResult result = new ApiResult();
                    result.setType("groq-chat");
                    result.setQuery(queryMap);
                    result.setResponse(chatResponse);
                    result.setCreatedAt(LocalDateTime.now());
                    result.setUpdatedAt(LocalDateTime.now());
                    
                    return repository.save(result)
                            .doOnSuccess(saved -> log.info("Saved to MongoDB with ID: {}", saved.getId()))
                            .doOnError(error -> log.error("Error saving to MongoDB: {}", error.getMessage()))
                            .flatMap(saved -> Mono.fromCallable(() -> objectMapper.writeValueAsString(chatResponse)));
                })
                .onErrorResume(e -> {
                    log.error("Error Groq Chat: {}", e.getMessage());
                    return Mono.just("{\"error\": \"" + e.getMessage() + "\"}");
                });
    }

    private String extractContent(Map<String, Object> response) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices != null && !choices.isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                if (message != null) {
                    return (String) message.get("content");
                }
            }
        } catch (Exception e) {
            log.error("Error extracting content: {}", e.getMessage());
        }
        return "No content available";
    }

    private Integer extractTokensUsed(Map<String, Object> response) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> usage = (Map<String, Object>) response.get("usage");
            if (usage != null) {
                return (Integer) usage.get("total_tokens");
            }
        } catch (Exception e) {
            log.error("Error extracting tokens: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public Mono<String> getWeather(double lat, double lon) {
        log.info("Getting weather for lat: {}, lon: {}", lat, lon);
        return weatherWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/current")
                        .queryParam("lat", lat)
                        .queryParam("lon", lon)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .flatMap(rawResponse -> {
                    log.info("Weather response received: {}", rawResponse);
                    
                    Map<String, Object> queryMap = new HashMap<>();
                    queryMap.put("latitude", lat);
                    queryMap.put("longitude", lon);
                    
                    ApiResult result = new ApiResult();
                    result.setType("weather");
                    result.setQuery(queryMap);
                    result.setResponse(rawResponse);
                    result.setCreatedAt(LocalDateTime.now());
                    result.setUpdatedAt(LocalDateTime.now());
                    
                    return repository.save(result)
                            .doOnSuccess(saved -> log.info("Saved weather to MongoDB with ID: {}", saved.getId()))
                            .doOnError(error -> log.error("Error saving weather to MongoDB: {}", error.getMessage()))
                            .flatMap(saved -> Mono.fromCallable(() -> objectMapper.writeValueAsString(rawResponse)));
                })
                .onErrorResume(e -> {
                    log.error("Error Weather: {}", e.getMessage(), e);
                    return Mono.just("{\"error\": \"" + e.getMessage() + "\"}");
                });
    }
}