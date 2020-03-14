package com.songlanyun.pay.payment.controller;


import com.songlanyun.pay.payment.bean.PayBean;
import com.songlanyun.pay.payment.common.api.ApiResult;
import com.songlanyun.pay.payment.service.PayService;
import com.songlanyun.pay.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


/**
 * @Description: 支付回调
 * @Date: 2018/7/10
 */
@Controller
@RequestMapping("api/pay")
@Slf4j
public class PayController {

    @Autowired
    private PayService payService;



    /**
     * 微信支付结果异步通知
     *
     * @param request 微信支付回调请求
     * @return
     */
    @RequestMapping(value = "wxPayNotifyUrl",method = {RequestMethod.POST})
    @ResponseBody
    public String WXPayNotify(ServerRequest request){

        log.debug("WxPay notify");
        String result = null;
        try {
            result = payService.wxPayNotify(request);
        } catch (Exception e) {
            log.error("微信支付结果通知解析失败",e);
            return "FAIL";
        }

        log.debug(result);
        return result;
    }

    /**
     * 支付宝支付结果异步通知
     *

     * @return
     */
    @RequestMapping(value = "aliPayNotifyUrl",method = RequestMethod.POST)
    @ResponseBody
    public Mono<String> aliPayNotify(ServerWebExchange exchang) {
        R r = R.ok("登陆成功");
        Mono<MultiValueMap<String, String>> data = exchang.getFormData();

        return data.map(formData -> {
            log.debug("AliPay notify");
            String result = null;
            try {
              //  result = payService.aliPayNotify(exchang.getRequest().getQueryParams());
                return result;
            } catch (Exception e) {
                log.error("支付宝结果异步通知解析失败",e);
                return "FAIL";
            }
        });

    }

    /**
     * 支付宝支付同步通知返回地址
     *
     * @param request 支付宝回调请求
     * @return
     */
    @RequestMapping(value = "aliPayReturnUrl",method = RequestMethod.GET)
    public String aliPayReturn(ServerRequest request){

        log.debug("AliPay return");
        String result = null;
        try {
            result = payService.aliPayReturnUrl(request);
        } catch (Exception e) {
            log.error("支付宝同步通知解析失败",e);
            return "alipay_fail_url";
        }

        log.debug(result);
        return result;
    }


}
