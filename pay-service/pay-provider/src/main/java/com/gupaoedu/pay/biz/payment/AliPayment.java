package com.gupaoedu.pay.biz.payment;

import com.alipay.api.AlipayRequest;
import com.gupaoedu.pay.biz.abs.BasePayment;
import com.gupaoedu.pay.biz.abs.Payment;
import com.gupaoedu.pay.biz.abs.PaymentContext;
import com.gupaoedu.pay.biz.abs.Validator;
import com.gupaoedu.pay.biz.payment.channel.alipay.AlipayBuildRequest;
import com.gupaoedu.pay.biz.payment.constants.AliPaymentConfig;
import com.gupaoedu.pay.biz.payment.constants.PayChannelEnum;
import com.gupaoedu.pay.biz.payment.context.AliPaymentContext;
import com.gupaoedu.pay.commons.AbstractRequest;
import com.gupaoedu.pay.commons.AbstractResponse;
import com.gupaoedu.pay.commons.PayReturnCodeEnum;
import com.gupaoedu.pay.dto.PaymentRequest;
import com.gupaoedu.pay.dto.PaymentResponse;
import com.gupaoedu.pay.exception.BizException;
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
public class AliPayment extends BasePayment {

    @Resource(name="aliPaymentValidator")
    private Validator validator;

    @Autowired
    AliPaymentConfig aliPaymentConfig;

    @Override
    public Validator getValidator() {
        return validator;
    }

    @Override
    public PaymentContext createContext(AbstractRequest request) {
        AliPaymentContext aliPaymentContext=new AliPaymentContext();
        PaymentRequest paymentRequest=(PaymentRequest)request;
        aliPaymentContext.setSubject(paymentRequest.getSubject());
        aliPaymentContext.setOutTradeNo(paymentRequest.getTradeNo());
        aliPaymentContext.setTotalFee(paymentRequest.getTotalFee());
        return aliPaymentContext;
    }

    @Override
    public void prepare(AbstractRequest request, PaymentContext context) throws BizException {
        SortedMap<String, Object> sParaTemp =new TreeMap<String, Object>();
        AliPaymentContext aliPaymentContext=(AliPaymentContext)context;
        sParaTemp.put("service", aliPaymentConfig.getAli_service());
        sParaTemp.put("partner", aliPaymentConfig.getAli_partner());
        sParaTemp.put("seller_email", aliPaymentConfig.getSeller_email());
        sParaTemp.put("seller_id", aliPaymentConfig.getSeller_id());
        sParaTemp.put("_input_charset", aliPaymentConfig.getInput_charset());
        sParaTemp.put("payment_type", 1);
        sParaTemp.put("it_b_pay", aliPaymentConfig.getIt_b_pay());
        sParaTemp.put("notify_url", aliPaymentConfig.getNotify_url());
        sParaTemp.put("return_url",aliPaymentConfig.getReturn_url());
        sParaTemp.put("out_trade_no",aliPaymentContext.getOutTradeNo());
        sParaTemp.put("subject", aliPaymentContext.getSubject());
        sParaTemp.put("total_fee", aliPaymentContext.getTotalFee().doubleValue());
        aliPaymentContext.setsParaTemp(sParaTemp);
    }


    @Override
    public AbstractResponse generalProcess(AbstractRequest request, PaymentContext context) throws BizException {
        PaymentResponse response=new PaymentResponse();
        AliPaymentContext aliPaymentContext=(AliPaymentContext)context;
        Map<String, Object> sPara = AlipayBuildRequest.buildRequestParam(aliPaymentContext.getsParaTemp(),aliPaymentConfig);
        String strPara = AlipayBuildRequest.buildRequest(sPara, "get", "确认",aliPaymentConfig);
        response.setCode(PayReturnCodeEnum.SUCCESS.getCode());
        response.setMsg(PayReturnCodeEnum.SUCCESS.getMsg());
        response.setHtmlStr(strPara);
        return response;
    }

    @Override
    public void afterProcess(AbstractRequest request, AbstractResponse respond, PaymentContext context) throws BizException {

    }

    @Override
    public String getPayChannel() {
        return PayChannelEnum.ALI_PAY.getCode();
    }
}
