package com.songlanyun.pay.config;

import com.songlanyun.pay.handler.UserHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class UserRouter {
    static final String API_BASE_URL = "/api/v1/";
    @Bean
    public RouterFunction<ServerResponse> routeCity(UserHandler userHandler) {
        return RouterFunctions
                .route(RequestPredicates.GET(API_BASE_URL+"listUser")
                                .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                        userHandler::listUser)
                .andRoute(RequestPredicates.GET(API_BASE_URL+"user/{id}")
                                .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                        userHandler::getUser)
                .andRoute(RequestPredicates.GET(API_BASE_URL+"deleteUser/{id}")
                                .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                        userHandler::deleteUser)
                .andRoute(RequestPredicates.POST(API_BASE_URL+"login")
                                .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                        userHandler::findUser)
                .andRoute(RequestPredicates.POST(API_BASE_URL+"saveUser")
                                .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                        userHandler::saveUser);
    }
}