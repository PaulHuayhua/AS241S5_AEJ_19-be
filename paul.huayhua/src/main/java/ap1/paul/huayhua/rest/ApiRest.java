package ap1.paul.huayhua.rest;

import ap1.paul.huayhua.service.ApiService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class ApiRest {

    private final ApiService service;

    public ApiRest(ApiService service) {
        this.service = service;
    }

    @GetMapping("/scraper")
    public Mono<String> scraper(@RequestParam String url) {
        System.out.println(">>> URL recibida: " + url);
        return service.scrape(url);
    }

    @GetMapping("/weather")
    public Mono<String> weather(
            @RequestParam double lat,
            @RequestParam double lon) {
        return service.getWeather(lat, lon);
    }
}