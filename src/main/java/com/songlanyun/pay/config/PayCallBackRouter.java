package com.songlanyun.pay.config;

import com.songlanyun.pay.handler.PayCallBackHandler;
import com.songlanyun.pay.handler.PayHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class PayCallBackRouter {
    static final String API_BASE_URL = "/api/v1/alipay/";
    @Bean
    public RouterFunction<ServerResponse> routePayCallBack(PayCallBackHandler  payCallBackHandler) {
        return RouterFunctions
                .route(RequestPredicates.GET(API_BASE_URL+"getPayResult")
                                .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                        payCallBackHandler::getPayResult)

                .andRoute(RequestPredicates.POST(API_BASE_URL+"aliPayReturn")
                                .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                        payCallBackHandler::aliPayReturn)     //支付宝支付同步通知返回地址
                .andRoute(RequestPredicates.POST(API_BASE_URL+"aliPayNotify"),
                        payCallBackHandler::aliPayNotify)  ;  //支付宝支付结果异步通知
    }
}

