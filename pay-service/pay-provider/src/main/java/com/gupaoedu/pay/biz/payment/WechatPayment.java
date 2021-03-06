package com.gupaoedu.pay.biz.payment;

import com.gupaoedu.pay.biz.abs.BasePayment;
import com.gupaoedu.pay.biz.abs.Payment;
import com.gupaoedu.pay.biz.abs.PaymentContext;
import com.gupaoedu.pay.biz.abs.Validator;
import com.gupaoedu.pay.biz.payment.channel.wechatpay.WeChatBuildRequest;
import com.gupaoedu.pay.biz.payment.commons.HttpClientUtil;
import com.gupaoedu.pay.biz.payment.constants.AliPaymentConfig;
import com.gupaoedu.pay.biz.payment.constants.PayChannelEnum;
import com.gupaoedu.pay.biz.payment.constants.PaymentConstants;
import com.gupaoedu.pay.biz.payment.constants.WechatPaymentConfig;
import com.gupaoedu.pay.biz.payment.context.WechatPaymentContext;
import com.gupaoedu.pay.commons.AbstractRequest;
import com.gupaoedu.pay.commons.AbstractResponse;
import com.gupaoedu.pay.commons.PayReturnCodeEnum;
import com.gupaoedu.pay.dto.PaymentRequest;
import com.gupaoedu.pay.dto.PaymentResponse;
import com.gupaoedu.pay.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 腾讯课堂搜索 咕泡学院
 * 加群获取视频：608583947
 * 风骚的Michael 老师
 */
@Service
public class WechatPayment extends BasePayment {

    Logger LOG = LoggerFactory.getLogger(WechatPayment.class);

    @Autowired
    WechatPaymentConfig wechatPaymentConfig;

    @Resource(name="wechatPaymentValidator")
    private Validator validator;

    @Override
    public Validator getValidator() {
        return validator;
    }

    @Override
    public PaymentContext createContext(AbstractRequest request) {
        WechatPaymentContext wechatPaymentContext=new WechatPaymentContext();
        PaymentRequest paymentRequest=(PaymentRequest)request;
        wechatPaymentContext.setProductId(paymentRequest.getTradeNo());
        wechatPaymentContext.setSpbillCreateIp(paymentRequest.getSpbillCreateIp());
        wechatPaymentContext.setTradeType(PaymentConstants.TradeTypeEnum.NATIVE.getType());
        return wechatPaymentContext;
    }

    @Override
    public void prepare(AbstractRequest request, PaymentContext context) throws BizException {
        WechatPaymentContext wechatPaymentContext=(WechatPaymentContext)context;
        SortedMap<Object, Object> paraMap = new TreeMap<>();
        paraMap.put("body", wechatPaymentContext.getBody());
        paraMap.put("out_trade_no", wechatPaymentContext.getOutTradeNo());
        paraMap.put("total_fee", wechatPaymentContext.getTotalFee().intValue());//单位分
        paraMap.put("spbill_create_ip", wechatPaymentContext.getSpbillCreateIp());
        paraMap.put("appid", wechatPaymentConfig.getWechatAppid());
        paraMap.put("mch_id", wechatPaymentConfig.getWechatMch_id());
        paraMap.put("nonce_str", WeChatBuildRequest.getNonceStr());
        paraMap.put("trade_type", wechatPaymentContext.getTradeType());
        paraMap.put("product_id",wechatPaymentContext.getProductId());
        paraMap.put("device_info","WEB");
        paraMap.put("notify_url", wechatPaymentConfig.getWechatNotifyurl());// 此路径是微信服务器调用支付结果通知路径
        String sign = WeChatBuildRequest.createSign(paraMap, wechatPaymentConfig.getWechatMchsecret());
        paraMap.put("sign", sign);
        String xml = WeChatBuildRequest.getRequestXml(paraMap);
        wechatPaymentContext.setXml(xml);
    }


    @Override
    public AbstractResponse generalProcess(AbstractRequest request, PaymentContext context) throws BizException {
        PaymentResponse response=new PaymentResponse();

        WechatPaymentContext wechatPaymentContext=(WechatPaymentContext)context;

        String xmlStr = HttpClientUtil.httpPost(wechatPaymentConfig.getWechatUnifiedOrder(), wechatPaymentContext.getXml());
        LOG.info("wechatPayment. generalProcess response：" + xmlStr);
        Map<String, String> resultMap = WeChatBuildRequest.doXMLParse(xmlStr);
        if ("SUCCESS".equals(resultMap.get("return_code"))) {
            if ("SUCCESS".equals(resultMap.get("result_code"))) {
                //表示订单处理成功
                response.setPrepayId(resultMap.get("prepay_id"));
                response.setCodeUrl(resultMap.get("code_url"));
                response.setCode(PayReturnCodeEnum.SUCCESS.getCode());
                response.setMsg(PayReturnCodeEnum.SUCCESS.getMsg());
            } else {
                String errMsg = resultMap.get("err_code") + ":" + resultMap.get("err_code_des");
                response.setCode(PayReturnCodeEnum.PAYMENT_PROCESSOR_FAILED.getCode());
                response.setMsg(PayReturnCodeEnum.PAYMENT_PROCESSOR_FAILED.getMsg(errMsg));
            }
        } else {
            response.setCode(PayReturnCodeEnum.PAYMENT_PROCESSOR_FAILED.getCode());
            response.setMsg(PayReturnCodeEnum.PAYMENT_PROCESSOR_FAILED.getMsg(resultMap.get("return_msg")));
        }
        return response;
    }

    @Override
    public void afterProcess(AbstractRequest request, AbstractResponse respond, PaymentContext context) throws BizException {

    }

    @Override
    public String getPayChannel() {
        return PayChannelEnum.WECHAT_PAY.getCode();
    }
}
