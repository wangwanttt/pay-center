package com.songlanyun.pay.payment.common.util;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.songlanyun.pay.dao.ProjectRepository;
import com.songlanyun.pay.domain.Project;
import com.songlanyun.pay.error.GlobalException;
import com.songlanyun.pay.payment.common.constant.PayTypeConst;
import com.songlanyun.pay.payment.configure.AliPayConfig;
import com.songlanyun.pay.utils.ResponseInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * @Description: aliPay 支付宝支付工具类
 */
@Slf4j
@Component
public class AliPayManager {


    private static ProjectRepository projectRepository;

    @Autowired
    public AliPayManager(ProjectRepository projectRepository) {
        AliPayManager.projectRepository = projectRepository;
    }

    private volatile static AlipayClient alipayClient;

    /**
     * 创建电脑网站支付订单
     *
     * @param orderNo      订单编号
     * @param amount       订单金额(单位: 元)
     * @param aliPayConfig 支付宝配置信息
     * @return
     */
    public static String createPCOrder( String orderNo, String amount, AliPayConfig aliPayConfig,Project prjVo)
            throws AlipayApiException, JsonProcessingException {
        //实例化客户端
        AliPayConfig config = AliPayManager.getInfo(aliPayConfig,  prjVo);
        createAlipayClient(config);
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setReturnUrl(aliPayConfig.getReturnUrl());
        request.setNotifyUrl(aliPayConfig.getNotifyUrl());

        Map<String,Object> requestParams = new HashMap<>(16);
        requestParams.put("out_trade_no", orderNo);
        requestParams.put("product_code", aliPayConfig.getProductCodePC());
        requestParams.put("total_amount", amount);
        requestParams.put("subject", aliPayConfig.getSubject());

        requestParams.put("extra_common_param",prjVo.getId()+","+prjVo.getCallFunc());
        ObjectMapper mapper = new ObjectMapper();

        request.setBizContent(mapper.writeValueAsString(requestParams));
        AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
        if (response.isSuccess()) {
            log.info("支付宝电脑网站支付订单创建成功");
            return response.getBody();
        } else {
            log.info("支付宝电脑网站支付订单创建成功");
        }
        return null;
    }


    /**
     * 创建手机网站支付订单
     *
     * @param orderNo      订单编号
     * @param amount       订单金额(单位: 元)
     * @param aliPayConfig 支付宝配置信息
     * @return
     */
    public static String createWapOrder( String orderNo, String amount, AliPayConfig aliPayConfig,Project prjVo)
            throws AlipayApiException, JsonProcessingException {
        //实例化客户端
        //实例化客户端
        AliPayConfig config = AliPayManager.getInfo(aliPayConfig,  prjVo);
        createAlipayClient(config);
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        request.setReturnUrl(aliPayConfig.getReturnUrl());
        request.setNotifyUrl(aliPayConfig.getNotifyUrl());

        Map<String,Object> requestParams = new HashMap<>(16);
        requestParams.put("out_trade_no", orderNo);
        requestParams.put("product_code", aliPayConfig.getProductCodeWap());
        requestParams.put("total_amount", amount);
        requestParams.put("subject", aliPayConfig.getSubject());
        requestParams.put("extra_common_param",prjVo.getId()+","+prjVo.getCallFunc());
        ObjectMapper mapper = new ObjectMapper();

        request.setBizContent(mapper.writeValueAsString(requestParams));
        AlipayTradeWapPayResponse response = alipayClient.pageExecute(request);
        if (response.isSuccess()) {
            log.info("支付宝手机网站支付订单创建成功");
            return response.getBody();
        } else {
            log.info("支付宝手机网站支付订单创建成功");
        }
        return null;
    }

    /**
     * 创建 APP 支付订单
     *
     * @param orderNo      订单编号
     * @param amount       订单金额(单位: 元)
     * @param aliPayConfig 支付宝配置信息
     */
    public static String createAppOrder( String orderNo, String amount, AliPayConfig aliPayConfig, Project prjVo)
            throws JsonProcessingException, AlipayApiException {
        //实例化客户端
        AliPayConfig config = AliPayManager.getInfo(aliPayConfig,  prjVo);
        createAlipayClient(config);

        AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
        request.setReturnUrl(aliPayConfig.getReturnUrl());
        request.setNotifyUrl(aliPayConfig.getNotifyUrl());

        Map<String, Object> requestParams = new HashMap<>(16);
        requestParams.put("out_trade_no", orderNo);
        requestParams.put("product_code", aliPayConfig.getProductCodeApp());
        requestParams.put("total_amount", amount);
        requestParams.put("subject", aliPayConfig.getSubject());
        requestParams.put("extra_common_param",prjVo.getId()+","+prjVo.getCallFunc());
        ObjectMapper mapper = new ObjectMapper();

        request.setBizContent(mapper.writeValueAsString(requestParams));
        AlipayTradeAppPayResponse response = alipayClient.sdkExecute(request);
        if (response.isSuccess()) {
            log.info(orderNo+"支付宝APP支付订单创建成功");
            return response.getBody();
        } else {
            log.info(orderNo+"支付宝APP支付订单创建成功no issucess");
        }
        return null;
    }


    /**
     * 初始化支付宝支付客户端(AlipayClient)
     * 单例模式
     *
     * @param aliPayConfig
     * @return
     */
    public static AlipayClient createAlipayClient(AliPayConfig aliPayConfig) {
        if (alipayClient == null) {
            synchronized (AliPayManager.class) {
                if (alipayClient == null) {
                    alipayClient = new DefaultAlipayClient(aliPayConfig.getAlipayUrl(), aliPayConfig.getAppId(),
                            aliPayConfig.getAppPrivateKey(), aliPayConfig.getFormat(), aliPayConfig.getCharset(),
                            aliPayConfig.getAlipayPublicKey(), aliPayConfig.getSignType());
                }
            }
        }
        return alipayClient;
    }

    /**
     * 得到alipay 配置
     **/
    public static AliPayConfig getInfo(AliPayConfig config,   Project prjVo) {

        config.setAppId(prjVo.getAppid());
        config.setAlipayPublicKey(prjVo.getPubKey());
        config.setAppPrivateKey(prjVo.getPriKey());
        config.setSubject(prjVo.getSubject());
        config.setAlipayUrl(prjVo.getGateWay());

        if (prjVo.getPayWay() == 11) {  // 支支付宝电脑网站支付 --返回的是from表单
            config.setMethodWapSubmit("alipay.trade.page.pay");
            config.setProductCodePC("FAST_INSTANT_TRADE_PAY");
            config.setMethodPCSubject(prjVo.getSubject());
        }
        if (prjVo.getPayWay() == 12) {  // 支付宝手机网站支付
            config.setMethodWapSubmit("alipay.trade.wap.pay");
            config.setProductCodeWap("QUICK_WAP_WAY");
        }

        if (prjVo.getPayWay() == 13) {  // 支付宝app支付
            config.setProductCodeApp("QUICK_MSECURITY_PAY");

        }
        return config;

    }
    /**
     * 支付结果查询
     *
     * @param aliPayConfig 支付宝支付信息配置
     * @param orderNo  订单号
     * @return
     */
    public static Map<String,String> getPayResult(AliPayConfig aliPayConfig, String orderNo){
        Map<String, String> resultMap = new HashMap<>(16);
        AlipayClient alipayClient = new DefaultAlipayClient(aliPayConfig.getAlipayUrl(), aliPayConfig.getAppId(),
                aliPayConfig.getAppPrivateKey(), aliPayConfig.getFormat(), aliPayConfig.getCharset(),
                aliPayConfig.getAlipayPublicKey(), aliPayConfig.getSignType());
        AlipayTradeQueryRequest alipayTradeQueryRequest = new AlipayTradeQueryRequest();
        alipayTradeQueryRequest.setBizContent("{" + "\"out_trade_no\":\"" + orderNo + "\"" + "}");
        try {
            AlipayTradeQueryResponse alipayTradeQueryResponse = alipayClient.execute(alipayTradeQueryRequest);
            if (!alipayTradeQueryResponse.isSuccess()) {
                log.info("调用支付宝查询接口失败");
                return null;
            }
            // 交易状态
            resultMap.put("tradeStatus",alipayTradeQueryResponse.getTradeStatus());
            // 支付流水号
            if (!StringUtils.isEmpty(alipayTradeQueryResponse.getTradeNo())) {
                resultMap.put("payNo",alipayTradeQueryResponse.getTradeNo());
            }
            resultMap.put("payType", PayTypeConst.ORDER_PAY_TYPE_ALIPAY_NOTE);
        } catch (Exception e) {
            log.error("支付宝支付结果查询失败",e);
            return null;
        }
        return resultMap;
    }


    /**
     * 获取订单支付结果异步通知参数
     *
     * @param requestParams
     * @return
     */
    public static Map getNotifyParams(Map requestParams) {
        // 获取支付宝POST过来反馈信息
        Map<String,String> params = new HashMap<>(16);
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }
        return  params;
    }


}
