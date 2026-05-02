package ap1.paul.huayhua.service;

import ap1.paul.huayhua.model.ApiResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface ApiService {

    Mono<String> chat(String message);

    Mono<String> getWeather(double lat, double lon);

    Flux<ApiResult> getAllResults();

    Mono<ApiResult> getResultById(String id);

    Mono<ApiResult> updateResult(String id);

    Mono<Void> deleteResult(String id);

    Mono<Void> restoreResult(String id);

    Mono<ApiResult> modifyAndExecute(String id, Map<String, Object> newQuery);
}