package com.songlanyun.pay.payment.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;

import com.songlanyun.pay.dao.AliTradeRepository;
import com.songlanyun.pay.domain.Project;
import com.songlanyun.pay.domain.AliTrade;
import com.songlanyun.pay.error.GlobalException;
import com.songlanyun.pay.payment.bean.PayBean;
import com.songlanyun.pay.payment.common.api.ApiResult;
import com.songlanyun.pay.payment.common.api.ResponseCode;
import com.songlanyun.pay.payment.common.constant.PayTypeConst;
import com.songlanyun.pay.payment.common.util.*;
import com.songlanyun.pay.payment.configure.AliPayConfig;
import com.songlanyun.pay.payment.configure.WxPayConfig;
import com.songlanyun.pay.payment.service.PayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @Description: 支付业务具体实现
 */
@Service("payService")
@Slf4j
public class PayServiceImpl implements PayService {

    @Autowired
    private WxPayConfig wxPayConfig;
    @Autowired
    private AliPayConfig aliPayConfig;

    private WebClient webClient;


    @Autowired
    private final AliTradeRepository aliOrderRepository;

    public PayServiceImpl(AliTradeRepository aliOrderRepository) {
        this.aliOrderRepository = aliOrderRepository;
    }

    /**
     * 创建支付订单
     *
     * @param payBean json 格式参数
     * @return
     */
    @Override
    public ApiResult createPayOrder(PayBean payBean, Project prjVo) throws Exception {
        // 微信支付金额换算
        int amountWxPay = CalculateUtil.multiply(Double.valueOf(payBean.getAmount()), 100, 2).intValue();
        // 返回结果
        Map<String, String> resultMap = new HashMap<>(16);

        //实例化客户端
        WxPayConfig wxConfig = WxPayManager.getInfo(wxPayConfig, prjVo);

        // 创建支付订单
        switch (prjVo.getPayWay()) {
            case PayTypeConst.ORDER_PAY_TYPE_ALIPAY_PC:
                // 支付宝电脑网站支付
                String aliPayPCForm = AliPayManager.createPCOrder(payBean.getOrderNo(),
                        String.valueOf(payBean.getAmount()), aliPayConfig, prjVo);
                if (!StringUtils.isEmpty(aliPayPCForm)) {
                    resultMap.put("prePayOrderInfo", aliPayPCForm);
                    return ApiResult.success(resultMap);
                }
                break;
            case PayTypeConst.ORDER_PAY_TYPE_ALIPAY_WAP:
                // 支付宝手机网站支付
                String aliPayWapForm = AliPayManager.createWapOrder(payBean.getOrderNo(),
                        String.valueOf(payBean.getAmount()), aliPayConfig, prjVo);
                if (!StringUtils.isEmpty(aliPayWapForm)) {
                    resultMap.put("prePayOrderInfo", aliPayWapForm);
                    return ApiResult.success(resultMap);
                }
                break;
            case PayTypeConst.ORDER_PAY_TYPE_ALIPAY_APP:
                // 支付宝 APP 支付
                String aliPayAppForm = AliPayManager.createAppOrder(payBean.getOrderNo(),
                        String.valueOf(payBean.getAmount()), aliPayConfig, prjVo);
                if (!StringUtils.isEmpty(aliPayAppForm)) {
                    resultMap.put("prePayOrderInfo", aliPayAppForm);
                    return ApiResult.success(resultMap);
                }
                break;
            case PayTypeConst.ORDER_PAY_TYPE_WX_NATIVE:
                // 微信 NATIVE 支付(二维码)
                Map<String, String> wxPayNativeMap = WxPayManager.createNativeOrder(wxConfig,
                        payBean.getOrderNo() + PayTypeConst.ORDER_PAY_TYPE_WX_NATIVE,
                        amountWxPay, payBean.getIp(),prjVo);
                if (wxPayNativeMap != null &&
                        Objects.equals(wxPayNativeMap.get("pre_pay_order_status"), wxConfig.getResponseSuccess())) {
                    resultMap.put("prePayOrderInfo", wxPayNativeMap.get("code_url"));
                    return ApiResult.success(resultMap);
                }
                break;
            case PayTypeConst.ORDER_PAY_TYPE_WX_JSAPI:
                // 微信 JsAPI 支付(公众号)
                if (StringUtils.isEmpty(payBean.getOpenId())) {
                    return ApiResult.failure(ResponseCode.PAY_SUBMIT_ERROR);
                }
                Map<String, String> wxPayJsAPIMap = WxPayManager.createJsAPIOrder(wxConfig,
                        payBean.getOrderNo() + PayTypeConst.ORDER_PAY_TYPE_WX_JSAPI,
                        amountWxPay, payBean.getIp(), payBean.getOpenId(),prjVo);
                if (wxPayJsAPIMap != null &&
                        Objects.equals(wxPayJsAPIMap.get("pre_pay_order_status"), wxConfig.getResponseSuccess())) {
                    return ApiResult.success(wxPayJsAPIMap);
                }
                break;
            case PayTypeConst.ORDER_PAY_TYPE_WX_H5:
                // 微信 H5 支付
                Map<String, String> wxPayH5Map = WxPayManager.createH5Order(wxConfig,
                        payBean.getOrderNo() + PayTypeConst.ORDER_PAY_TYPE_WX_H5,
                        amountWxPay, payBean.getIp(),prjVo);
                if (wxPayH5Map != null &&
                        Objects.equals(wxPayH5Map.get("pre_pay_order_status"), wxConfig.getResponseSuccess())) {
                    resultMap.put("prePayOrderInfo", wxPayH5Map.get("mweb_url"));
                    return ApiResult.success(resultMap);
                }
                break;
            case PayTypeConst.ORDER_PAY_TYPE_WX_APP:
                // 微信 APP 支付
                Map<String, String> wxPayAppMap = WxPayManager.createAppOrder(wxConfig,
                        payBean.getOrderNo() + PayTypeConst.ORDER_PAY_TYPE_WX_APP,
                        amountWxPay, payBean.getIp(),prjVo);
                if (wxPayAppMap != null &&
                        Objects.equals(wxPayAppMap.get("pre_pay_order_status"), wxConfig.getResponseSuccess())) {
                    return ApiResult.success(wxPayAppMap);
                }
                break;
            case PayTypeConst.ORDER_PAY_TYPE_WX_MINI:
                // 微信 小程序 支付
                if (StringUtils.isEmpty(payBean.getOpenId())) {
                    return ApiResult.failure(ResponseCode.PAY_SUBMIT_ERROR);
                }
                Map<String, String> wxPayMiniMap = WxPayManager.createJsAPIOrder(wxConfig,
                        payBean.getOrderNo() + PayTypeConst.ORDER_PAY_TYPE_WX_MINI,
                        amountWxPay, payBean.getIp(), payBean.getOpenId(),prjVo);
                if (wxPayMiniMap != null &&
                        Objects.equals(wxPayMiniMap.get("pre_pay_order_status"), wxConfig.getResponseSuccess())) {
                    return ApiResult.success(wxPayMiniMap);
                }
                break;
            default:
                return ApiResult.failure(ResponseCode.PAY_TYPE_ERROR);
        }
        return ApiResult.failure(ResponseCode.PAY_SUBMIT_ERROR);
    }

    /**
     * (主动)获取支付结果
     *
     * @param payBean 订单信息(json 格式参数)
     * @return
     */
    @Override
    public ApiResult getPayResult(PayBean payBean, Project prjVo) throws Exception {
        // 返回结果
        Map<String, String> resultMap;
        AliPayConfig aliConfig = AliPayManager.getInfo(aliPayConfig, prjVo);
        WxPayConfig wxConfig = WxPayManager.getInfo(wxPayConfig, prjVo);
        switch (payBean.getPayType()) {
            case PayTypeConst.ORDER_PAY_TYPE_ALIPAY_PC:
            case PayTypeConst.ORDER_PAY_TYPE_ALIPAY_WAP:
            case PayTypeConst.ORDER_PAY_TYPE_ALIPAY_APP:
                resultMap = AliPayManager.getPayResult(aliConfig, payBean.getOrderNo());
                break;
            case PayTypeConst.ORDER_PAY_TYPE_WX_NATIVE:
            case PayTypeConst.ORDER_PAY_TYPE_WX_JSAPI:
            case PayTypeConst.ORDER_PAY_TYPE_WX_H5:
            case PayTypeConst.ORDER_PAY_TYPE_WX_APP:
            case PayTypeConst.ORDER_PAY_TYPE_WX_MINI:
                resultMap = WxPayManager.getPayResult(wxConfig, payBean.getOrderNo() + payBean.getPayType());
                break;
            default:
                return ApiResult.failure(ResponseCode.PAY_TYPE_ERROR);
        }
        if (MapUtil.isEmpty(resultMap)) {
            return ApiResult.failure(ResponseCode.PAY_STATUS_ERROR);
        }

        return ApiResult.success(resultMap);
    }

    /**
     * 微信支付结果通知
     *
     * @param request 微信支付回调请求
     * @return 支付结果
     */
    @Override
    public String wxPayNotify(ServerRequest request) {

        String result = null;
        try {
            InputStream inputStream = null;
            /**
             * 读取通知参数
             */
            String strXML = FileUtil.getStringFromStream(inputStream);
            Map<String, String> reqMap = MapUtil.xml2Map(strXML);
            if (MapUtil.isEmpty(reqMap)) {
                log.warn("request param is null");
                return wxPayConfig.getResponseFail();
            }
            /**
             * 校验签名
             */
            if (!SignUtil.signValidate(reqMap, wxPayConfig.getKey(), wxPayConfig.getFieldSign())) {
                log.warn("wxPay sign is error");
                return wxPayConfig.getResponseFail();
            }
            String orderNo = reqMap.get("out_trade_no").substring(0, reqMap.get("out_trade_no").length() - 2);
            log.debug("微信支付回调,订单编号: {}", orderNo);
            // TODO 其他业务处理


            Map<String, String> resultMap = new HashMap<>(16);
            resultMap.put("return_code", wxPayConfig.getResponseSuccess());
            resultMap.put("return_msg", "OK");
            result = MapUtil.map2Xml(resultMap);
        } catch (IOException e) {
            log.error("get request inputStream error", e);
            return wxPayConfig.getResponseFail();
        } catch (Exception e) {
            log.error("resolve request param error", e);
            return wxPayConfig.getResponseFail();
        }
        return result;
    }

    /**
     * 支付宝支付结果通知
     *
     * @param request 支付宝回调请求
     * @return
     */

    public Mono<String> aliPayNotify(ServerRequest request) {
        // 读取通知参数
        Mono<MultiValueMap<String, String>> data = request.exchange().getFormData();

        return data.flatMap(formData -> {
            Map params = formData.toSingleValueMap();
            AliTrade order = new AliTrade();
            BeanUtils.copyProperties(order, params);
            if (MapUtil.isEmpty(params)) {
                return Mono.just(aliPayConfig.getResponseFail());
            }
            try {
                // 签名校验
                if (!AlipaySignature.rsaCheckV1(params, aliPayConfig.getAlipayPublicKey(),
                        aliPayConfig.getCharset(), aliPayConfig.getSignType())) {
                    return Mono.just(aliPayConfig.getResponseFail());
                }
                String orderNo = params.get("out_trade_no").toString();
                log.info("支付宝回调,订单编号: {}", orderNo);
                // TODO 其他业务处理--- 将此订单信息写入数据库
                return null;//insertOrder(order);


            } catch (AlipayApiException e) {
                log.error("支付宝回调验证失败", e);
                return Mono.just(aliPayConfig.getResponseFail());
            }

        }).onErrorReturn(aliPayConfig.getResponseFail());

    }


    /**
     * 支付宝支付同步通知返回地址
     *
     * @param request
     * @return
     */
    @Override
    public String aliPayReturnUrl(ServerRequest request) {
        // 读取通知参数
        Map<String, String> params = AliPayManager.getNotifyParams(request.queryParams());
        if (MapUtil.isEmpty(params)) {
            return "alipay_fail_url";
        }
        try {
            // 签名校验
            if (!AlipaySignature.rsaCheckV1(params, aliPayConfig.getAlipayPublicKey(),
                    aliPayConfig.getCharset(), aliPayConfig.getSignType())) {
                return "alipay_fail_url";
            }

        } catch (AlipayApiException e) {
            log.error("支付宝回调验证失败", e);
            return aliPayConfig.getResponseFail();
        }
        return "alipay_success_url";
    }


    /**
     * 通过 contoller  非 handler 支付宝支付结果通知
     *
     * @return
     */
    public Mono<String> aliPayNotify(Map<String, String> params) {

        if (MapUtil.isEmpty(params)) {
            return Mono.just(aliPayConfig.getResponseFail());
        }
        try {
            // 签名校验
            if (!AlipaySignature.rsaCheckV1(params, aliPayConfig.getAlipayPublicKey(),
                    aliPayConfig.getCharset(), aliPayConfig.getSignType())) {
                return Mono.just(aliPayConfig.getResponseFail());
            }
            String orderNo = params.get("out_trade_no");
            log.debug("支付宝回调,订单编号: {}", orderNo);
            // TODO 其他业务处理 ---如果订单已经入库，则不管，否则写入数据库，并调用客户端通知
            AliTrade tratoVo = new AliTrade();
            tratoVo = getTrandeVo(tratoVo, params);
            return aliOrderRepository.findByTradeNo(orderNo).flatMap(orderVoc -> {
                log.info(orderVoc.getTradeNo() + "订单号已存在 ");
                return Mono.just(aliPayConfig.getResponseSuccess());

            }).switchIfEmpty(aliOrderRepository.save(tratoVo).flatMap(v -> {  //数据库中无此记录则将其写入，并调用用户设置的回调方法
                //调用 项目管理中设置的远程回调地址返回参数
                WebClient webClient = WebClient.create();
                 return webClient
                         .post().uri(v.getCallFunc())
                         .contentType(MediaType.APPLICATION_JSON)
                         .body(BodyInserters.fromValue(v))
                         .retrieve()
                         .onStatus(HttpStatus::is4xxClientError, clientResponse ->
                                 Mono.error(new GlobalException(-400,"服务器错误 "))
                         )
                         .onStatus(HttpStatus::is5xxServerError, clientResponse ->
                                 Mono.error(new GlobalException(-500,"服务器错误 "))
                         ).bodyToMono(String.class).flatMap(retStr->{
                             log.info(retStr);
                             return Mono.just(aliPayConfig.getResponseSuccess());
                         });


            }).onErrorResume(e->{
                log.info(String.valueOf(e));
                return   Mono.error(new GlobalException(-600,"订单支付成功但保存数据库失败 "));
            }));

//           return insertOrder(tratoVo).onErrorResume(e->{
//                log.info("支付宝收款成功，但写入本地数据库失败");
//                return Mono.error(new GlobalException(-300,"收款成功，但写入本地数据库失败"));
//            }) ;

        } catch (AlipayApiException e) {
            log.error("支付宝回调验证失败", e);
            return Mono.just(aliPayConfig.getResponseFail());
        }

    }


    private AliTrade getTrandeVo(AliTrade tvo, Map<String, String> params) {
        tvo.setAppId(params.get("app_id"));
        tvo.setGmtCreate(params.get("gmt_create"));
        tvo.setCharset(params.get("charset"));
        tvo.setSellerEmail(params.get("seller_email"));
        tvo.setPassbackParams(params.get("setPassbackParams"));
        tvo.setSubject(params.get("subject"));
        tvo.setCharset(params.get("sign"));
        tvo.setCharset(params.get("buyer_id"));
        tvo.setInvoiceAmount(Double.valueOf(params.get("invoice_amount")));
        tvo.setFundBillList(params.get("fund_bill_list"));
        tvo.setNotifyType(params.get("notify_type"));
        tvo.setTradeStatus(params.get("trade_status"));
        tvo.setReceiptAmount(Double.valueOf(params.get("receipt_amount")));
        tvo.setBuyerPayAmount(Double.valueOf(params.get("buyer_pay_amount")));
        tvo.setSignType(params.get("sign_type"));
        tvo.setSellerId(params.get("seller_id"));
        tvo.setGmtPayment(params.get("gmt_payment"));
        tvo.setNotifyType(params.get("notify_type"));
        tvo.setNotifyTime(params.get("notify_time"));
        tvo.setTradeNo(params.get("out_trade_no"));
        tvo.setBuyerLogonId(params.get("buyer_logon_id"));
        tvo.setPointAmount(Double.valueOf(params.get("point_amount")));
        tvo.setPassbackParams(params.get("passback_params"));
        String[] param = params.get("passback_params").split(",");
        tvo.setPrjId(param[0]);
        tvo.setCallFunc(param[1]);
        return tvo;
    }

}
