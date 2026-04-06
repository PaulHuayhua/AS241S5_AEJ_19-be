package ap1.paul.huayhua.service;

import reactor.core.publisher.Mono;

public interface ApiService {

    Mono<String> scrape(String url);

    Mono<String> getWeather(double lat, double lon);
}