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
import reactor.core.publisher.Flux;
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
        log.info("Groq Chat - Mensaje: {}", message);
        
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
                    log.info("Respuesta de Groq recibida");
                    
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
                    result.setDeleted(false);
                    
                    return repository.save(result)
                            .doOnSuccess(saved -> log.info("Guardado en MongoDB con ID: {}", saved.getId()))
                            .doOnError(error -> log.error("Error al guardar en MongoDB: {}", error.getMessage()))
                            .flatMap(saved -> Mono.fromCallable(() -> objectMapper.writeValueAsString(chatResponse)));
                })
                .onErrorResume(e -> {
                    log.error("Error en Groq Chat: {}", e.getMessage());
                    
                    // Crear respuesta de error más informativa
                    Map<String, Object> errorResponse = new HashMap<>();
                    if (e.getMessage().contains("401")) {
                        errorResponse.put("error", "Error de autenticación con Groq API");
                        errorResponse.put("message", "La API key de Groq no es válida o ha expirado");
                        errorResponse.put("suggestion", "Verifica tu API key de Groq en la configuración");
                    } else if (e.getMessage().contains("429")) {
                        errorResponse.put("error", "Límite de rate excedido");
                        errorResponse.put("message", "Has excedido el límite de solicitudes por minuto");
                        errorResponse.put("suggestion", "Espera un momento antes de hacer otra consulta");
                    } else {
                        errorResponse.put("error", "Error de conexión con Groq API");
                        errorResponse.put("message", e.getMessage());
                        errorResponse.put("suggestion", "Verifica tu conexión a internet y la configuración de la API");
                    }
                    
                    return Mono.fromCallable(() -> objectMapper.writeValueAsString(errorResponse));
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
            log.error("Error al extraer contenido: {}", e.getMessage());
        }
        return "Contenido no disponible";
    }

    private Integer extractTokensUsed(Map<String, Object> response) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> usage = (Map<String, Object>) response.get("usage");
            if (usage != null) {
                return (Integer) usage.get("total_tokens");
            }
        } catch (Exception e) {
            log.error("Error al extraer tokens: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public Mono<String> getWeather(double lat, double lon) {
        log.info("Obteniendo clima para lat: {}, lon: {}", lat, lon);
        return weatherWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/current")
                        .queryParam("lat", lat)
                        .queryParam("lon", lon)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .flatMap(rawResponse -> {
                    log.info("Respuesta de clima recibida: {}", rawResponse);
                    
                    Map<String, Object> queryMap = new HashMap<>();
                    queryMap.put("latitude", lat);
                    queryMap.put("longitude", lon);
                    
                    ApiResult result = new ApiResult();
                    result.setType("weather");
                    result.setQuery(queryMap);
                    result.setResponse(rawResponse);
                    result.setCreatedAt(LocalDateTime.now());
                    result.setUpdatedAt(LocalDateTime.now());
                    result.setDeleted(false);
                    
                    return repository.save(result)
                            .doOnSuccess(saved -> log.info("Clima guardado en MongoDB con ID: {}", saved.getId()))
                            .doOnError(error -> log.error("Error al guardar clima en MongoDB: {}", error.getMessage()))
                            .flatMap(saved -> Mono.fromCallable(() -> objectMapper.writeValueAsString(rawResponse)));
                })
                .onErrorResume(e -> {
                    log.error("Error en Weather: {}", e.getMessage(), e);
                    return Mono.just("{\"error\": \"" + e.getMessage() + "\"}");
                });
    }

    @Override
    public Flux<ApiResult> getAllResults() {
        log.info("Obteniendo todos los resultados (incluyendo eliminados)");
        return repository.findAll()
                .onErrorResume(e -> {
                    log.error("Error al obtener resultados de MongoDB: {}", e.getMessage(), e);
                    return Flux.empty();
                })
                .collectList()
                .flatMapMany(list -> {
                    // Ordenar en memoria
                    list.sort((a, b) -> {
                        // Primero ordenar por estado de eliminado (no eliminados primero)
                        if (a.getDeleted() != b.getDeleted()) {
                            return a.getDeleted() ? 1 : -1;
                        }
                        // Luego ordenar por fecha de creación (más recientes primero)
                        return b.getCreatedAt().compareTo(a.getCreatedAt());
                    });
                    return Flux.fromIterable(list);
                });
    }

    @Override
    public Mono<ApiResult> getResultById(String id) {
        log.info("Obteniendo resultado por ID: {}", id);
        return repository.findById(id)
                .filter(result -> !result.getDeleted())
                .switchIfEmpty(Mono.error(new RuntimeException("Resultado no encontrado o eliminado")));
    }

    @Override
    public Mono<ApiResult> updateResult(String id) {
        log.info("Re-ejecutando consulta para resultado con ID: {}", id);
        return repository.findById(id)
                .filter(result -> !result.getDeleted())
                .flatMap(existing -> {
                    String type = existing.getType();
                    Map<String, Object> query = existing.getQuery();
                    
                    if ("groq-chat".equals(type)) {
                        String message = (String) query.get("message");
                        return reExecuteChat(message, existing);
                    } else if ("weather".equals(type)) {
                        Double lat = getDoubleValue(query.get("latitude"));
                        Double lon = getDoubleValue(query.get("longitude"));
                        return reExecuteWeather(lat, lon, existing);
                    } else {
                        return Mono.error(new RuntimeException("Tipo desconocido: " + type));
                    }
                })
                .switchIfEmpty(Mono.error(new RuntimeException("Resultado no encontrado o eliminado")));
    }

    private Double getDoubleValue(Object value) {
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        } else if (value instanceof String) {
            return Double.parseDouble((String) value);
        }
        return 0.0;
    }

    private Mono<ApiResult> reExecuteChat(String message, ApiResult existing) {
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
                    String content = extractContent(response);
                    Integer tokensUsed = extractTokensUsed(response);
                    
                    Map<String, Object> chatResponse = new HashMap<>();
                    chatResponse.put("content", content);
                    chatResponse.put("model", groqModel);
                    chatResponse.put("tokens_used", tokensUsed);
                    
                    existing.setResponse(chatResponse);
                    existing.setUpdatedAt(LocalDateTime.now());
                    
                    return repository.save(existing);
                })
                .onErrorResume(e -> {
                    log.error("Error al re-ejecutar chat: {}", e.getMessage());
                    String errorMsg = "Error al re-ejecutar chat: ";
                    if (e.getMessage().contains("401")) {
                        errorMsg += "API key de Groq no válida o expirada";
                    } else if (e.getMessage().contains("429")) {
                        errorMsg += "Límite de rate excedido, intenta más tarde";
                    } else {
                        errorMsg += e.getMessage();
                    }
                    return Mono.error(new RuntimeException(errorMsg));
                });
    }

    private Mono<ApiResult> reExecuteWeather(double lat, double lon, ApiResult existing) {
        return weatherWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/current")
                        .queryParam("lat", lat)
                        .queryParam("lon", lon)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .flatMap(rawResponse -> {
                    existing.setResponse(rawResponse);
                    existing.setUpdatedAt(LocalDateTime.now());
                    
                    return repository.save(existing);
                })
                .onErrorResume(e -> {
                    log.error("Error al re-ejecutar clima: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Error al re-ejecutar clima: " + e.getMessage()));
                });
    }

    @Override
    public Mono<Void> deleteResult(String id) {
        log.info("Eliminación lógica de resultado con ID: {}", id);
        return repository.findById(id)
                .filter(result -> !result.getDeleted())
                .flatMap(result -> {
                    result.setDeleted(true);
                    result.setUpdatedAt(LocalDateTime.now());
                    return repository.save(result);
                })
                .switchIfEmpty(Mono.error(new RuntimeException("Resultado no encontrado o ya eliminado")))
                .then();
    }

    @Override
    public Mono<Void> restoreResult(String id) {
        log.info("Restauración de resultado con ID: {}", id);
        return repository.findById(id)
                .doOnNext(result -> log.info("Resultado encontrado: ID={}, deleted={}", result.getId(), result.getDeleted()))
                .filter(result -> result.getDeleted() != null && result.getDeleted())
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Resultado no encontrado o no está eliminado: {}", id);
                    return Mono.error(new RuntimeException("Resultado no encontrado o no está eliminado"));
                }))
                .flatMap(result -> {
                    log.info("Restaurando resultado: {}", result.getId());
                    result.setDeleted(false);
                    result.setUpdatedAt(LocalDateTime.now());
                    return repository.save(result);
                })
                .doOnSuccess(saved -> log.info("Resultado restaurado exitosamente: {}", id))
                .doOnError(error -> log.error("Error al restaurar resultado {}: {}", id, error.getMessage()))
                .then();
    }

    @Override
    public Mono<ApiResult> modifyAndExecute(String id, Map<String, Object> newQuery) {
        log.info("Modificando y re-ejecutando consulta con ID: {}", id);
        return repository.findById(id)
                .filter(result -> !result.getDeleted())
                .switchIfEmpty(Mono.error(new RuntimeException("Resultado no encontrado o eliminado")))
                .flatMap(existing -> {
                    String type = existing.getType();
                    
                    if ("groq-chat".equals(type)) {
                        String message = (String) newQuery.get("message");
                        if (message == null || message.trim().isEmpty()) {
                            return Mono.error(new RuntimeException("El mensaje no puede estar vacío"));
                        }
                        return reExecuteChatWithNewParams(message, existing);
                    } else if ("weather".equals(type)) {
                        Double lat = getDoubleValue(newQuery.get("latitude"));
                        Double lon = getDoubleValue(newQuery.get("longitude"));
                        if (lat == null || lon == null) {
                            return Mono.error(new RuntimeException("Latitud y longitud son requeridas"));
                        }
                        return reExecuteWeatherWithNewParams(lat, lon, existing);
                    } else {
                        return Mono.error(new RuntimeException("Tipo desconocido: " + type));
                    }
                });
    }

    private Mono<ApiResult> reExecuteChatWithNewParams(String message, ApiResult existing) {
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
                    String content = extractContent(response);
                    Integer tokensUsed = extractTokensUsed(response);
                    
                    Map<String, Object> chatResponse = new HashMap<>();
                    chatResponse.put("content", content);
                    chatResponse.put("model", groqModel);
                    chatResponse.put("tokens_used", tokensUsed);
                    
                    Map<String, Object> queryMap = new HashMap<>();
                    queryMap.put("message", message);
                    
                    existing.setQuery(queryMap);
                    existing.setResponse(chatResponse);
                    existing.setUpdatedAt(LocalDateTime.now());
                    
                    return repository.save(existing);
                })
                .onErrorResume(e -> {
                    log.error("Error al re-ejecutar chat con nuevos parámetros: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Error al ejecutar consulta: " + e.getMessage()));
                });
    }

    private Mono<ApiResult> reExecuteWeatherWithNewParams(double lat, double lon, ApiResult existing) {
        return weatherWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/current")
                        .queryParam("lat", lat)
                        .queryParam("lon", lon)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .flatMap(rawResponse -> {
                    Map<String, Object> queryMap = new HashMap<>();
                    queryMap.put("latitude", lat);
                    queryMap.put("longitude", lon);
                    
                    existing.setQuery(queryMap);
                    existing.setResponse(rawResponse);
                    existing.setUpdatedAt(LocalDateTime.now());
                    
                    return repository.save(existing);
                })
                .onErrorResume(e -> {
                    log.error("Error al re-ejecutar clima con nuevos parámetros: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Error al ejecutar consulta: " + e.getMessage()));
                });
    }
}