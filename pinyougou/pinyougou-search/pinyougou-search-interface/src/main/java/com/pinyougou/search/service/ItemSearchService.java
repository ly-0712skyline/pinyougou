package com.pinyougou.search.service;

import com.pinyougou.pojo.TbItem;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {
    /**
     * 根据搜索条件搜索es中商品sku
     * @param searchMap 搜索条件
     * @return 查询结果
     */
    Map<String, Object> search(Map<String, Object> searchMap);

    /**
     * 批量导入商品sku到es
     * @param itemList 商品sku列表
     */
    void importItemList(List<TbItem> itemList);

    /**
     * 根据商品spu id数组批量删除es中对应的sku
     * @param goodsIds 商品spu id数组
     */
    void deleteByGoodsIds(Long[] goodsIds);
}
