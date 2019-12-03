package com.pinyougou.mapper;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbBrand;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/*.xml")
public class BrandMapperTest {

    @Autowired
    private BrandMapper brandMapper;

    @Test
    public void queryAll() {
        List<TbBrand> list = brandMapper.queryAll();
        for (TbBrand tbBrand : list) {
            System.out.println(tbBrand);
        }
    }

    @Test
    public void selectAll() {
        List<TbBrand> list = brandMapper.selectAll();
        for (TbBrand tbBrand : list) {
            System.out.println(tbBrand);
        }
    }

    @Test
    public void selectByPrimaryKey() {
        TbBrand brand = brandMapper.selectByPrimaryKey(2L);
        System.out.println(brand);
    }

    @Test
    public void select() {
        //条件查询
        TbBrand param = new TbBrand();
        param.setFirstChar("C");
        List<TbBrand> list = brandMapper.select(param);
        for (TbBrand tbBrand : list) {
            System.out.println(tbBrand);
        }
    }

    @Test
    public void insertSelective() {
        //选择性新增；如果传递的对象属性是有值的则会出现在insert语句中，没有赋值的则不会出现
        //insert into tb_brand(id, name) values(?, ?);
        TbBrand brand = new TbBrand();
        brand.setName("test1");
        brand.setFirstChar("T");

        brandMapper.insertSelective(brand);
    }


    @Test
    public void updateByPrimaryKeySelective() {
        //选择性更新；如果传递的对象属性是有值的则会出现在update语句中，没有赋值的则不会出现
        //update tb_brand set name=? where id=?
        TbBrand brand = new TbBrand();
        brand.setId(23L);
        brand.setName("test11");
        brandMapper.updateByPrimaryKeySelective(brand);
    }


    @Test
    public void deleteByPrimaryKey() {
        //根据主键删除
        brandMapper.deleteByPrimaryKey(23L);
    }


    //分页条件查询
    @Test
    public void selectByExample() {

        //设置分页(当前页号，页大小)；只对紧接着执行的查询语句生效
        PageHelper.startPage(2, 2);

        //查询条件构造对象
        Example example = new Example(TbBrand.class);

        //查询条件对象
        Example.Criteria criteria = example.createCriteria();

        //根据首字母查询
        criteria.andEqualTo("firstChar", "C");

        //设置排序，根据id降序排序
        example.orderBy("id").desc();


        List<TbBrand> list = brandMapper.selectByExample(example);

        //转换为分页信息对象
        PageInfo<TbBrand> pageInfo = new PageInfo<>(list);

        System.out.println("符合本次查询的总记录数为：" + pageInfo.getTotal());
        System.out.println("符合本次查询的总页数为：" + pageInfo.getPages());
        System.out.println("符合本次查询的当前页号为：" + pageInfo.getPageNum());
        System.out.println("符合本次查询的页大小为：" + pageInfo.getPageSize());

        for (TbBrand tbBrand : pageInfo.getList()) {
            System.out.println(tbBrand);
        }
    }

    //批量新增
    @Test
    public void insertList() {
        //选择性新增；如果传递的对象属性是有值的则会出现在insert语句中，没有赋值的则不会出现
        //insert into tb_brand(id, name) values(?, ?);
        TbBrand brand1 = new TbBrand();
        brand1.setName("test1");
        brand1.setFirstChar("T");

        TbBrand brand2 = new TbBrand();
        brand2.setName("test2");
        brand2.setFirstChar("T");

        List<TbBrand> list = new ArrayList<>();
        list.add(brand1);
        list.add(brand2);

        brandMapper.insertList(list);
    }

    //批量删除
    @Test
    public void deleteByIds() {
        Long[] ids = {24L, 25L};

        //将数组的每个元素使用 , 连接
        String idsStr = StringUtils.join(ids, ",");

        brandMapper.deleteByIds(idsStr);
    }
}