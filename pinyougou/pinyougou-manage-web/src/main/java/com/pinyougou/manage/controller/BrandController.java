package com.pinyougou.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import com.pinyougou.vo.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping("/brand")
@RestController
public class BrandController {

    @Reference(timeout = 3000)
    private BrandService brandService;

    /**
     * 获取格式化的品牌列表；格式为：[{id:'1',text:'联想'},{id:'2',text:'华为'}]
     * @return 品牌列表；格式为：[{id:'1',text:'联想'},{id:'2',text:'华为'}]
     */
    @GetMapping("/selectOptionList")
    public List<Map<String, Object>> selectOptionList(){
        return brandService.selectOptionList();
    }

    /**
     * 根据品牌id查询品牌
     * @param id 品牌id
     * @return 品牌
     */
    @GetMapping("/findOne/{id}")
    public TbBrand findOne(@PathVariable Long id){
        return brandService.findOne(id);
    }

    /**
     * 批量删除
     * @param ids id数组
     * @return 操作结果
     */
    @GetMapping("/delete")
    public Result delete(Long[] ids){
        try {
            brandService.deleteByIds(ids);

            return Result.ok("删除品牌成功！");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("删除品牌失败！");
    }

    /**
     * 新增品牌
     * @param brand 品牌
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result add(@RequestBody TbBrand brand){
        try {
            brandService.add(brand);

            return Result.ok("新增品牌成功！");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("新增品牌失败！");
    }

    /**
     * 修改品牌
     * @param brand 品牌
     * @return 操作结果
     */
    @PostMapping("/update")
    public Result update(@RequestBody TbBrand brand){
        try {
            brandService.update(brand);

            return Result.ok("修改品牌成功！");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("修改品牌失败！");
    }

    /**
     * 根据分页参数分页查询分页信息
     * @param pageNum 页号
     * @param pageSize 页大小
     * @return 分页信息
     */
    @GetMapping("/findPage")
    public PageInfo<TbBrand> findPage(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                      @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        return brandService.findPage(pageNum, pageSize);
    }

    /**
     * 根据分页参数分页查询品牌列表
     * @param pageNum 页号
     * @param pageSize 页大小
     * @return 品牌列表
     */
    @GetMapping("/testPage")
    public List<TbBrand> testPage(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                  @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize) {
        //return brandService.testPage(pageNum, pageSize);
        return brandService.findPage(pageNum, pageSize).getList();
    }

    /**
     * 查询品牌列表
     *
     * @return 品牌列表
     */
    @GetMapping("/findAll")
    public List<TbBrand> findAll() {
        //return brandService.queryAll();
        return brandService.findAll();
    }

    /**
     * 根据条件分页查询
     * @param pageNum 页号
     * @param pageSize 页大小
     * @param brand 查询条件对象
     * @return 分页信息对象
     */
    @PostMapping("/search")
    public PageInfo<TbBrand> search(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                    @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                                    @RequestBody TbBrand brand){
        return brandService.search(pageNum, pageSize, brand);
    }
}
