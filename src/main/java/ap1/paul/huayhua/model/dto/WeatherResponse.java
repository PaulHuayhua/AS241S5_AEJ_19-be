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
public class WeatherResponse {
    
    @JsonProperty("latitude")
    private Double latitude;
    
    @JsonProperty("longitude")
    private Double longitude;
    
    @JsonProperty("timezone")
    private String timezone;
    
    @JsonProperty("temperature_c")
    private Double temperatureC;
    
    @JsonProperty("condition")
    private String condition;
    
    @JsonProperty("weather_code")
    private Integer weatherCode;
    
    @JsonProperty("wind_speed_kmh")
    private Double windSpeedKmh;
    
    @JsonProperty("relative_humidity_percent")
    private Integer relativeHumidityPercent;
    
    @JsonProperty("is_day")
    private Boolean isDay;
}
