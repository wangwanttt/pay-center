package com.songlanyun.pay.handler;

import com.songlanyun.pay.dao.AliTradeRepository;
import com.songlanyun.pay.dao.ProjectRepository;
import com.songlanyun.pay.domain.AliTrade;
import com.songlanyun.pay.domain.Project;
import com.songlanyun.pay.error.GlobalException;
import com.songlanyun.pay.payment.bean.PayBean;
import com.songlanyun.pay.payment.common.api.ApiResult;
import com.songlanyun.pay.payment.service.PayService;
import com.songlanyun.pay.utils.PageQuery;
import com.songlanyun.pay.utils.PageSupport;
import com.songlanyun.pay.utils.ResponseInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Component
@Slf4j
public class PayHandler {

    @Autowired
    private ReactiveMongoTemplate reactiveMongoTemplate;
    @Autowired
    private PayService payService;

    @Autowired
    private final ProjectRepository projectRepository;

    @Autowired
    private final AliTradeRepository aliTradeRepository;

    public PayHandler(ProjectRepository projectRepository, AliTradeRepository aliTradeRepository) {
        this.projectRepository = projectRepository;
        this.aliTradeRepository = aliTradeRepository;
    }

    /**
     * 根据订单号得到订单记录
     * 参数 id 订单号
     **/
    public Mono<ServerResponse> getOrderById(ServerRequest request) {
        String id = request.pathVariable("id");
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(ResponseInfo.ok(aliTradeRepository.findByTradeNo(id)), ResponseInfo.class);
    }


    public Mono<ServerResponse> pay(ServerRequest request) {
        Mono fallback = Mono.error(new GlobalException(-200, "支付失败 "));
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();
        Mono<PayBean> payParam = request.bodyToMono(PayBean.class);
        return payParam.flatMap(payBean -> {
            Optional<InetSocketAddress> address = request.remoteAddress();
            log.info(address.get().getAddress().toString());
            payBean.setIp(address.get().getAddress().toString());
            Mono<Project> prj = projectRepository.findById(payBean.getPrjId()).switchIfEmpty(fallback);
            return prj.flatMap(project -> {
                ApiResult apiResult = null;
                try {
                    apiResult = payService.createPayOrder(payBean, project);
                } catch (Exception e) {
                    return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(ResponseInfo.ok(fallback), ResponseInfo.class);
                }
                return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(Mono.just(apiResult), ApiResult.class);
            }).onErrorResume(e -> {
                Object err = e;
                log.info("-----失败----");
                return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(ResponseInfo.ok(fallback), ResponseInfo.class);
            });
        });
    }



    /**
     * 得到订单分页列表列表
     * request 参数为 page:xx  size :xx
     **/
    public Mono<ServerResponse> getTradeList(ServerRequest request) {
        return request.bodyToMono(PageQuery.class).flatMap(pageQuery -> {
            return tradePageQuery(pageQuery);
        });

    }
    /**
     * 订单分页列表的具体实现
     **/
    private Mono<ServerResponse> tradePageQuery(PageQuery pageQuery) {
        Query query = getQuery(pageQuery);
        String[] strArray = {"gmtCreate"};
        pageQuery.setOrder(strArray);
        Pageable pageable = getPageable(pageQuery);

        Query with = query.with(pageable);
        // with.addCriteria()       query.with(new Sort(new Sort.Order(Sort.Direction.DESC, "update_date")));
        //得到总记录数  Flux<Person> flux = template.find(Query.query(Criteria.where("lastname").is("White")), Person.class);
        Mono<Long> count = reactiveMongoTemplate.count(new Query(), AliTrade.class);
        return count.flatMap(sums -> {
            long size = pageQuery.getPage() * pageQuery.getSize();
            if (sums.longValue() == size) {
                return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(ResponseInfo.info(-200, "无更多数据"), ResponseInfo.class);
            }
            //获取数据
            return reactiveMongoTemplate.find(with, AliTrade.class).collectList()
                    .map(list -> new PageSupport(list, pageQuery.getPage(), pageQuery.getSize(), sums)).flatMap(pageSupportVo -> {
                        return ServerResponse.ok().body(ResponseInfo.pageOk(pageSupportVo), ResponseInfo.class);
                    }).switchIfEmpty(ServerResponse.ok().body(Mono.just(ResponseInfo.info(-200, "分页查询参数不能为null")), ResponseInfo.class));

        });
    }


    /**
     * 得到分页对象
     **/
    protected Pageable getPageable(PageQuery pageQuery) {
        //排序条件
        if (ArrayUtils.isEmpty(pageQuery.getOrder())) {
            String[] order = {"_id"};
            pageQuery.setOrder(order);
        }
        Sort sort = Sort.by(Sort.Direction.DESC, pageQuery.getOrder());
        //分页
        return PageRequest.of(pageQuery.getPage(), pageQuery.getSize(), sort);

    }

    /**
     * 得到分页的查询条件
     **/
    protected Query getQuery(PageQuery pageQuery) {
        Query query = new Query();

        Pageable pageable = getPageable(pageQuery);
        return query.with(pageable);
    }

    /**
     * 分页的订单列表

     public Mono<PageSupport<AliTrade>> getOrderPage(Pageable page) {
     return aliTradeRepository.findAll()
     .collectList()
     .map(list -> new PageSupport(
     list
     .stream()
     .skip(page.getPageNumber() * page.getPageSize())
     .limit(page.getPageSize())
     .collect(Collectors.toList()),
     page.getPageNumber(), page.getPageSize(), list.size()));
     }  **/




}

