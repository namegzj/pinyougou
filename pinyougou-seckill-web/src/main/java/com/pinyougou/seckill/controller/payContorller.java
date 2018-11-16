package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import com.pinyougou.vo.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class payContorller {


    @Reference
    private SeckillOrderService seckillOrderService;
    @Reference
    private WeixinPayService weixinPayService;

    @GetMapping("/createNative")
    public Map<String, String> createNative(String outTradeNo) {
        TbSeckillOrder order = seckillOrderService.getSeckillOrderInRedisByOrderId(outTradeNo);
        if (order != null) {
            String totalFee = (long) (order.getMoney().doubleValue() * 100) + "";
            return weixinPayService.createNative(outTradeNo, totalFee);
        }
        return new HashMap<>();
    }

    @GetMapping("/queryPayStatus")
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
                    seckillOrderService.saveSeckillOrderInRedisToDb(outTradeNo, resultMap.get("trade_state"));
                    result = Result.ok("支付成功");
                    break;
                }
                count++;
                if (count > 20) {
                    Map<String, String> map = weixinPayService.closeOrder(outTradeNo);
                    if (map != null && "ORDERPAID".equals(map.get("err_code"))) {
                        seckillOrderService.saveSeckillOrderInRedisToDb(outTradeNo,resultMap.get("trade_state"));
                        result = Result.ok("支付成功");
                        break;

                    }
                //如果微信那边订单被关闭了；则需要删除redis中的订单
                seckillOrderService.deleteSeckillOrderInRedis(outTradeNo);
                    result = Result.fail("支付超时");
                break;
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
