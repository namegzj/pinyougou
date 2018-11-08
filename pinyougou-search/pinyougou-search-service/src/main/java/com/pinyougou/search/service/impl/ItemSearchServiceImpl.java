package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public Map<String, Object> search(Map<String, Object> searchMap) {
        Map<String, Object> resultMap = new HashMap<>();
        //创建查询条件对象
       // SimpleQuery query = new SimpleQuery();
        SimpleHighlightQuery query = new SimpleHighlightQuery();

        //处理搜索关键字中的空格问题
        if (!StringUtils.isEmpty(searchMap.get("keywords"))) {
            searchMap.put("keywords", searchMap.get("keywords").toString().replaceAll(" ", ""));
        }

        //查询条件
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);



        //根据分类查询
        if (!StringUtils.isEmpty(searchMap.get("category"))) {
            Criteria categoryCriteria = new Criteria("item_category").is(searchMap.get("category"));
            SimpleFacetQuery simpleFacetQuery = new SimpleFacetQuery(categoryCriteria);
            query.addFilterQuery(simpleFacetQuery);
        }
        //根据品牌查询
        if (!StringUtils.isEmpty(searchMap.get("brand"))) {
            Criteria brandCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            SimpleFacetQuery simpleFacetQuery = new SimpleFacetQuery(brandCriteria);
            query.addFilterQuery(simpleFacetQuery);
        }
        //根据规格过滤查询
        if (searchMap.get("spec") != null) {
            Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
            Set<Map.Entry<String, String>> entrySet = specMap.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                Criteria specCriteria = new Criteria("item_spec_" + entry.getKey()).is(entry.getValue());
                SimpleFacetQuery simpleFacetQuery = new SimpleFacetQuery(specCriteria);
                query.addFilterQuery(simpleFacetQuery);
            }
        }
        //根据价格区间过滤查询
        if (!StringUtils.isEmpty(searchMap.get("price"))) {
            String[] prices = searchMap.get("price").toString().split("-");
            //价格大于等于起始价格
            Criteria priceStartCriteria = new Criteria("item_price").greaterThanEqual(prices[0]);
            SimpleFacetQuery startQuery = new SimpleFacetQuery(priceStartCriteria);
            query.addFilterQuery(startQuery);
            //结束价格
            if (!"*".equals(prices[1])) {
                Criteria priceEndCriteria = new Criteria("item_price").lessThanEqual(prices[1]);
                SimpleFacetQuery endQuery = new SimpleFacetQuery(priceEndCriteria);
                query.addFilterQuery(endQuery);
            }
        }

        int pageNo = 1;
        int pageSize = 20;

        if (!StringUtils.isEmpty(searchMap.get("pageNo"))) {
            pageNo = Integer.parseInt(searchMap.get("pageNo").toString());
        }

        if (!StringUtils.isEmpty(searchMap.get("pageSize"))) {
            pageSize = Integer.parseInt(searchMap.get("pageSize").toString());
        }
        //起始索引号 = （当前页号-1）*页大小
        query.setOffset((pageNo - 1) * pageSize);
        //页大小
        query.setRows(pageSize);

        //排序查询
        if (!StringUtils.isEmpty(searchMap.get("sortField"))&& !StringUtils.isEmpty(searchMap.get("sort"))) {
            String sortOrder = searchMap.get("sort").toString();
            //创建排序对象sort；参数1：排序的序列；参数2：排序的域名
            Sort sort = new Sort("DESC".equals(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC, "item_" + searchMap.get("sortField"));

            query.addSort(sort);

        }


        //设置高亮
        HighlightOptions highlightOptions = new HighlightOptions();
        //添加一个要高亮显示的域名
        highlightOptions.addField("item_title");
        //高亮起始标签
        highlightOptions.setSimplePrefix("<font style='color:red'>");
        //高亮结束标签
        highlightOptions.setSimplePostfix("</font>");
        query.setHighlightOptions(highlightOptions);

        //ScoredPage<TbItem> scoredPage = solrTemplate.queryForPage(query, TbItem.class);

        //itemList+pageInfo ...
        HighlightPage<TbItem> highlightPage = solrTemplate.queryForHighlightPage(query, TbItem.class);

        //itemList===List<TbItem>
        List<HighlightEntry<TbItem>> highlightEntryList = highlightPage.getHighlighted();

      /*  for (HighlightEntry<TbItem> highlightEntry : highlightEntryList) {
            TbItem item = highlightEntry.getEntity();
            //highlightOptions.addField("item_title");
            List<HighlightEntry.Highlight> highlightList = highlightEntry.getHighlights();
            HighlightEntry.Highlight highlight = highlightList.get(0);
            //item_keywords
            List<String> snippletList = highlight.getSnipplets();
            String title = snippletList.get(0);
            item.setTitle(title);
        }*/
        if (highlightEntryList != null && highlightEntryList.size() > 0) {
            for (HighlightEntry<TbItem> entry : highlightEntryList) {
                if (entry.getHighlights().size() > 0 && entry.getHighlights().get(0).getSnipplets() != null) {
                    //设置的是返回回来的那些商品标题
                    entry.getEntity().setTitle(entry.getHighlights().get(0).getSnipplets().get(0));
                }
            }
        }
        resultMap.put("rows", highlightPage.getContent());
        //总页数
        resultMap.put("totalPages", highlightPage.getTotalPages());
        //总记录数
        resultMap.put("total", highlightPage.getTotalElements());


        return resultMap;
    }

    @Override
    public void importItemList(List<TbItem> list) {
        for (TbItem item : list) {
            Map map = JSON.parseObject(item.getSpec(), Map.class);
            item.setSpecMap(map);
        }
        solrTemplate.saveBeans(list);
        solrTemplate.commit();

    }

    @Override
    public void deleteItemListByGoodsIdList(List<Long> goodsIdList) {
        Criteria criteria = new Criteria("item_goodsid").in(goodsIdList);
        SimpleFacetQuery query = new SimpleFacetQuery(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();

    }
}
