package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.BrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import com.pinyougou.service.impl.BaseServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.Map;

@Service
public class BrandServiceImpl extends BaseServiceImpl<TbBrand> implements BrandService {

    @Autowired
    private BrandMapper brandMapper;

    @Override
    public List<TbBrand> queryAll() {
        return brandMapper.queryAll();
    }

    @Override
    public List<TbBrand> testPage(Integer pageNum, Integer pageSize) {
        //设置分页
        PageHelper.startPage(pageNum, pageSize);

        return brandMapper.selectAll();
    }

    @Override
    public PageInfo<TbBrand> search(Integer pageNum, Integer pageSize, TbBrand brand) {
        //设置分页
        PageHelper.startPage(pageNum, pageSize);

        //--根据品牌名称、首字母进行条件模糊查询
        //select * from tb_brand where name like '%?%' and first_char = ?

        Example example = new Example(TbBrand.class);
        Example.Criteria criteria = example.createCriteria();

        //首字母
        //if(brand.getFirstChar() != null && !brand.getFirstChar().equals("")){
        if(StringUtils.isNotBlank(brand.getFirstChar())){
            criteria.andEqualTo("firstChar", brand.getFirstChar());
        }

        //品牌名称
        if (StringUtils.isNotBlank(brand.getName())) {
            criteria.andLike("name", "%" + brand.getName() + "%");
        }

        List<TbBrand> list = brandMapper.selectByExample(example);

        return new PageInfo<>(list);
    }

    @Override
    public List<Map<String, Object>> selectOptionList() {
        return brandMapper.selectOptionList();
    }
}
