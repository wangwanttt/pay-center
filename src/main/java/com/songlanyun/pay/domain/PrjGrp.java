package com.songlanyun.pay.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrjGrp {

    @Id
    private String id;

    /**
     * 项目名称
     */
    private String name;
}
