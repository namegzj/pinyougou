package com.pinyougou.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.sellergoods.service.ItemCatService;
import com.pinyougou.vo.Goods;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ItemController {

    @Reference
    private ItemCatService itemCatService;

    @Reference
    private GoodsService goodsService;

    @GetMapping("/{goodsId}")
    public ModelAndView toItemPage(@PathVariable("goodsId")Long goodsId) {
        //接收商品spu id，根据该Id查询3级分类中文名称、基本、描述、sku列表
        ModelAndView mv = new ModelAndView("item");

        Goods goods = goodsService.findGoodsByIdAndStatus(goodsId, "1");
        //一级分类
        TbItemCat itemCat1 =  itemCatService.findOne(goods.getGoods().getCategory1Id());
        mv.addObject("itemCat1", itemCat1.getName());
        //二级分类
        TbItemCat itemCat2 = itemCatService.findOne(goods.getGoods().getCategory2Id());
        mv.addObject("itemCat2", itemCat2.getName());
        //三级分类
        TbItemCat itemCat3 = itemCatService.findOne(goods.getGoods().getCategory3Id());
        mv.addObject("itemCat3", itemCat3);

        //基本信息
        mv.addObject("goods", goods.getGoods());
        //描述信息
        mv.addObject("goodsDesc", goods.getGoodsDesc());

        //sku列表
        mv.addObject("itemList", goods.getItemList());
        return mv;
    }
}
