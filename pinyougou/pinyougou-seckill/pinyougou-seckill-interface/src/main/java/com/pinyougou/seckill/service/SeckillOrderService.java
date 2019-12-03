package com.pinyougou.seckill.service;

import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.service.BaseService;

import java.util.List;

public interface SeckillOrderService extends BaseService<TbSeckillOrder> {
    /**
     * 根据条件搜索
     * @param pageNum 页号
     * @param pageSize 页面大小
     * @param seckillOrder 搜索条件
     * @return 分页信息
     */
    PageInfo<TbSeckillOrder> search(Integer pageNum, Integer pageSize, TbSeckillOrder seckillOrder);

    /**
     * 生成秒杀订单保存到redis中
     * @param seckillGoodsId 秒杀商品ID
     * @return 订单id
     */
    String submitOrder(Long seckillGoodsId, String userId) throws InterruptedException;

    /**
     * 根据在redis中的秒杀订单
     * @param outTradeNo 秒杀订单id
     * @return 秒杀订单
     */
    TbSeckillOrder findSeckillOrderInRedisByOrderId(String outTradeNo);

    /**
     * 将redis中订单保存到MySQL中
     * @param outTradeNo 订单ID
     * @param transaction_id 微信交易号
     */
    void saveSeckillOrderInRedisToDb(String outTradeNo, String transaction_id);

    /**
     * 将redis中的订单删除并更新库存
     * @param outTradeNo 订单ID
     */
    void deleteSeckillOrderInRedis(String outTradeNo) throws InterruptedException;
}
