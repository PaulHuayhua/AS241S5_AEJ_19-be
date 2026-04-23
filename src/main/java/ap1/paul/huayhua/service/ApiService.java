package ap1.paul.huayhua.service;

import reactor.core.publisher.Mono;

public interface ApiService {

    Mono<String> chat(String message);

    Mono<String> getWeather(double lat, double lon);
}