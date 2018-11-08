package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.service.BaseService;
import com.pinyougou.vo.Goods;
import com.pinyougou.vo.PageResult;

import java.util.List;

public interface GoodsService extends BaseService<TbGoods> {
    PageResult search(Integer page, Integer rows, TbGoods goods);


    void addGoods(Goods goods);

    Goods findGoodsById(Long id);

    void updateGoods(Goods goods);

    void updateStatus(Long[] ids, String status);

    void deleteGoodsByIds(Long[] ids);

    void updateMarketable(Long[] ids, String marketable);

    List<TbItem> findItemListByGoodsIdsAndStatus(Long[] ids, String status);

    Goods findGoodsByIdAndStatus(Long goodsId, String status);
}
