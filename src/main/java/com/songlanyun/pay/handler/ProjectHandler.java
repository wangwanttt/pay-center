package com.songlanyun.pay.handler;

import com.songlanyun.pay.dao.ProjectRepository;
import com.songlanyun.pay.domain.Project;

import com.songlanyun.pay.error.GlobalException;
import com.songlanyun.pay.payment.configure.AliPayConfig;
import com.songlanyun.pay.utils.ResponseInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
public class ProjectHandler {

    @Autowired
    private final ProjectRepository projectRepository;

    public ProjectHandler(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }


    public Mono<ServerResponse> save(ServerRequest request) {
        Mono<Void> fallback = Mono.error(new GlobalException(-200, "项目名称已存在 "));

        return request
                .bodyToMono(Project.class)
                .flatMap(prjVo -> {
                    String name = prjVo.getName();
                    return projectRepository.findByNameAndPayTypeAndAndPayWay(name,prjVo.getPayType(),prjVo.getPayWay())
                            .flatMap(userDbRecord -> {
                                return ServerResponse.ok().contentType(APPLICATION_JSON).body(ResponseInfo.info(-100, "此项目此支付类型和方式已存在"), ResponseInfo.class);
                            }).switchIfEmpty(
                                    ServerResponse.ok().contentType(APPLICATION_JSON).body(ResponseInfo.ok(insertPrj(prjVo), "保存成功"), ResponseInfo.class)
                            );
                });
    }


    public Mono insertPrj(Project prjVo) {
        return projectRepository.insert(prjVo);
    }


    public Mono<ServerResponse> list(ServerRequest request) {
        Mono<List<Project>> m = projectRepository.findAll().collectList();

        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(ResponseInfo.ok(m), Project.class);
    }


    public Mono<ServerResponse> update(ServerRequest request) {
        return request.bodyToMono(Project.class).flatMap(project -> {
            return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(ResponseInfo.ok(projectRepository.save(project),"更新成功"), Project.class);
        });

    }
    public Mono<ServerResponse> getProjectById(ServerRequest request) {
        String forexId = request.pathVariable("id");
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();
        Mono<Project> forex = projectRepository.findById(forexId);

        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(forex, Project.class)
                .switchIfEmpty(notFound);
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        String id = request.pathVariable("id");
        id = id.replaceAll("\"", "");
        Mono<Void> delId = projectRepository.deleteById(id);
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(ResponseInfo.ok(delId), Project.class);

    }
}
