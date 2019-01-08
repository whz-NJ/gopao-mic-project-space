package com.gupaoedu.pay.services;

import com.gupaoedu.pay.TransactionPayService;
import com.gupaoedu.pay.biz.abs.BasePayment;
import com.gupaoedu.pay.dto.PaymentRequest;
import com.gupaoedu.pay.dto.PaymentResponse;
import com.gupaoedu.pay.exception.ExceptionUtil;
import com.gupaoedu.pay.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 腾讯课堂搜索 咕泡学院
 * 加群获取视频：608583947
 * 风骚的Michael 老师
 */
@Service("transactionPayService")
public class TransactionPayServiceImpl implements TransactionPayService {

    private final Logger Log = LoggerFactory.getLogger(TransactionPayServiceImpl.class);


    @Override
    public PaymentResponse execPay(PaymentRequest request) {
        PaymentResponse paymentResponse=new PaymentResponse();
        try {
            paymentResponse=(PaymentResponse) BasePayment.paymentMap.
                    get(request.getPayChannel()).process(request);
        }catch (Exception e){
            Log.error("execPay occur exception:"+e);
            ServiceException serviceException=(ServiceException) ExceptionUtil.handlerException4biz(e);
            paymentResponse.setCode(serviceException.getErrorCode());
            paymentResponse.setMsg(serviceException.getErrorMessage());
        }finally {
            Log.error("execPay occur exception:");
        }
        return paymentResponse;
    }
}
