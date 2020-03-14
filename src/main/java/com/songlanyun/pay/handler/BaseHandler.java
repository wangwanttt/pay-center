package com.songlanyun.pay.handler;

import com.songlanyun.pay.utils.PageQuery;
import com.songlanyun.pay.utils.ResponseInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.ParameterizedType;

@Slf4j
public abstract class BaseHandler<Repository extends ReactiveMongoRepository, Entity> {
    @Autowired(required = false)
    private Repository repository;
    /**
     * mongo模板
     */
    @Autowired
    private ReactiveMongoTemplate reactiveMongoTemplate;
    /**
     * 当前EntityClass
     */
    private final Class<Entity> entityClass = (Class<Entity>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
    private ServerRequest request;

    /**
     * 新增
     *
     * @param request
     * @return
     */
    public Mono<ServerResponse> add(ServerRequest request) {
        //获取参数
        Mono<Entity> entityMono = request.bodyToMono(entityClass);
        return entityMono.flatMap(entity -> {
            //新增
            return ServerResponse.ok().body(repository.save(entity), entityClass);
        })
                //参数为null
                .switchIfEmpty(ServerResponse.ok().body(Mono.just(ResponseInfo.info(-200,"新增 参数不能为null")), ResponseInfo.class));
    }

    /**
     * 查找所有
     *
     * @param request
     * @return
     */
    public Mono<ServerResponse> queryAll(ServerRequest request) {
        Mono<Long> count = repository.count();
        return count.flatMap(sums -> {
            if (sums.longValue() == 0) {
                return ServerResponse.ok().body(Mono.just(ResponseInfo.info(-200,"无数据")), ResponseInfo.class);
            }
            Flux entityFlux = repository.findAll(Sort.by(Sort.Direction.ASC, "_id"));
            return ServerResponse.ok().body(entityFlux, entityClass);
        });
    }



    /**
     * 分页条件封装
     *
     * @param pageQuery
     * @return
     */
    protected Pageable getPageable(PageQuery pageQuery) {
        //排序条件
        if (ArrayUtils.isEmpty(pageQuery.getOrder())) {
            String[] order = {"_id"};
            pageQuery.setOrder(order);
        }
        Sort sort =   Sort.by(Sort.Direction.DESC, pageQuery.getOrder());
        //分页
        return PageRequest.of(pageQuery.getPage(), pageQuery.getSize(), sort);
    }

    /**
     * 分页查询条件封装
     *
     * @param pageQuery
     * @return
     */
    protected Query getQuery(Object pageQuery) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        query.addCriteria(criteria);
        return query;
    }



    /**
     * 获取当前Entity对象
     *
     * @return
     */
    private Entity instance() {
        try {
            return entityClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
