package ap1.paul.huayhua.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "results")
public class ApiResult {

    @Id
    private String id;

    private String type;     // scraper | weather
    private String query;    // url o lat/lon
    private String response; // respuesta API
}