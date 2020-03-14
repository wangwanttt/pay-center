package com.songlanyun.pay.dao;

import com.songlanyun.pay.domain.Project;
import com.songlanyun.pay.domain.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;


@Repository
public interface UserRepository extends ReactiveMongoRepository<User, Long> {
    Mono<User> findByNameAndPassword(String name, String psw);
}
