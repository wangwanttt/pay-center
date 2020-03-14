package com.songlanyun.pay.config;

import com.songlanyun.pay.handler.ProjectHandler;
import com.songlanyun.pay.handler.UserHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class ProjectRouter {
    static final String API_BASE_URL = "/api/v1/prj/";
    @Bean
    public RouterFunction<ServerResponse> routePrj(ProjectHandler projectHandler) {
        return RouterFunctions
                .route(RequestPredicates.GET(API_BASE_URL+"list")
                                .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                        projectHandler::list)
                .andRoute(RequestPredicates.GET(API_BASE_URL+"getByid/{id}")
                                .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                        projectHandler::getProjectById)

                .andRoute(RequestPredicates.DELETE(API_BASE_URL+"delete/{id}")
                                .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                        projectHandler::delete)
                .andRoute(RequestPredicates.POST(API_BASE_URL+"update")
                                .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                        projectHandler::update)
                .andRoute(RequestPredicates.POST(API_BASE_URL+"save")
                                .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                        projectHandler::save);
    }
}

