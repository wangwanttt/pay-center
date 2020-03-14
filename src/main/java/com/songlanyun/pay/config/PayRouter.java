package com.songlanyun.pay.config;

import com.songlanyun.pay.handler.PayHandler;
import com.songlanyun.pay.handler.UserHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class PayRouter {
    static final String API_BASE_URL = "/api/v1/";

    @Bean
    public RouterFunction<ServerResponse> routePay(PayHandler payHandler) {
        return RouterFunctions
                .route(RequestPredicates.POST(API_BASE_URL + "pay")
                        .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), payHandler::pay)
                .andRoute(RequestPredicates.GET(API_BASE_URL + "getOrder/{id}")
                                .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                        payHandler::getOrderById)
                .andRoute(RequestPredicates.GET(API_BASE_URL + "getAliOrder/{id}")
                                .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                        payHandler::getTradeList)
                .andRoute(RequestPredicates.POST(API_BASE_URL + "getOrderList")
                                .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                        payHandler::getTradeList);

    }
}