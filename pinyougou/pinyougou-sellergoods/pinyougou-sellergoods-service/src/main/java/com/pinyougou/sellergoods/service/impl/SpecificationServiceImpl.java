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
import com.pinyougou.vo.Specification;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Transactional
@Service
public class SpecificationServiceImpl extends BaseServiceImpl<TbSpecification> implements SpecificationService {

    @Autowired
    private SpecificationMapper specificationMapper;

    @Autowired
    private SpecificationOptionMapper specificationOptionMapper;

    @Override
    public PageInfo<TbSpecification> search(Integer pageNum, Integer pageSize, TbSpecification specification) {
        //设置分页
        PageHelper.startPage(pageNum, pageSize);
        //创建查询对象
        Example example = new Example(TbSpecification.class);

        //创建查询条件对象
        Example.Criteria criteria = example.createCriteria();

        //模糊查询
        if (StringUtils.isNotBlank(specification.getSpecName())) {
            criteria.andLike("specName", "%" + specification.getSpecName() + "%");
        }

        List<TbSpecification> list = specificationMapper.selectByExample(example);
        return new PageInfo<>(list);
    }

    @Override
    public void addSpecification(Specification specification) {
        //保存规格；通用mapper默认已经实现了在新增实体类之后主键回填
        add(specification.getSpecification());

        //保存规格选项列表
        for (TbSpecificationOption tbSpecificationOption : specification.getSpecificationOptionList()) {
            tbSpecificationOption.setSpecId(specification.getSpecification().getId());
        }
        specificationOptionMapper.insertList(specification.getSpecificationOptionList());
    }

    @Override
    public Specification findSpecificationById(Long id) {
        Specification specification = new Specification();

        //-- 根据规格id查询规格
        //select * from tb_specification where id = ?
        specification.setSpecification(findOne(id));

        //--根据规格id查询选项列表
        //select * from tb_specification_option where spec_id=?
        TbSpecificationOption param = new TbSpecificationOption();
        param.setSpecId(id);
        List<TbSpecificationOption> specificationOptionList = specificationOptionMapper.select(param);
        specification.setSpecificationOptionList(specificationOptionList);

        return specification;
    }

    @Override
    public void updateSpecification(Specification specification) {
        //- 更新规格
        update(specification.getSpecification());

        //- 根据规格id删除规格选项列表
        //    -- 根据规格id删除规格选项列表
        //    delete from tb_specification_option where spec_id=?
        TbSpecificationOption param = new TbSpecificationOption();
        param.setSpecId(specification.getSpecification().getId());
        specificationOptionMapper.delete(param);

        //保存规格选项列表
        for (TbSpecificationOption tbSpecificationOption : specification.getSpecificationOptionList()) {
            tbSpecificationOption.setSpecId(specification.getSpecification().getId());
        }
        specificationOptionMapper.insertList(specification.getSpecificationOptionList());
    }

    @Override
    public void deleteSpecificationByIds(Long[] ids) {
        //- 根据规格id数组批量删除规格
        //    -- 根据规格id数组批量删除规格
        //    DELETE FROM  `tb_specification` WHERE `id` IN (?,?,?) ;
        deleteByIds(ids);
        //- 根据规格id数组批量删除规格选项
        //    -- 根据规格id数组批量删除规格选项
        //    DELETE FROM  `tb_specification_option` WHERE `spec_id` IN (?,?,?) ;
        Example example = new Example(TbSpecificationOption.class);
        example.createCriteria().andIn("specId", Arrays.asList(ids));
        specificationOptionMapper.deleteByExample(example);
    }

    @Override
    public List<Map<String, Object>> selectOptionList() {
        return specificationMapper.selectOptionList();
    }

}
