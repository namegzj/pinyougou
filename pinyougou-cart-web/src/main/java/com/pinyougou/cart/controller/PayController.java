package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.vo.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/pay")
@RestController
public class PayController {

    @Reference
    private OrderService orderService;

    @Reference
    private WeixinPayService weixinPayService;


    @GetMapping("createNative")
    public Map<String, String> createNative(String outTradeNo) {
        //1、获取支付日志
        TbPayLog payLog = orderService.findPayLogByOutTradeNo(outTradeNo);
        if (payLog != null) {
            String totalFee = payLog.getTotalFee() + "";
            //调用支付统一下单方法生成支付订单并返回信息
            return weixinPayService.createNative(outTradeNo, totalFee);
        }
        return new HashMap<>();
    }

    @GetMapping("queryPayStatus")
    public Result queryPayStatus(String outTradeNo) {
        Result result = Result.fail("支付失败");
        try {
            int count = 0;
            while (true) {

                Map<String, String> resultMap = weixinPayService.queryPayStatus(outTradeNo);

                if (resultMap == null) {
                    break;
                }

                if ("SUCCESS".equals(resultMap.get("trade_state"))) {
    //                如果支付成功则修改本次交易中对应的所有订单的支付状态为已支付
                    orderService.updateOrderStatus(outTradeNo, resultMap.get("transaction_id"));
                    result =  Result.ok("支付成功");
                    break;
                }

                count++;
                if (count > 60) {
                    result = Result.fail("二维码超时");
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result ;

    }

}
