package com.pinyougou.sellergoods.service;

import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.service.BaseService;
import com.pinyougou.vo.Goods;

import java.util.List;

public interface GoodsService extends BaseService<TbGoods> {
    /**
     * 根据条件搜索
     * @param pageNum 页号
     * @param pageSize 页面大小
     * @param goods 搜索条件
     * @return 分页信息
     */
    PageInfo<TbGoods> search(Integer pageNum, Integer pageSize, TbGoods goods);

    /**
     * 新增
     * @param goods 商品信息（基本、描述、sku列表）
     */
    void addGoods(Goods goods);

    /**
     * 根据主键查询商品vo
     * @param id 主键
     * @return 商品vo（基本、描述、sku列表）
     */
    Goods findGoodsById(Long id);

    /**
     * 修改
     * @param goods 商品信息（基本、描述、sku列表）
     */
    void updateGoods(Goods goods);
    /**
     * 批量修改商品审核状态
     * @param ids 商品spu id数组
     * @param status 商品的审核状态
     */
    void updateStatus(Long[] ids, String status);
    /**
     * 根据主键数组批量更新删除标识
     * @param ids 主键数组
     */
    void deleteGoodsByIds(Long[] ids);

    /**
     * 根据spu id数组查询已启用的商品数据
     * @param ids spu id数组
     * @param itemStatus sku的状态
     * @return sku列表
     */
    List<TbItem> findGoodsByIdsAndStatus(Long[] ids, String itemStatus);

    /**
     * 根据商品spu id查询商品vo和特定状态的sku
     * @param goodsId 商品spu id
     * @param itemStatus 商品sku的状态
     * @return 商品vo
     */
    Goods findGoodsByIdAndItemStatus(Long goodsId, String itemStatus);
}
