package com.songlanyun.pay.payment.service;

import com.songlanyun.pay.domain.Project;
import com.songlanyun.pay.payment.bean.PayBean;
import com.songlanyun.pay.payment.common.api.ApiResult;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * @Description: 支付业务
 * @Author: junqiang.lu
 * @Date: 2018/7/10
 */
@Service
public interface PayService {

    /**
     * 创建支付订单
     *
     * @param payBean json 订单信息(json 格式参数)
     * @return
     * @throws Exception
     */


    ApiResult createPayOrder(PayBean payBean, Project prjVo) throws Exception;

    /**
     * (主动)获取支付结果
     *
     * @param payBean 订单信息(json 格式参数)
     * @return
     * @throws Exception
     */
    ApiResult getPayResult(PayBean payBean, Project project) throws Exception;

    /**
     * 微信支付结果异步通知
     *
     * @param request 微信支付回调请求
     * @return 支付结果
     */
    String wxPayNotify(ServerRequest request);

    /**
     * 支付宝支付结果异步通知
     *
     * @param request 支付宝回调请求
     * @return
     */
    Mono<String> aliPayNotify(ServerRequest request);

    Mono<String> aliPayNotify(Map<String, String> params);

    /**
     * 支付宝支付同步通知返回地址
     *
     * @param request
     * @return
     */
    String aliPayReturnUrl(ServerRequest request);


}
