package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.SpecificationMapper;
import com.pinyougou.mapper.SpecificationOptionMapper;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.sellergoods.service.SpecificationService;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.PageResult;
import com.pinyougou.vo.Specification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
@Service(interfaceClass = SpecificationService.class)
public class SpecificationServiceImpl extends BaseServiceImpl<TbSpecification> implements SpecificationService {

    @Autowired
    private SpecificationMapper specificationMapper;

    @Autowired
    private SpecificationOptionMapper specificationOptionMapper;

    @Override
    public PageResult search(Integer page, Integer rows, TbSpecification specification) {
        PageHelper.startPage(page, rows);
        Example example = new Example(TbSpecification.class);
        Example.Criteria criteria = example.createCriteria();
        if (!StringUtils.isEmpty(specification.getSpecName())) {
            criteria.andLike("specName", "%" + specification.getSpecName() + "%");
        }
        List<TbSpecification> list = specificationMapper.selectByExample(example);
        PageInfo<TbSpecification> pageInfo = new PageInfo<>(list);
        return new PageResult(pageInfo.getTotal(),pageInfo.getList());
    }

    /**
     * 保存规格及其选项列表到数据库中
     *
     * @param specification
     */
    @Override
    public void add(Specification specification) {
        specificationMapper.insertSelective(specification.getSpecification());

        if (specification.getSpecificationOptionList() != null & specification.getSpecificationOptionList().size() > 0) {
            for (TbSpecificationOption specificationOption:specification.getSpecificationOptionList()) {
                specificationOption.setSpecId(specification.getSpecification().getId());
                specificationOptionMapper.insertSelective(specificationOption);
            }
        }
    }

    /**
     * 根据规格id到数据库中查询规格及其选项
     *
     * @param id 规格id
     * @return 规格及其选项
     */
    @Override
    public Specification findOne(Long id) {
        Specification specification = new Specification();

        specification.setSpecification(specificationMapper.selectByPrimaryKey(id));

        TbSpecificationOption parm = new TbSpecificationOption();
        parm.setSpecId(id);

        List<TbSpecificationOption> specificationOptionList = specificationOptionMapper.select(parm);
        specification.setSpecificationOptionList(specificationOptionList);

        return specification;
    }

    /**
     * 规格、选项集合更新到数据库中
     *
     * @param specification 规格及其选项
     */
    @Override
    public void update(Specification specification) {
        //更新规格
        specificationMapper.updateByPrimaryKeySelective(specification.getSpecification());
        //删除规格选项
        TbSpecificationOption option = new TbSpecificationOption();
        option.setSpecId(specification.getSpecification().getId());
        specificationOptionMapper.delete(option);
        //添加规格选项
        if (specification.getSpecificationOptionList() != null & specification.getSpecificationOptionList().size() > 0) {
            for (TbSpecificationOption specificationOption : specification.getSpecificationOptionList()) {
                specificationOption.setSpecId(specification.getSpecification().getId());
                specificationOptionMapper.insertSelective(specificationOption);
            }
        }
    }
    /**
     * 删除规格及其对应的所有选项
     *
     * @param ids 规格id集合
     */
    @Override
    public void deleteSpecificationByIds(Long[] ids) {

        deleteByIds(ids);
        Example example = new Example(TbSpecificationOption.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("specId", Arrays.asList(ids));
        specificationOptionMapper.deleteByExample(example);
    }

    /**
     * 查询规格列表；
     *
     * @return 规格列表
     */
    @Override
    public List<Map<String, String>> selectOptionList() {
       return specificationMapper.selectOptionList();
    }
}
