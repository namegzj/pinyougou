package com.pinyougou.item.activemq.listener;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.sellergoods.service.ItemCatService;
import com.pinyougou.vo.Goods;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.listener.adapter.AbstractAdaptableMessageListener;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

public class ItemTopicMessageListener extends AbstractAdaptableMessageListener {

    @Value("${ITEM_HTML_PATH}")
    private String ITEM_HTML_PATH;

    @Reference
    private GoodsService goodsService;

    @Reference
    private ItemCatService itemCatService;

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Override
    public void onMessage(Message message, Session session) throws JMSException {

        //接收消息（商品spu id数组）
        ObjectMessage objectMessage = (ObjectMessage) message;
        Long[] ids = (Long[]) objectMessage.getObject();

        //生成HTML页面
        if (ids != null && ids.length > 0) {
            for (Long id : ids) {
                genHtml(id);
            }
        }

    }

    private void genHtml(Long id) {

        try {
            Configuration configuration = freeMarkerConfigurer.getConfiguration();
            Template template = configuration.getTemplate("item.ftl");

            Map<String, Object> map = new HashMap<>();

            Goods goods = goodsService.findGoodsByIdAndStatus(id, "1");
            TbItemCat itemCat1 = itemCatService.findOne(goods.getGoods().getCategory1Id());
            map.put("itemCat1", itemCat1.getName());

            TbItemCat itemCat2 = itemCatService.findOne(goods.getGoods().getCategory2Id());
            map.put("itemCat2", itemCat2.getName());

            TbItemCat itemCat3 = itemCatService.findOne(goods.getGoods().getCategory3Id());
            map.put("itemCat3", itemCat3.getName());

            map.put("goods", goods.getGoods());
            map.put("goodsDesc", goods.getGoodsDesc());
            map.put("itemList", goods.getItemList());

            FileWriter fileWriter = new FileWriter(ITEM_HTML_PATH + id + ".html");
            template.process(map, fileWriter);

            fileWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
