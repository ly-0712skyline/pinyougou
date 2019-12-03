package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.SpecificationOptionMapper;
import com.pinyougou.mapper.TypeTemplateMapper;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbTypeTemplate;
import com.pinyougou.sellergoods.service.TypeTemplateService;
import com.pinyougou.service.impl.BaseServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.Map;

@Service
public class TypeTemplateServiceImpl extends BaseServiceImpl<TbTypeTemplate> implements TypeTemplateService {

    @Autowired
    private TypeTemplateMapper typeTemplateMapper;

    @Autowired
    private SpecificationOptionMapper specificationOptionMapper;

    @Override
    public PageInfo<TbTypeTemplate> search(Integer pageNum, Integer pageSize, TbTypeTemplate typeTemplate) {
        //设置分页
        PageHelper.startPage(pageNum, pageSize);
        //创建查询对象
        Example example = new Example(TbTypeTemplate.class);

        //创建查询条件对象
        Example.Criteria criteria = example.createCriteria();

        //模糊查询
        if (StringUtils.isNotBlank(typeTemplate.getName())) {
            criteria.andLike("name", "%" + typeTemplate.getName() + "%");
        }

        List<TbTypeTemplate> list = typeTemplateMapper.selectByExample(example);
        return new PageInfo<>(list);
    }

    @Override
    public List<Map> findSpecList(Long id) {
        //根据分类模板id查询规格列表
        TbTypeTemplate typeTemplate = findOne(id);
        //[{"id":27,"text":"网络"},{"id":32,"text":"机身内存"}]
        List<Map> specList = JSON.parseArray(typeTemplate.getSpecIds(), Map.class);

        //遍历每个规格，根据规格id查询其对应的选项
        if (specList != null && specList.size() > 0) {
            for (Map map : specList) {
                Long specId = Long.parseLong(map.get("id").toString());
                TbSpecificationOption param = new TbSpecificationOption();
                param.setSpecId(specId);
                List<TbSpecificationOption> options = specificationOptionMapper.select(param);

                map.put("options", options);
            }
        }

        //返回列表
        return specList;
    }

}
