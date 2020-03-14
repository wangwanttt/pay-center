package com.songlanyun.pay.payment.bean;

import lombok.Data;

import java.io.Serializable;

@Data
public class PayBean implements Serializable {
    private static final long serialVersionUID = -7551908500227408235L;

    /**
     * 用户订单号
     */
    private String orderNo;
    /**
     * 订单总金额
     */
    private String amount;
    /**
     * 用户实际 ip 地址
     */
    private String ip;

    /**
     * 项目id
     */
    private String prjId;

    /**
     * 支付方式
     * 11: 支付宝电脑网站支付
     * 12: 支付宝手机网站支付
     * 13: 支付宝 APP 支付
     *
     * 21: 微信 NATIVE 支付(二维码支付)
     * 22: 微信 JSAPI 支付
     * 23: 微信 H5 支付
     * 24: 微信 APP 支付
     * 25: 微信 小程序 支付
     */
    private int payType;

    /**
     * 微信 JSAPI/小程序 支付必传
     */
    private String openId;

}
