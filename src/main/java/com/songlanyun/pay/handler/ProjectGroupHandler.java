package com.songlanyun.pay.handler;

import com.songlanyun.pay.dao.ProGrpRepository;
import com.songlanyun.pay.dao.ProjectRepository;
import com.songlanyun.pay.domain.PrjGrp;
import com.songlanyun.pay.domain.Project;
import com.songlanyun.pay.error.GlobalException;
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
public class ProjectGroupHandler {

    @Autowired
    private final ProGrpRepository proGrpRepository;

    public ProjectGroupHandler(ProGrpRepository projectRepository) {
        this.proGrpRepository = projectRepository;
    }


    public Mono<ServerResponse> save(ServerRequest request) {
        Mono<Void> fallback = Mono.error(new GlobalException(-200, "项目名称已存在 "));

        return request
                .bodyToMono(PrjGrp.class)
                .flatMap(prjVo -> {
                    String name = prjVo.getName();
                    return proGrpRepository.findByName(name)
                            .flatMap(userDbRecord -> {
                                return ServerResponse.ok().contentType(APPLICATION_JSON).body(ResponseInfo.info(-100, "项目名称已存在"), ResponseInfo.class);
                            }).switchIfEmpty(
                                    ServerResponse.ok().contentType(APPLICATION_JSON).body(ResponseInfo.ok(insertPrj(prjVo), "保存成功"), ResponseInfo.class)
                            );
                });
    }


    public Mono insertPrj(PrjGrp prjVo) {
        return proGrpRepository.insert(prjVo);
    }


    public Mono<ServerResponse> list(ServerRequest request) {
        Mono<List<PrjGrp>> m = proGrpRepository.findAll().collectList();

        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(ResponseInfo.ok(m), Project.class);
    }


    public Mono<ServerResponse> update(ServerRequest request) {
        return request.bodyToMono(PrjGrp.class).flatMap(project -> {
            return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(ResponseInfo.ok(proGrpRepository.save(project),"更新成功"), Project.class);
        });

    }
    public Mono<ServerResponse> getProjectById(ServerRequest request) {
        String forexId = request.pathVariable("id");
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();
        Mono<PrjGrp> forex = proGrpRepository.findById(forexId);

        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(forex, Project.class)
                .switchIfEmpty(notFound);
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        String id = request.pathVariable("id");
        id = id.replaceAll("\"", "");
        Mono<Void> delId = proGrpRepository.deleteById(id);

        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(ResponseInfo.ok(delId), PrjGrp.class);

    }
}
