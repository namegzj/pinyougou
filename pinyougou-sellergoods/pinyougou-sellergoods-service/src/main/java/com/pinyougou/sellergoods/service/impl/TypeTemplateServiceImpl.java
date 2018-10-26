package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.container.page.Page;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.SpecificationOptionMapper;
import com.pinyougou.mapper.TypeTemplateMapper;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbTypeTemplate;
import com.pinyougou.sellergoods.service.TypeTemplateService;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service(interfaceClass = TypeTemplateService.class)
public class TypeTemplateServiceImpl extends BaseServiceImpl<TbTypeTemplate> implements TypeTemplateService {

    @Autowired
    private TypeTemplateMapper typeTemplateMapper;

    @Autowired
    private SpecificationOptionMapper specificationOptionMapper;

    @Override
    public PageResult search(Integer page, Integer rows, TbTypeTemplate typeTemplate) {
        PageHelper.startPage(page, rows);

        Example example = new Example(TbTypeTemplate.class);
        Example.Criteria criteria = example.createCriteria();
        if (!StringUtils.isEmpty(typeTemplate.getName())) {
            criteria.andLike("name", "%" + typeTemplate.getName() + "%");
        }
        List<TbTypeTemplate> list = typeTemplateMapper.selectByExample(example);
        PageInfo<TbTypeTemplate> info = new PageInfo<>(list);
        return new PageResult(info.getTotal(), info.getList());
    }


    public void deleteTypeTemplateByIds(Long[] ids) {
        deleteByIds(ids);
        Example example = new Example(TbTypeTemplate.class);
        example.createCriteria().andIn("id", Arrays.asList(ids));
        typeTemplateMapper.deleteByExample(example);

    }

    @Override
    public List<Map> findSpecList(Long id) {
        //1、根据分类模版id查询分类模版
        TbTypeTemplate typeTemplate = findOne(id);

        //2、获取规格列表字符串并转换为一个集合
        typeTemplate.getSpecIds();
        List<Map> list = JSONArray.parseArray(typeTemplate.getSpecIds(), Map.class);
        //3、遍历每一个规格，根据规格id查询该规格对应的所有规格选项并设置到规格中
        for (Map map : list) {
            //获得规格Id
            Long specId = Long.parseLong(map.get("id").toString());

            TbSpecificationOption option = new TbSpecificationOption();
            option.setSpecId(specId);
            List<TbSpecificationOption> options = specificationOptionMapper.select(option);

            map.put("options", options);

        }
        return list;
    }
}
