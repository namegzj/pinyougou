package com.pinyougou.seckill.service;

import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.service.BaseService;
import com.pinyougou.vo.PageResult;

import java.util.List;

public interface SeckillGoodsService extends BaseService<TbSeckillGoods> {

    PageResult search(Integer page, Integer rows, TbSeckillGoods seckillGoods);

    List<TbSeckillGoods> findList();

    /**
     * 根据秒杀商品id查询在redis中的秒杀商品
     * @param id
     * @return
     */
    TbSeckillGoods findOneInRedisById(Long id);
}