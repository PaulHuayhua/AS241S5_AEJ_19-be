package ap1.paul.huayhua.rest;

import ap1.paul.huayhua.service.ApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api")
@Tag(name = "API Services", description = "Endpoints para servicios de IA (Groq Chat y Weather)")
public class ApiRest {

    private final ApiService service;

    @Autowired
    public ApiRest(ApiService service) {
        this.service = service;
    }

    @PostMapping("/chat")
    @Operation(summary = "Groq Chat", description = "Chat con IA usando Groq (Llama 3.3)")
    public Mono<String> chat(
            @Parameter(description = "Mensaje para el chat", required = true)
            @RequestBody ap1.paul.huayhua.model.ChatRequest request) {
        log.info("Endpoint /chat - Message: {}", request.getMessage());
        return service.chat(request.getMessage());
    }

    @GetMapping("/chat")
    @Operation(summary = "Groq Chat (GET)", description = "Chat con IA usando Groq (Llama 3.3) via query param")
    public Mono<String> chatGet(
            @Parameter(description = "Mensaje para el chat", required = true, example = "Explica qué es la IA")
            @RequestParam String message) {
        log.info("Endpoint /chat (GET) - Message: {}", message);
        return service.chat(message);
    }

    @GetMapping("/weather")
    @Operation(summary = "Weather API", description = "Obtiene información del clima por coordenadas")
    public Mono<String> weather(
            @Parameter(description = "Latitud", required = true, example = "-12.0464")
            @RequestParam double lat,
            @Parameter(description = "Longitud", required = true, example = "-77.0428")
            @RequestParam double lon) {
        log.info("Endpoint /weather - lat: {}, lon: {}", lat, lon);
        return service.getWeather(lat, lon);
    }
}