package com.songlanyun.pay.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.songlanyun.pay.dao.AliTradeRepository;
import com.songlanyun.pay.domain.AliTrade;
import com.songlanyun.pay.error.GlobalException;

import com.songlanyun.pay.payment.common.util.FileUtil;
import com.songlanyun.pay.payment.common.util.MapUtil;
import com.songlanyun.pay.payment.common.util.SignUtil;
import com.songlanyun.pay.payment.configure.AliPayConfig;
import com.songlanyun.pay.payment.configure.WxPayConfig;
import com.songlanyun.pay.payment.service.PayService;
import com.songlanyun.pay.utils.InputStreamCollector;
import com.songlanyun.pay.utils.ResponseInfo;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
//import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
//import static org.springframework.web.reactive.function.BodyInserters.fromPublisher;
//import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@RestController
@RequestMapping(value = "/pay")
@Slf4j
public class PayCallBackController {


    @Autowired
    private WxPayConfig wxPayConfig;
    @Autowired
    private AliPayConfig aliPayConfig;

    @Autowired
    private PayService payService;


    @Autowired
    private final AliTradeRepository aliOrderRepository;

    public PayCallBackController(AliTradeRepository aliOrderRepository) {
        this.aliOrderRepository = aliOrderRepository;
    }

    /**
     * 项目给支付中心的调用方法示例
     **/
    @PostMapping("/getAliPayInfo")
    public Mono<String> getAliPayInfo(ServerWebExchange exchang) {

        Mono<MultiValueMap<String, String>> data = exchang.getFormData();
        return data.flatMap(formData -> {
            Map<String, String> params = formData.toSingleValueMap();
            return Mono.just("SUCCESS");
        });
    }


    /**
     * 支付宝回调，用hanlder方式 ，由于其返回的不是 sucess字符串，会导致异步回调多次调用
     **/
    @PostMapping("aliPayNotify")
    public Mono<String> aliPayNotify(ServerWebExchange exchang) {
        Mono<MultiValueMap<String, String>> data = exchang.getFormData();
        return data.flatMap(formData -> {
            Map<String, String> params = formData.toSingleValueMap();
            if (MapUtil.isEmpty(params)) {
                return Mono.just(aliPayConfig.getResponseFail());
            }
            // 签名校验
            try {
                if (!AlipaySignature.rsaCheckV1(params, aliPayConfig.getAlipayPublicKey(),
                        aliPayConfig.getCharset(), aliPayConfig.getSignType())) {
                    return Mono.just(aliPayConfig.getResponseFail());
                }
            } catch (AlipayApiException e) {
                return Mono.just(aliPayConfig.getResponseFail());
            }
            String orderNo = params.get("out_trade_no");
            log.debug("支付宝回调,订单编号: {}", orderNo);
            // TODO 其他业务处理 ---如果订单已经入库，则不管，否则写入数据库，并调用客户端通知
            AliTrade tratoVo = new AliTrade();
            tratoVo = getTrandeVo(tratoVo, params);
            Mono<AliTrade> aliOrder = aliOrderRepository.findByTradeNo(orderNo);
            return aliOrder.flatMap(orderVoc -> {
                log.info(orderVoc.getTradeNo() + "订单号已存在 ");
                return Mono.just(aliPayConfig.getResponseSuccess());
            }).switchIfEmpty(aliOrderRepository.save(tratoVo).flatMap(v -> {  //数据库中无此记录则将其写入，并调用用户设置的回调方法
                //调用 项目管理中设置的远程回调地址返回参数
                Mono<String> result = Mono.justOrEmpty(aliPayConfig.getResponseFail());
                result = doThirdFunc(v);
                result.subscribe(PayCallBackController::handleResponse);
                //  String response = result.block();
                // System.out.println(response);
                return Mono.just(aliPayConfig.getResponseSuccess());
            })).onErrorResume(e -> Mono.just("Error " + e.getMessage()));
        });
    }


    /**
     * 支付宝回调，用hanlder方式 ，由于其返回的不是 sucess字符串，会导致异步回调多次调用
     **/
    @PostMapping("wxPayNotify")
    public Mono<String> wxPayNotify(ServerWebExchange exchang) {
        Flux<DataBuffer> body = exchang.getRequest().getBody();
        return body.collect(InputStreamCollector::new, (t, dataBuffer) -> t.collectInputStream(dataBuffer.asInputStream()))
                .flatMap(inputStream -> {
                    /**
                     * 读取通知参数
                     */
                    String strXML = null;
                    Mono<String> result = Mono.just(wxPayConfig.getResponseFail());

                    try {
                        strXML = FileUtil.getStringFromStream(inputStream.getInputStream());
                    } catch (IOException e) {
                        return Mono.just(wxPayConfig.getResponseFail());
                    }

                    Map<String, String> reqMap = null;
                    try {
                        reqMap = MapUtil.xml2Map(strXML);
                    } catch (DocumentException e) {
                        return Mono.just(wxPayConfig.getResponseFail());
                    }

                    if (MapUtil.isEmpty(reqMap)) {
                        log.warn("request param is null");
                        return Mono.just(wxPayConfig.getResponseFail());
                    }
                    /**
                     * 校验签名
                     */
                    if (!SignUtil.signValidate(reqMap, wxPayConfig.getKey(), wxPayConfig.getFieldSign())) {
                        log.warn("wxPay sign is error");
                        return Mono.just(wxPayConfig.getResponseFail());
                    }
                    String orderNo = reqMap.get("out_trade_no").substring(0, reqMap.get("out_trade_no").length() - 2);
                    log.info("微信支付回调,订单编号: {}", orderNo);
                    // TODO 其他业务处理
                    AliTrade tratoVo = new AliTrade();
                    tratoVo = getWxTrandeVo(tratoVo, reqMap);
                    Mono<AliTrade> aliOrder = aliOrderRepository.findByTradeNo(orderNo);
                    return aliOrder.flatMap(orderVoc -> {
                        log.info(orderVoc.getTradeNo() + "订单号已存在 ");
                        return Mono.just(wxReturnStr());
                    }).switchIfEmpty(aliOrderRepository.save(tratoVo).flatMap(v -> {  //数据库中无此记录则将其写入，并调用用户设置的回调方法
                        //调用 项目管理中设置的远程回调地址返回参数
                        Mono<String> resultV = Mono.justOrEmpty(aliPayConfig.getResponseFail());
                        resultV=doThirdFunc(v);
                        resultV.subscribe(PayCallBackController::handleResponse);
                        //  String response = result.block();
                        // System.out.println(response);
                        return Mono.just(wxReturnStr());
                    })).onErrorResume(e -> Mono.just("Error " + e.getMessage()));
                });

    }


    private String wxReturnStr(){
        Map<String, String> resultMap = new HashMap<>(16);
        resultMap.put("return_code", wxPayConfig.getResponseSuccess());
        resultMap.put("return_msg", "OK");
        String resultV = MapUtil.map2Xml(resultMap);
        return resultV;
    }
    /**
     * 调用第3方的接口
     **/
    Mono<String> doThirdFunc(AliTrade v) {
        WebClient webClient = WebClient.create();
        return webClient
                .post().uri(v.getCallFunc())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(v))
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                            // return Mono.error(new GlobalException(-400, "服务器错误 "));
                            return clientResponse.bodyToMono(ResponseInfo.class).flatMap(error -> {
                                log.info("服务器400错误 ");
                                return Mono.error(new GlobalException(-400, error.getMsg()));
                            });
                        }

                )
                .onStatus(HttpStatus::is5xxServerError, clientResponse ->
                        {
                            log.info("服务器500错误 ");
                            return Mono.error(new GlobalException(-500, "服务器错误 "));
                        }
                ).bodyToMono(String.class).flatMap(retStr -> {
                    log.info(retStr);
                    //发送通知成功---将当前订单的通知标识标为已通知
                    v.setIsNotify(1);
                    return aliOrderRepository.save(v).flatMap(updateVo -> {
                        log.info("发送消息成功--并更新状态");
                        return Mono.just(aliPayConfig.getResponseSuccess());
                    });
                });

    }


    public static String handleResponse(String s) {
        System.out.println("handle response");
        System.out.println(s);
        return "success";
    }

    private AliTrade getTrandeVo(AliTrade tvo, Map<String, String> params) {
        tvo.setAppId(params.get("app_id"));
        tvo.setGmtCreate(params.get("gmt_create"));
        tvo.setCharset(params.get("charset"));
        tvo.setSellerEmail(params.get("seller_email"));
        tvo.setPassbackParams(params.get("setPassbackParams"));
        tvo.setSubject(params.get("subject"));
        tvo.setSign(params.get("sign"));
        tvo.setBuyerId(params.get("buyer_id"));
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
        tvo.setOutTradeNo(params.get("out_trade_no"));
        tvo.setBuyerLogonId(params.get("buyer_logon_id"));
        tvo.setPointAmount(Double.valueOf(params.get("point_amount")));
        tvo.setPassbackParams(params.get("passback_params"));
        String[] param = params.get("passback_params").split(",");
        tvo.setPrjId(param[0]);
        tvo.setCallFunc(param[1]);
        return tvo;
    }



    private AliTrade getWxTrandeVo(AliTrade tvo, Map<String, String> params) {
        tvo.setAppId(params.get("appid"));
        //商户交易号
        tvo.setOutTradeNo(params.get("transaction_id"));
        //买家openId
        tvo.setBuyerId(params.get("openid"));
        //用户自定义的订单号
        //tvo.setOutTradeNo(params.get("out_trade_no"));
        tvo.setTradeNo(params.get("out_trade_no"));
       // sign号
        tvo.setSign(params.get("sign"));
       //订单时间
        tvo.setGmtCreate(params.get("time_end"));
        tvo.setCharset("utf-8");
       // 交易类型	trade_type
        tvo.setTradeStatus(params.get("trade_type"));

        //卖家id  133
        tvo.setSellerId(params.get("mch_id"));
        //交易金额
        tvo.setReceiptAmount(Double.valueOf(params.get("total_fee")));

        //交易金额
        tvo.setBuyerPayAmount(Double.valueOf(params.get("total_fee")));

        tvo.setPassbackParams(params.get("attach"));
        String[] param = params.get("attach").split(",");
        tvo.setPrjId(param[0]);
        tvo.setCallFunc(param[1]);
        return tvo;
    }
    @GetMapping("/webclient/test")
    public Mono<ResponseInfo> testWebclient(@RequestBody ResponseInfo requestPayload) {
        String url = "http://localhost:8080/api/write/simple";
        return WebClient.create()
                .post()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(requestPayload))
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response -> {
                    return response.bodyToMono(ResponseInfo.class).flatMap(error -> {
                        return Mono.error(new GlobalException(-400, error.getMsg()));
                    });
                })
                .onStatus(HttpStatus::is5xxServerError, response -> {
                    return response.bodyToMono(ResponseInfo.class).flatMap(error -> {
                        return Mono.error(new GlobalException(-400, error.getMsg()));
                    });
                })
                .bodyToMono(ResponseInfo.class);
    }

}
