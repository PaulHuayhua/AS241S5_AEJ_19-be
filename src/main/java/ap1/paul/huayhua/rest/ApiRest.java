package ap1.paul.huayhua.rest;

import ap1.paul.huayhua.model.ApiResult;
import ap1.paul.huayhua.model.ChatRequest;
import ap1.paul.huayhua.service.ApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

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

    @GetMapping("/test-groq")
    @Operation(summary = "Test Groq Connection", description = "Prueba la conexión con Groq API")
    public Mono<String> testGroq() {
        log.info("Endpoint /test-groq - Probando conexión con Groq");
        return service.chat("Hola, responde solo con 'Conexión exitosa'");
    }

    @PostMapping("/chat")
    @Operation(summary = "Groq Chat", description = "Chat con IA usando Groq (Llama 3.3)")
    public Mono<String> chat(
            @Parameter(description = "Mensaje para el chat", required = true)
            @RequestBody ChatRequest request) {
        log.info("Endpoint /chat - Mensaje: {}", request.getMessage());
        return service.chat(request.getMessage());
    }

    @GetMapping("/chat")
    @Operation(summary = "Groq Chat (GET)", description = "Chat con IA usando Groq (Llama 3.3) via query param")
    public Mono<String> chatGet(
            @Parameter(description = "Mensaje para el chat", required = true, example = "Explica qué es la IA")
            @RequestParam String message) {
        log.info("Endpoint /chat (GET) - Mensaje: {}", message);
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

    @GetMapping("/results")
    @Operation(summary = "Listar consultas", description = "Obtiene todas las consultas registradas (no eliminadas)")
    public Flux<ApiResult> getAllResults() {
        log.info("Endpoint /results - Obteniendo todos los resultados");
        return service.getAllResults();
    }

    @GetMapping("/results/{id}")
    @Operation(summary = "Obtener consulta por ID", description = "Obtiene una consulta específica por su ID")
    public Mono<ApiResult> getResultById(
            @Parameter(description = "ID de la consulta", required = true)
            @PathVariable String id) {
        log.info("Endpoint /results/{} - Obteniendo resultado por ID", id);
        return service.getResultById(id);
    }

    @PutMapping("/results/{id}")
    @Operation(summary = "Re-ejecutar consulta", description = "Re-ejecuta la consulta a la API de IA y actualiza el resultado")
    public Mono<ApiResult> updateResult(
            @Parameter(description = "ID de la consulta", required = true)
            @PathVariable String id) {
        log.info("Endpoint /results/{} - Re-ejecutando consulta", id);
        return service.updateResult(id);
    }

    @DeleteMapping("/results/{id}")
    @Operation(summary = "Eliminar consulta (lógico)", description = "Realiza un borrado lógico de una consulta")
    public Mono<Void> deleteResult(
            @Parameter(description = "ID de la consulta", required = true)
            @PathVariable String id) {
        log.info("Endpoint /results/{} - Eliminando resultado (lógico)", id);
        return service.deleteResult(id);
    }

    @PutMapping("/results/restore/{id}")
    @Operation(summary = "Restaurar consulta", description = "Restaura una consulta eliminada lógicamente")
    public Mono<Void> restoreResult(
            @Parameter(description = "ID de la consulta", required = true)
            @PathVariable String id) {
        log.info("Endpoint PUT /results/restore/{} - Restaurando resultado", id);
        return service.restoreResult(id);
    }

    @PutMapping("/results/modify/{id}")
    @Operation(summary = "Modificar y ejecutar consulta", description = "Modifica los parámetros de una consulta y la re-ejecuta")
    public Mono<ApiResult> modifyAndExecute(
            @Parameter(description = "ID de la consulta", required = true)
            @PathVariable String id,
            @Parameter(description = "Nuevos parámetros de consulta", required = true)
            @RequestBody Map<String, Object> newQuery) {
        log.info("Endpoint PUT /results/modify/{} - Modificando consulta con nuevos parámetros: {}", id, newQuery);
        return service.modifyAndExecute(id, newQuery);
    }

    @DeleteMapping("/results/cleanup/corrupted")
    @Operation(summary = "Limpiar datos corruptos", description = "Elimina documentos con datos corruptos de la base de datos")
    public Mono<String> cleanupCorruptedData() {
        log.info("Endpoint /results/cleanup/corrupted - Limpiando datos corruptos");
        return service.getAllResults()
                .filter(result -> {
                    try {
                        // Intentar acceder a response para ver si causa error
                        result.getResponse();
                        return false; // No está corrupto
                    } catch (Exception e) {
                        log.warn("Documento corrupto encontrado: {}", result.getId());
                        return true; // Está corrupto
                    }
                })
                .flatMap(corrupted -> service.deleteResult(corrupted.getId()).thenReturn(corrupted.getId()))
                .collectList()
                .map(deletedIds -> {
                    if (deletedIds.isEmpty()) {
                        return "No se encontraron documentos corruptos";
                    }
                    return "Documentos corruptos eliminados: " + deletedIds.size() + " - IDs: " + String.join(", ", deletedIds);
                });
    }
}