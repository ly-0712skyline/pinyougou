package com.pinyougou.es;

import com.alibaba.fastjson.JSON;
import com.pinyougou.es.dao.ItemElasticSearchRepository;
import com.pinyougou.mapper.ItemMapper;
import com.pinyougou.pojo.TbItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:spring/*.xml")
public class ItemImport2ElasticSearchTest {

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private ItemElasticSearchRepository repository;

    @Test
    public void importItem(){
        //1.查询已启用的sku
        TbItem param = new TbItem();
        param.setStatus("1");
        List<TbItem> itemList = itemMapper.select(param);

        //2.遍历每个sku将spec字符串转换为map设置到specMap
        for (TbItem tbItem : itemList) {
            Map<String, String> map = JSON.parseObject(tbItem.getSpec(), Map.class);
            tbItem.setSpecMap(map);
        }

        //3.保存数据到es
        repository.saveAll(itemList);
    }
}
