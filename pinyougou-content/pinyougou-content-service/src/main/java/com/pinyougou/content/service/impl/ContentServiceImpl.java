package com.pinyougou.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.ContentMapper;
import com.pinyougou.pojo.TbContent;
import com.pinyougou.content.service.ContentService;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@Service(interfaceClass = ContentService.class)
public class ContentServiceImpl extends BaseServiceImpl<TbContent> implements ContentService {

    @Autowired
    private ContentMapper contentMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    public static final String CONTENT = "content";


    /**
     * 新增
     *
     * @param tbContent
     */
    @Override
    public void add(TbContent tbContent) {
        super.add(tbContent);
        updateContentInRedisByCategoryId(tbContent.getCategoryId());
    }

    private void updateContentInRedisByCategoryId(Long categoryId) {
        redisTemplate.boundHashOps(CONTENT).delete(categoryId);
    }

    /**
     * 更新
     *
     * @param tbContent
     */
    @Override
    public void update(TbContent tbContent) {
        TbContent oldContent = findOne(tbContent.getCategoryId());
        super.update(tbContent);
        updateContentInRedisByCategoryId(tbContent.getCategoryId());
        if (!oldContent.getCategoryId().equals(tbContent.getCategoryId())) {
            updateContentInRedisByCategoryId(oldContent.getCategoryId());
        }
    }

    /**
     * 删除
     *
     * @param ids
     */
    @Override
    public void deleteByIds(Serializable[] ids) {
        Example example = new Example(TbContent.class);
        example.createCriteria().andIn("id", Arrays.asList(ids));
        List<TbContent> contentList = contentMapper.selectByExample(example);
        if (contentList != null) {
            for (TbContent tbContent : contentList) {
                updateContentInRedisByCategoryId(tbContent.getCategoryId());
            }
        }
        super.deleteByIds(ids);
    }

    @Override
    public PageResult search(Integer page, Integer rows, TbContent content) {
        PageHelper.startPage(page, rows);

        Example example = new Example(TbContent.class);
        Example.Criteria criteria = example.createCriteria();
        /*if(!StringUtils.isEmpty(content.get***())){
            criteria.andLike("***", "%" + content.get***() + "%");
        }*/

        List<TbContent> list = contentMapper.selectByExample(example);
        PageInfo<TbContent> pageInfo = new PageInfo<>(list);

        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public List<TbContent> findContentListByCategoryId(Long categoryId) {
        List<TbContent> contentList = null;

        try {
            contentList = (List<TbContent>)redisTemplate.boundHashOps(CONTENT).get(categoryId);
            if (contentList != null) {
                return contentList;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Example example = new Example(TbContent.class);
        //分类,状态
        example.createCriteria().andEqualTo("categoryId", categoryId)
                .andEqualTo("status", "1");
        //降序
        example.orderBy("sortOrder").desc();
        contentList = contentMapper.selectByExample(example);
        //将分类对应的内容设置到redis中
        try {
            redisTemplate.boundHashOps(CONTENT).put(categoryId,contentList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contentList;
    }
}
