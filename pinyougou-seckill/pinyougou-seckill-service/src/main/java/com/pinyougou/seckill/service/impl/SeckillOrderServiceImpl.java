package com.pinyougou.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.common.util.RedisLock;
import com.pinyougou.mapper.SeckillGoodsMapper;
import com.pinyougou.mapper.SeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;

@Service(interfaceClass = SeckillOrderService.class)
public class SeckillOrderServiceImpl extends BaseServiceImpl<TbSeckillOrder> implements SeckillOrderService {

    private static final String SECKILL_ORDERS = "SECKILL_ORDERS";
    private static final String SECKILL_GOODS = "SECKILL_GOODS";
    @Autowired
    private SeckillOrderMapper seckillOrderMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private IdWorker idWorker;

    @Override
    public PageResult search(Integer page, Integer rows, TbSeckillOrder seckillOrder) {
        PageHelper.startPage(page, rows);

        Example example = new Example(TbSeckillOrder.class);
        Example.Criteria criteria = example.createCriteria();
        /*if(!StringUtils.isEmpty(seckillOrder.get***())){
            criteria.andLike("***", "%" + seckillOrder.get***() + "%");
        }*/

        List<TbSeckillOrder> list = seckillOrderMapper.selectByExample(example);
        PageInfo<TbSeckillOrder> pageInfo = new PageInfo<>(list);

        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    public String submitOrder(String userId, Long seckillId) throws InterruptedException {
        String seckillOrderId = "";
        //加分布式锁
        RedisLock redisLock = new RedisLock(redisTemplate);
        if (redisLock.lock(seckillId.toString())) {
            TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps(SECKILL_GOODS).get(seckillId);
            if (seckillGoods == null) {
                throw new RuntimeException("商品不存在");
            }
            if (seckillGoods.getStockCount() == 0) {
                throw new RuntimeException("已抢完");
            }
            seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
            if (seckillGoods.getStockCount() > 0) {
//                如果库存大于0，则需要更新秒杀商品到redis
                redisTemplate.boundHashOps(SECKILL_GOODS).put(seckillId, seckillGoods);
            } else {
//                如果库存为0,则需要更新回数据库,并删除redis
                seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
                redisTemplate.boundHashOps(SECKILL_GOODS).delete(seckillId);
            }
            //释放锁
            redisLock.unlock(seckillId.toString());

            //生成秒杀订单并存入redis
            TbSeckillOrder seckillOrder = new TbSeckillOrder();
            seckillOrder.setId(idWorker.nextId());
            seckillOrderId = seckillOrder.getId().toString();

            seckillOrder.setStatus("0");
            seckillOrder.setSeckillId(seckillId);
            seckillOrder.setCreateTime(new Date());
            seckillOrder.setUserId(userId);
            seckillOrder.setSellerId(seckillGoods.getSellerId());
            //秒杀价
            seckillOrder.setMoney(seckillGoods.getCostPrice());

            redisTemplate.boundHashOps(SECKILL_ORDERS).put(seckillOrderId,seckillOrder);

        }
        return seckillOrderId;
    }

    @Override
    public TbSeckillOrder getSeckillOrderInRedisByOrderId(String outTradeNo) {

        return (TbSeckillOrder) redisTemplate.boundHashOps(SECKILL_ORDERS).get(outTradeNo);
    }

    @Override
    public void saveSeckillOrderInRedisToDb(String outTradeNo, String trade_state) {
        TbSeckillOrder seckillOrder = getSeckillOrderInRedisByOrderId(outTradeNo);
        //2、更新秒杀订单的支付状态
        seckillOrder.setStatus("1");
        seckillOrder.setPayTime(new Date());
        seckillOrder.setTransactionId(trade_state);
        //3、保存订单到数据库中
        seckillOrderMapper.insertSelective(seckillOrder);
        //4、删除redis中的秒杀订单
        redisTemplate.boundHashOps(SECKILL_ORDERS).delete(outTradeNo);

    }

    @Override
    public void deleteSeckillOrderInRedis(String outTradeNo) throws InterruptedException {
        TbSeckillOrder seckillOrder = getSeckillOrderInRedisByOrderId(outTradeNo);
        //加分布式锁
        RedisLock redisLock = new RedisLock(redisTemplate);
        if (redisLock.lock(seckillOrder.getSeckillId().toString())) {
            //查询redis中订单对应的秒杀商品
            TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps(SECKILL_GOODS).get(seckillOrder.getSeckillId());
            if (seckillGoods == null) {
                //从Mysql中查询秒杀商品
                seckillGoods = seckillGoodsMapper.selectByPrimaryKey(seckillGoods.getSellerId());
            }
            seckillGoods.setStockCount(seckillGoods.getStockCount() + 1 );
            redisTemplate.boundHashOps(SECKILL_GOODS).put(seckillGoods.getId(),seckillGoods);
            //释放分布式锁
            redisLock.unlock(seckillOrder.getSeckillId().toString());
            //删除redis中的订单
            redisTemplate.boundHashOps(SECKILL_ORDERS).delete(outTradeNo);
        }
    }


}
