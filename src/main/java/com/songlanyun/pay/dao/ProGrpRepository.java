package com.songlanyun.pay.dao;

import com.songlanyun.pay.domain.PrjGrp;
import com.songlanyun.pay.domain.Project;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ProGrpRepository extends ReactiveMongoRepository<PrjGrp, String> {
   Mono<Project> findByName(String name);
}
