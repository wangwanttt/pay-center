package com.songlanyun.pay.handler;

import com.songlanyun.pay.dao.ProjectRepository;
import com.songlanyun.pay.domain.Project;
import com.songlanyun.pay.error.GlobalException;
import com.songlanyun.pay.payment.bean.PayBean;
import com.songlanyun.pay.payment.common.api.ApiResult;
import com.songlanyun.pay.payment.service.PayService;
import com.songlanyun.pay.utils.ResponseInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class PayCallBackHandler {

    @Autowired
    private PayService payService;

    @Autowired
    private final ProjectRepository projectRepository;



    public PayCallBackHandler(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;

    }

    /**
     * 查询订单支付结果
     * 订单信息( json 格式数据)
     *
     * @return
     */
    public Mono<ServerResponse> getPayResult(ServerRequest request) {
        Mono fallback = Mono.error(new GlobalException(-200, "无查询记录 "));
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();
        Mono<PayBean> payParam = request.bodyToMono(PayBean.class);
        return payParam.flatMap(payBean -> {
                    try {
                        Mono<Project> prj = projectRepository.findById(payBean.getPrjId()).switchIfEmpty(fallback);
                        return prj.flatMap(project -> {
                            ApiResult apiResult = null;
                            try {
                                apiResult = payService.getPayResult(payBean,project);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(Mono.just(apiResult), ApiResult.class);
                        });
                    } catch (Exception e) {
                        log.error("订单支付结果查询失败", e);
                        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(ResponseInfo.info(-200, "订单支付结果查询失败"), ResponseInfo.class);
                    }
                }
        );

    }


    /**
     * 支付宝支付同步通知返回地址
     *
     * @param request 支付宝回调请求
     * @return
     */
    public  Mono<ServerResponse> aliPayReturn(ServerRequest request) {

        log.debug("AliPay 同步返回return");
        Mono<String> result = null;
        try {
            result= null;//payService.aliPayReturnUrl(request);
            return ServerResponse.ok().body(BodyInserters.fromValue(result), String.class);
        } catch (Exception e) {
            log.error("支付宝同步通知解析失败", e);
            return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(ResponseInfo.info(-200,"支付宝同步通知解析失败"), ResponseInfo.class);
          }

    }

    /**
     * 支付宝支付结果异步通知
        * @return
     */
    public Mono<ServerResponse> aliPayNotify(ServerRequest request){

            String result = null;
            try {
              return payService.aliPayNotify(request).flatMap(res->{
                   return ServerResponse.ok().body(BodyInserters.fromValue(res));//返回给支付宝 sucess 不让其重复异步通知
                }) ;

            } catch (Exception e) {
                log.error("支付宝结果异步通知解析失败", e);
                return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(ResponseInfo.info(-200, "支付宝结果异步通知解析失败"), ResponseInfo.class);
            }


    }


    /**
     * 微信支付结果异步通知
     *
     * @param request 微信支付回调请求
     * @return
     */
    @RequestMapping(value = "wxPayNotifyUrl", method = {RequestMethod.POST})
    @ResponseBody
    public String WXPayNotify(ServerRequest request) {

        log.debug("WxPay notify");
        String result = null;
        try {
            result = payService.wxPayNotify(request);
        } catch (Exception e) {
            log.error("微信支付结果通知解析失败", e);
            return "FAIL";
        }

        log.debug(result);
        return result;
    }


}
