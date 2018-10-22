package com.pinyougou.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.container.page.Page;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.sellergoods.service.SpecificationService;
import com.pinyougou.vo.PageResult;
import com.pinyougou.vo.Result;
import com.pinyougou.vo.Specification;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/specification")
public class SpecificationController {

    @Reference
    private SpecificationService specificationService;

    @PostMapping("/add")
    public Result add(@RequestBody Specification specification) {
        try {
            specificationService.add(specification);
            return Result.ok("添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("添加失败");
        }
    }

    @RequestMapping("/findAll")
    public List<TbSpecification> findAll() {
        return specificationService.findAll();

    }

    @GetMapping("findOne")
    public Specification findOne(Long id) {
        return specificationService.findOne(id);

    }

    @PostMapping("/search")
    public PageResult search(@RequestBody  TbSpecification specification,
                             @RequestParam(value = "page", defaultValue = "1")Integer page,
                             @RequestParam(value = "rows", defaultValue = "10")Integer rows) {
        return specificationService.search(page, rows, specification);
    }


    @GetMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            specificationService.deleteByIds(ids);
          return   Result.ok("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
           return Result.fail("删除失败");
        }
    }

    @PostMapping("/update")
    public Result update(@RequestBody Specification specification) {
        try {
            specificationService.update(specification);
            return Result.ok("修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("修改失败");
        }
    }

    @GetMapping("/selectOptionList")
    public List<Map<String, String>> selectOptionList() {
        return specificationService.selectOptionList();
    }
}
