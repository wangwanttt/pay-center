package com.songlanyun.pay.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

/**
 * 城市实体类
 *
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    /**
     * 编号
     */
    @Id
    private String id;

    /**
     * 项目名称
     */
    private String name;

    private String appid;
    private String subject;
    /** 支付类型 10 --支付宝 20---微信  **/
    private int payType=10;

    private String mchId;
    /**
     * 支付方式
     * 1 : 支付宝支付数字标识
     * 11: 支付宝电脑网站支付
     * 12: 支付宝手机网站支付
     * 13: 支付宝 APP 支付
     * AliPay: 支付宝支付文字说明
     *
     * 2: 微信支付标识
     * 21: 微信 NATIVE 支付(二维码支付)
     * 22: 微信 JSAPI 支付
     * 23: 微信 H5 支付
     * 24: 微信 APP 支付
     * 25: 微信 小程序 支付
     * WxPay: 微信支付文字说明
     */
    private int payWay=11;

    /** 私钥  **/
    private String priKey;

    /** 公钥  **/
    private String pubKey;

    /** 网关  **/
    private String gateWay;

    /** 回调  **/
    private String callFunc;

    /** ip  **/
    private String ip;

    /**
     * 描述
     */
    private String remark;


}
