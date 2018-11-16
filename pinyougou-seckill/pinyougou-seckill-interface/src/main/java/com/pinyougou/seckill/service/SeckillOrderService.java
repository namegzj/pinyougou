package com.pinyougou.seckill.service;

import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.service.BaseService;
import com.pinyougou.vo.PageResult;

public interface SeckillOrderService extends BaseService<TbSeckillOrder> {

    PageResult search(Integer page, Integer rows, TbSeckillOrder seckillOrder);

    String submitOrder(String userId, Long seckillId) throws InterruptedException;

    TbSeckillOrder getSeckillOrderInRedisByOrderId(String outTradeNo);

    void saveSeckillOrderInRedisToDb(String outTradeNo, String trade_state);

    void deleteSeckillOrderInRedis(String outTradeNo) throws InterruptedException;
}