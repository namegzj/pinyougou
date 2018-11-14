package com.pinyougou.pay.service;

import java.util.Map;

public interface WeixinPayService {
    Map<String,String> createNative(String outTradeNo, String totalFee);

    Map<String,String> queryPayStatus(String outTradeNo);
}
