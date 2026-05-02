package ap1.paul.huayhua.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "api_results")
public class ApiResult {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Id
    private String id;

    @Field("type")
    private String type;

    @Field("query")
    private Map<String, Object> query;

    @Field("response")
    private Object responseRaw; // Cambiado temporalmente para manejar ambos tipos

    @Field("created_at")
    @JsonProperty("created_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Field("updated_at")
    @JsonProperty("updated_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @Field("deleted")
    private Boolean deleted = false;

    // Getter personalizado para response
    @JsonProperty("response")
    public Map<String, Object> getResponse() {
        if (responseRaw == null) {
            return new HashMap<>();
        }
        
        if (responseRaw instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) responseRaw;
            return map;
        }
        
        if (responseRaw instanceof String) {
            try {
                return objectMapper.readValue((String) responseRaw, new TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {
                log.error("Error al parsear response string: {}", e.getMessage());
                Map<String, Object> errorMap = new HashMap<>();
                errorMap.put("error", "Error al parsear respuesta");
                errorMap.put("raw", responseRaw);
                return errorMap;
            }
        }
        
        return new HashMap<>();
    }

    // Setter personalizado para response
    public void setResponse(Map<String, Object> response) {
        this.responseRaw = response;
    }
}