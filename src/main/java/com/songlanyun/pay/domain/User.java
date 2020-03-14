package com.songlanyun.pay.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
@Data
public class User    {
    @Id
    private Long id;

    private String name;

    private String password;

    private String token="wwtest";

}
