package ap1.paul.huayhua.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("content")
    private String content;
    
    @JsonProperty("model")
    private String model;
    
    @JsonProperty("tokens_used")
    private Integer tokensUsed;
}
