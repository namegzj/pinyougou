package com.pinyougou.search.service;

import com.pinyougou.pojo.TbItem;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {

    Map<String, Object> search(Map<String, Object> searchMap);

    void importItemList(List<TbItem> list);

    void deleteItemListByGoodsIdList(List<Long> goodsIdList);
}
