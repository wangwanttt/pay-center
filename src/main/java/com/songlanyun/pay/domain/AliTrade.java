package com.songlanyun.pay.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

/** 支付宝异步通知返回类  **/

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AliTrade {
    /**
     * 编号
     */
    @Id
    private String id;
//
//
//    private String gmt_create;
//    private String seller_email;
//    private String subject;
//    private String sign;
//    private String buyer_id;
//    private String invoice_amount;
//    private String notify_id;
//    private String fund_bill_list;
//    private String notify_type;
//    private String trade_status;
//    private String receipt_amount;
//    private String app_id;
//    private String buyer_pay_amount;
//    private String sign_type;
//    private String seller_id;
//    private String gmt_payment;
//    private String notify_time;
//    private String version;
//    private String out_trade_no;
//    private String total_amount;
//    private String tradeNo;
//    private String auth_app_id;
//    private String buyer_logon_id;
//    private String point_amount;


    /**
     * 通知时间
     */
    private String notifyTime;

    /**
     * 通知类型
     */
    private String notifyType;

    /**
     * 通知校验ID
     */
    private String notifyId;

    /**
     * 支付宝分配给开发者的应用Id
     */
    private String appId;

    /**
     * 编码格式
     */
    private String charset;

    /**
     * 编码格式
     */
    private String version;

    /**
     * 签名方式
     */
    private String signType;

    /**
     * 签名
     */
    private String sign;

    /*
        业务参数
     */

    /**
     * 商户网站唯一订单号---用户自定义的订单号
     * 需保证在商户网站中的唯一性。是请求时对应的参数，原样返回。
     */
    private String outTradeNo;

    /**
     * 商品名称
     */
    private String subject;

    /**
     * 该交易在支付宝系统中的交易流水号。
     */
    private String tradeNo;

    /**
     * 商户业务号---微信的交易注销水号
     */
    private String outBizNo;

    /**
     * 交易状态 或微信的 交易类型	如app,web之类
     */
    private String tradeStatus;

    /**
     * 卖家支付宝用户号
     */
    private String sellerId;

    /**
     * 卖家支付宝帐号
     */
    private String sellerEmail;

    /**
     * 买家支付宝用户号
     */
    private String buyerId;

    /**
     * 买家支付宝账号
     */
    private String buyerLogonId;

    /**
     * 交易金额
     */
    private double totalAmount;

    /**
     * 实收金额
     */
    private double receiptAmount;

    /**
     * 开票金额
     */
    private double invoiceAmount;

    /**
     * 买家付款金额
     */
    private double buyerPayAmount;

    /**
     * 集分宝金额
     */
    private double pointAmount;

    /**
     * 总退款金额
     */
    private double refundFee;

    /**
     * 商品描述
     */
    private String body;

    /**
     * 交易创建时间
     */
    private String gmtCreate;

    /**
     * 交易付款时间
     */
    private String gmtPayment;

    /**
     * 支付金额信息
     */
    private String fundBillList;

    /**
     * 回传参数
     */
    private String passbackParams;

    /**  项目id **/
    private String prjId;

    /** 回调地址  **/
    private String callFunc;
    /**
     * 优惠券信息
     */
    private String voucherDetailList;

    /**
     * 退款时间
     */
    private String gmtRefund;

    /**
     * 交易结束时间
     */
    private String gmtClose;

    /**
     * 创建时间
     */
    private String createTime;

    /** 是否已调用 第三方接口 **/
    private int isNotify=0;


}
