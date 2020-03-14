package com.songlanyun.pay.dao;

import com.songlanyun.pay.domain.Project;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ProjectRepository extends ReactiveMongoRepository<Project, String> {
   /** 某项目id ,支付方式，支付类型  **/
   Mono<Project> findByNameAndPayTypeAndAndPayWay(String name,int payType,int payWay);
}
