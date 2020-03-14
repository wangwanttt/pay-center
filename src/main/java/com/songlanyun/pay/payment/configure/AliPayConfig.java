package com.songlanyun.pay.payment.configure;

import com.songlanyun.pay.dao.ProjectRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.stereotype.Component;

/**
 * @Description: aliPay 支付宝支付配置信息
 */

@Component
@Configuration
@PropertySource(value = "classpath:aliPay.properties")
@Data
public class AliPayConfig {

    private String appId;
    @Value("${alipay.server.url}")
    private String alipayUrl;
    /**
     * 开发者应用私钥
     */
    private String appPrivateKey;

    /**
     * 支付宝公钥
     */
    private String alipayPublicKey;

    /**
     * 支付宝回调通知地址(POST 请求)
     */
    @Value("${alipay.notify.url}")
    private String notifyUrl;

    /**
     * 支付宝支付成功后返回地址(GET 请求)
     */
    @Value("${alipay.return.url}")
    private String returnUrl;


    /**
     * 支付宝电脑网站支付下单并支付接口方法
     */
    private String methodPCSubject;

    /**
     * 支付宝手机网站支付下单并支付接口方法
     */
    private String methodWapSubmit;

    /**
     * 支付宝电脑网站支付销售产品码
     */
    private String productCodePC;

    /**
     * 支付宝手机网站支付销售产品码
     */
    private String productCodeWap;

    /**
     * 支付宝 APP 支付销售产品码
     */
    private String productCodeApp;

    /**
     * 支付宝支付订单标题
     */
    private String subject;

    /**
     * 参数返回格式，只支持 json
     */
    @Value("${alipay.format}")
    private String format;

    /**
     * 字符编码格式
     */
    @Value("${alipay.charset}")
    private String charset;

    /**
     * 签名算法,支持 RSA2 和 RSA，推荐使用 RSA2
     */
    @Value("${alipay.sign.type}")
    private String signType;


    /**
     * 支付宝异步回调通知成功时返回结果
     */
    @Value("${alipay.response.success}")
    private String responseSuccess;

    /**
     * 支付宝异步回调通知失败时返回结果
     */
    @Value("${alipay.response.fail}")
    private String responseFail;


    /**
     * 支付宝交易状态: 交易创建，等待买家付款
     */
    @Value("${alipay.tradeStatus.pay}")
    private String waitBuyerPay;

    /**
     * 支付宝交易状态: 未付款交易超时关闭，或支付完成后全额退款
     */
    @Value("${alipay.tradeStatus.closed}")
    private String tradeClosed;

    /**
     * 支付宝交易状态: 交易支付成功
     */
    @Value("${alipay.tradeStatus.success}")
    private String tradeSuccess;

    /**
     * 支付宝交易状态: 交易结束，不可退款
     */
    @Value("${alipay.tradeStatus.finished}")
    private String tradeFinished;

//    @Bean
//    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
//        return new PropertySourcesPlaceholderConfigurer();
//    }

    //从数据库中取值 ，为本类属性赋值

}