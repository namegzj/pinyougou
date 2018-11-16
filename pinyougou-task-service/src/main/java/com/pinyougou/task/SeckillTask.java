package com.pinyougou.task;

import com.pinyougou.mapper.SeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class SeckillTask {
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Scheduled(cron = "0/3 * * * * *")
    public void refreshSeckillGoods() {
        //查询在redis中的秒杀商品id集合
        Set idsSet = redisTemplate.boundHashOps("SECKILL_GOODS").keys();
        List ids = new ArrayList(idsSet);


        // System.out.println(new Date());
    //每分钟执行查询秒杀商品数据库表，将审核通过的，库存大于 0，开始时间小于
        //等于当前时间，结束时间大于当前时间并且缓存中不存在的秒杀商品存入缓存
        Example example = new Example(TbSeckillGoods.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("status", "1");
        criteria.andGreaterThan("stockCount", 0);

        criteria.andLessThanOrEqualTo("startTime", new Date());
        criteria.andGreaterThan("endTime", new Date());

        if (ids.size() > 0) {
            criteria.andNotIn("id", ids);
        }
        List<TbSeckillGoods> seckillGoods = seckillGoodsMapper.selectByExample(example);

        if (seckillGoods != null && seckillGoods.size() > 0) {
            for (TbSeckillGoods seckillGood : seckillGoods) {
                redisTemplate.boundHashOps("SECKILL_GOODS").put(seckillGood.getId(),seckillGood);
            }

        }
    }

    @Scheduled(cron = "0/2 * * * * ?")
    public void removeSeckillGoods(){
     //每秒钟都去检查 redis 中的商品是否过期；若过期则从 redis 中移除秒杀商品并将该商品更新到数据库
        List<TbSeckillGoods> seckillGoods =  redisTemplate.boundHashOps("SECKILL_GOODS").values();

        if (seckillGoods != null && seckillGoods.size() > 0) {
            for (TbSeckillGoods seckillGood : seckillGoods) {
                if (seckillGood.getEndTime().getTime() < System.currentTimeMillis()) {

                    seckillGoodsMapper.updateByPrimaryKeySelective(seckillGood);

                    redisTemplate.boundHashOps("ECKILL_GODDS").delete(seckillGood.getId());
                }
            }
        }

    }

}
