package com.songlanyun.pay.utils;

import lombok.Data;
import lombok.ToString;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Serializable;

@Data
@ToString
public class ResponseInfo2<T> implements Serializable {
    String msg;
    Integer code;
    private static final long serialVersionUID = -2551908500227408235L;

}