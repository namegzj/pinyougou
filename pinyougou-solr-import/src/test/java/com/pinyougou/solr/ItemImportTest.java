package com.pinyougou.solr;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pinyougou.mapper.ItemMapper;
import com.pinyougou.pojo.TbItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:spring/applicationContext-*.xml")
public class ItemImportTest {

    public static final String STATUS = "1";

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private SolrTemplate solrTemplate;

    @Test
    public void test() {
        //1、查询数据库中已启用的sku商品列表
        TbItem param = new TbItem();
        param.setStatus(STATUS);

        List<TbItem> itemList = itemMapper.select(param);
        //2、逐个遍历每个商品，将spec转换到specMap中
        for (TbItem item : itemList) {
            Map map = JSONObject.parseObject(item.getSpec(), Map.class);
            item.setSpecMap(map);
        }
        //3、批量导入到solr中
        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();
    }
}