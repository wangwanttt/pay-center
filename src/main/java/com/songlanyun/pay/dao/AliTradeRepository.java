package com.songlanyun.pay.dao;

import com.songlanyun.pay.domain.AliTrade;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface AliTradeRepository extends ReactiveMongoRepository<AliTrade, String> {
   Mono<AliTrade> findByTradeNo(String orderNo);


}
