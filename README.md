# pay-center
基于webflux的响应式支付中心 --- 欢迎和我交流

---------- 相对于传统的spring mvc一个请求一个线程来填满内存，webflux架构的支付能承载更多的用户，此项目更能接入微服务集，性能更好--

现在仅调试了 支付宝和微信App支付，其他类型支付会陆续添加 ---
1、相应的APP测试代码：
uni.requestPayment({
						provider: 'wxpay'/'alipay',
						orderInfo: orderInfo,
						success: function(res) {
							alert( JSON.stringify(res))
							console.log('success:' + JSON.stringify(res));
						},
						fail: function(err) {
								alert( JSON.stringify(err))
							console.log('fail:' + JSON.stringify(err));
						}
					});


2、使用方法
 通过支付后台管理系统，设置在微信或支付宝申请的appid,appserct等，然后设置callFunc--即支付成功后提供给支付中心的调用你自己项目的方法（此方法也是响应式开发，负载杠杠的）
 
 3、调用支付接口
 app端直接调用  http://ip.xx.xx.xx/api/v1/pay 
 post参数对象 ：
 let aliPayParam = {
					"orderNo": 订单号【注意不要有小数点】 ,
					"amount": 0.01,
					"prjId": "5e69faea70c5654c674e9c8a" //后台管理系统设置的支付参数类id
				};
即可


4、查询某订单情况接口
http://ip.xx.xx.xx//api/v1/getOrder/{订单号id}


附：支付成功后返回给你的项目的对象 AliTrade重要返回信息说明：
public class AliTrade { 
 
    /**
     * 支付宝分配给开发者的应用Id
     */
    private String appId;
  

    /*
        业务参数
     */

    /**
     * 微信支付交易流水号或支付宝的自定义订单号
     */
    private String outTradeNo;

    /**
     * 商品名称
     */
    private String subject;

    /**
     * 订单号。------ 【是自定义的订单号，非系统生成的交易流水号】
     */
    private String tradeNo;

     
    /**
     * 交易状态 或微信的 交易类型	如app,web之类
     */
    private String tradeStatus;

    /**
     * 卖家支付宝用户号
     */
    private String sellerId;

    /**
   
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
     * 商品描述
     */
    private String body; 

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

    /** 是否已调用 第三方接口 ------相当重要，1---表示已经调用了你的提供给支付中心的方法**/
    private int isNotify=0;


}
