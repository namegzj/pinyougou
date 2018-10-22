package com.pinyougou.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbTypeTemplate;
import com.pinyougou.sellergoods.service.TypeTemplateService;
import com.pinyougou.vo.PageResult;
import com.pinyougou.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping("typeTemplate")
@RestController
public class TypeTemplateController {

    /**
     * 数据加载
     */
    @Reference
    private TypeTemplateService typeTemplateService;

    @PostMapping("/search")
    public PageResult search(@RequestBody  TbTypeTemplate typeTemplate,
                             @RequestParam(value = "page", defaultValue = "1")Integer page,
                             @RequestParam(value = "rows", defaultValue = "10")Integer rows) {
        return typeTemplateService.search(page, rows, typeTemplate);
    }

    /**
     * 添加
     * @param typeTemplate
     * @return  Result
     */
    @PostMapping("/add")
    public Result add(@RequestBody TbTypeTemplate typeTemplate) {
        try {
            typeTemplateService.add(typeTemplate);
            return Result.ok("添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("添加失败");
        }
    }

    @GetMapping("/delete")
    public Result delete(Long [] ids) {
        try {
            typeTemplateService.deleteByIds(ids);
            return Result.ok("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("删除失败");
        }

    }

    @GetMapping("/findOne")
    public TbTypeTemplate findOne(Long id) {
     return    typeTemplateService.findOne(id);
    }

    @PostMapping("/update")
    public Result update(@RequestBody TbTypeTemplate typeTemplate) {
        try {
            typeTemplateService.update(typeTemplate);
            return Result.ok("修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("修改失败");
        }

    }

}
