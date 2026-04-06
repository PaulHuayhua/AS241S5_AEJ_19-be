package ap1.paul.huayhua.repository;

import ap1.paul.huayhua.model.ApiResult;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ApiResultRepository extends ReactiveMongoRepository<ApiResult, String> {
}