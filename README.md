# pay-center
基于webflux的响应式支付中心

---------- 相对于传统的支付，webflux架构的支付能承载更多的用户，现在仅调试了 支付宝和微信App支付，其他类型支付会陆续添加 ---
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
