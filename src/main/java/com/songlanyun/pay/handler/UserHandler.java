package com.songlanyun.pay.handler;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.mongodb.DuplicateKeyException;
import com.songlanyun.pay.dao.UserRepository;
import com.songlanyun.pay.domain.User;
import com.songlanyun.pay.error.GlobalException;
import com.songlanyun.pay.utils.ResponseInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.MalformedParameterizedTypeException;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.util.MimeTypeUtils.TEXT_PLAIN;

@Component
public class UserHandler {

    @Autowired
    private final UserRepository repository;

    public Mono<User> findUser(String name, String psw) {
        return  repository.findByNameAndPassword(name, psw);
    }

    public Mono<User> findCityById(String name,String psw) {
        return repository.findByNameAndPassword(name,psw);
    }

    public UserHandler(UserRepository repository) {
        this.repository = repository;
    }

    //http://localhost:8888/saveUser
    public Mono<ServerResponse> saveUser(ServerRequest request) {
        Mono<User> user = request.bodyToMono(User.class);
        return ServerResponse.ok().build(
                repository.insert(user).then()
        );
    }

    //http://localhost:8888/deleteUser/1
    public Mono<ServerResponse> deleteUser(ServerRequest request) {
        Long userId = Long.valueOf(request.pathVariable("id"));
        return ServerResponse.ok().build(repository.deleteById(userId).then());
    }

    //http://localhost:8888/user/1
    public Mono<ServerResponse> getUser(ServerRequest request) {
        Long userId = Long.valueOf(request.pathVariable("id"));
        Mono<User> User = repository.findByNameAndPassword("admin","111111");
        return ServerResponse.ok().contentType(APPLICATION_JSON).body(User, User.class);
    }

    //http://localhost:8888/listUser
    public Mono<ServerResponse> listUser(ServerRequest request) {
        Flux<User> userList = repository.findAll();
        return ServerResponse.ok().contentType(APPLICATION_JSON).body(userList, User.class);
    }
    public Mono<ServerResponse> findUser(ServerRequest request) {
        Mono<Object> project = request.bodyToMono(Map.class);
        Mono<User> fallback = Mono.error(new GlobalException(-200,"用户名或密码不存在"));

        return request.bodyToMono(Map.class)
                .flatMap(map -> {
                    String name = map.get("username").toString();
                    String psw = map.get("password").toString();

                   Mono<User> user = repository.findByNameAndPassword(name, psw).switchIfEmpty(fallback);
                    // Mono<List<User>> m = repository.findAll().collectList();
                    return ServerResponse.ok().contentType(APPLICATION_JSON).body( ResponseInfo.ok(user,"sucess"), User.class) ;
                   // return ServerResponse.ok().build( repository.findById(1l)).then()) ;
                });
    }

}


//    public Mono<ServerResponse> register( ServerRequest request )
//    {
//        return request
//                .bodyToMono( User.class )
//                // make sure you use Reactive DataBase Access in order to
//                // get the all benefits of Non-Blocking I/O with Project Reactor
//                // if you use JPA - consider Moving to R2DBC r2dbc.io
//                .flatMap( user -> // <1>
//                        Mono.just( userRepository.findByPhone( user.phone ) ) // <2>
//                                .map(userDbRecord -> {
//                                    logger.info( "register. User already exist with phone: " + userDbRecord.getPhone() + ", id: " + userDbRecord.getId() );
//                                    return userDbRecord.getToken();
//                                })
//                                .switchIfEmpty(Mono.fromSupplier(() -> UUID.randomUUID().toString())) <3>
//                        .flatMap(uuid -> {
//                            SMS.send( user.phone, random ); <4>
//                                    Auth auth = new Auth();
//                            auth.token = uuid;
//                            return ok().contentType( APPLICATION_JSON ).syncBody( auth );
//                        })
//                );
//    }
//request.bodyToMono(User.class)
//        .doOnSuccess(user -> return userRepository.insert(user))
//        .map(user -> ServerResponse.ok(user))
//
//request
//        .getQueryParam("type")
//        .map(type -> service.getAddressByType(type))
//        .orElseGet(() -> service.getAllAddresses());

//
//您不应该在Handler中执行阻塞操作。
//
//        您应该将代码更改为此：
//
//@Component
//public class DataStreamHandler {
//
//    public Mono<ServerResponse> pipeEvent(ServerRequest request) {
//        return request.bodyToMono(String.class)
//                .doOnNext(System.out::println)
//                .then(ServerResponse.ok().body(fromObject("OK")));
//    }
//}