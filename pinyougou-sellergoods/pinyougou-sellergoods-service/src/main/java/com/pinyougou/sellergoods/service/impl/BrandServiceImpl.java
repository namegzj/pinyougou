package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.sellergoods.service.BrandService;
import com.pinyougou.mapper.BrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.service.BaseService;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.Map;


@Service(interfaceClass = BrandService.class)
public class BrandServiceImpl extends BaseServiceImpl<TbBrand> implements BrandService {

    @Autowired
    private BrandMapper brandMapper;

    @Override
    public List<TbBrand> queryAll() {
        return brandMapper.queryAll();
    }

    @Override
    public List<TbBrand> testPage(Integer page, Integer rows) {
        PageHelper.startPage(page, rows);
        return brandMapper.selectAll();
    }

    @Override
    public PageResult search(TbBrand brand, Integer pageNo, Integer rows) {
        PageHelper.startPage(pageNo, rows);
        //创建查询对象；本次要操作的实体类
        Example example = new Example(TbBrand.class);
        //创建查询条件对象 -- 构建where子句
        Example.Criteria criteria = example.createCriteria();
        //根据首字母
        if (!StringUtils.isEmpty(brand.getFirstChar())) {
            //-->brand.getFirstChar() != null && !"".equals(brand.getFirstChar())
            criteria.andEqualTo("firstChar", brand.getFirstChar());
        }
        //根据品牌名称
        if (!StringUtils.isEmpty(brand.getName())) {
            //模糊查询 ---> name like %a%
            criteria.andLike("name", "%" + brand.getName() + "%");
        }
           //根据条件查询
        List<TbBrand> list = brandMapper.selectByExample(example);
        //转换为分页信息
        PageInfo<TbBrand> pageInfo = new PageInfo<>(list);
        //返回页面对象
        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public List<Map<String, String>> selectOptionList() {

        return brandMapper.selectOptionList();

    }
}
