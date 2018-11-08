package com.pinyougou.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.sellergoods.service.ItemCatService;
import com.pinyougou.vo.Goods;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RequestMapping("/test")
@RestController
public class PageTestController {
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Reference
    private ItemCatService itemCatService;

    @Reference
    private GoodsService goodsService;

    @Value("${ITEM_HTML_PATH}")
    private String ITEM_HTML_PATH;

    @GetMapping("/audit")
    public String audit(Long [] goodsIds) {
        for (Long goodsId : goodsIds) {
            genItemHtml(goodsId);
        }
        return "success";
    }

    @GetMapping("/delete")
    public String delete(Long[] goodsIds) {
        for (Long goodsId : goodsIds) {
            String fileName = ITEM_HTML_PATH + goodsId + ".html";
            File file = new File(fileName);
            if (file.exists()) {
                file.delete();
            }
        }
        return "success";

    }

    private void genItemHtml(Long goodsId) {

        try {
            //获取freemarker配置对象
            Configuration configuration = freeMarkerConfigurer.getConfiguration();
            //获取模板
            Template template = configuration.getTemplate("item.ftl");
            //数据
            Map<String, Object> map = new HashMap<>();
            //查询基本信息
            Goods goods = goodsService.findGoodsByIdAndStatus(goodsId, "1");

            //一级分类
            TbItemCat itemCat1 = itemCatService.findOne(goods.getGoods().getCategory1Id());
            map.put("itemCat1",itemCat1.getName());

            //二级分类
            TbItemCat itemCat2 = itemCatService.findOne(goods.getGoods().getCategory2Id());
            map.put("itemCat2", itemCat2.getName());

            //三级分类
            TbItemCat itemCat3 = itemCatService.findOne(goods.getGoods().getCategory3Id());
            map.put("itemCat3", itemCat3.getName());

            //描述信息
            map.put("goodsDesc", goods.getGoodsDesc());

            // sku列表
            map.put("itemList", goods.getItemList());

            FileWriter fileWriter = new FileWriter(ITEM_HTML_PATH + goodsId + ".html");
            //输出
            template.process(map,fileWriter);

            fileWriter.close();


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
