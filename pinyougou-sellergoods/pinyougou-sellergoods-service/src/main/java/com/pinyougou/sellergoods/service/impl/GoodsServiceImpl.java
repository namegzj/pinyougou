package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.Goods;
import com.pinyougou.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Service(interfaceClass = GoodsService.class)
public class GoodsServiceImpl extends BaseServiceImpl<TbGoods> implements GoodsService {


    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private GoodsDescMapper goodsDescMapper;

    @Autowired
    private ItemCatMapper itemCatMapper;

    @Autowired
    private SellerMapper sellerMapper;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private ItemMapper itemMapper;


    @Override
    public PageResult search(Integer page, Integer rows, TbGoods goods) {
        PageHelper.startPage(page, rows);

        Example example = new Example(TbGoods.class);
        Example.Criteria criteria = example.createCriteria();
        /*if(!StringUtils.isEmpty(goods.get***())){
            criteria.andLike("***", "%" + goods.get***() + "%");
        }*/

        List<TbGoods> list = goodsMapper.selectByExample(example);
        PageInfo<TbGoods> pageInfo = new PageInfo<>(list);

        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public void addGoods(Goods goods) {
        //保存基本信息
        add(goods.getGoods());
        //描述信息
        goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
        goodsDescMapper.insertSelective(goods.getGoodsDesc());

       //保存商品sku列表
        saveItemList(goods);
    }

    private void saveItemList(Goods goods) {
        if ("1".equals(goods.getGoods().getIsEnableSpec())) {
            //Sku 列表
            for (TbItem item : goods.getItemList()) {
                String title = goods.getGoods().getGoodsName();

                // 组合规格选项形成 SKU 标题
                Map<String, Object> map = JSONArray.parseObject(item.getSpec());
                Set<Map.Entry<String, Object>> entries = map.entrySet();

                for (Map.Entry<String, Object> entry : entries) {
                    title += " " + entry.getValue().toString();
                }
                item.setTitle(title);
                setItemValue(item, goods);
                itemMapper.insertSelective(item);
            }
        } else {
            // 如果没有启动规格，则只存在一条 SKU 信息
            TbItem tbItem = new TbItem();
            tbItem.setTitle(goods.getGoods().getGoodsName());
            tbItem.setPrice(goods.getGoods().getPrice());
            tbItem.setNum(9999);
            tbItem.setStatus("0");
            tbItem.setIsDefault("1");
            tbItem.setSpec("{}");
            setItemValue(tbItem, goods);
            itemMapper.insertSelective(tbItem);
        }
    }

    private void setItemValue(TbItem item, Goods goods) {
        //图片
        List<Map> imgList = JSONArray.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
        if (imgList != null && imgList.size() > 0) {
            //将商品的第一张图片作为sku的图片
            item.setImage(imgList.get(0).get("url").toString());
        }

        //商品分类id
        item.setCategoryid(goods.getGoods().getCategory3Id());

        //商品分类名称
        TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
        item.setCategory(itemCat.getName());

        //创建时间
        item.setCreateTime(new Date());

        //更新时间
        item.setUpdateTime(item.getCreateTime());

        //spu 商品ID
        item.setGoodsId(goods.getGoods().getId());

        //商家ID
        item.setSellerId(goods.getGoods().getSellerId());

        //商家名称
        TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
        item.setSeller(seller.getName());

        //品牌名称
        TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
        item.setBrand(brand.getName());

    }
}
