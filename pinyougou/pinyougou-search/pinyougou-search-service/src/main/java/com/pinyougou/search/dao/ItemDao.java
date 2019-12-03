package com.pinyougou.search.dao;

import com.pinyougou.pojo.TbItem;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemDao extends ElasticsearchRepository<TbItem, Long> {

    /**
     * 根据商品spu id数组批量删除es中对应的sku
     * @param goodsIds 商品spu id数组
     */
    void deleteByGoodsIdIn(Long[] goodsIds);
}
