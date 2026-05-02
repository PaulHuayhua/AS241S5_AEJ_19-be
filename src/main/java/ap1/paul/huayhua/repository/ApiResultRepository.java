package ap1.paul.huayhua.repository;

import ap1.paul.huayhua.model.ApiResult;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface ApiResultRepository extends ReactiveMongoRepository<ApiResult, String> {
    Flux<ApiResult> findByDeletedFalse();
}