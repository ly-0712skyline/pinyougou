package com.pinyougou.task;

import com.pinyougou.mapper.SeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class SeckillTask {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    //秒杀商品存放在redis中的键名key
    public static final String SECKILL_GOODS = "SECKILL_GOODS";

    /**
     * 更新秒杀商品数据
     * 将新增的已经审核，库存大于0，开始时间小于等于当前时间，结束时间大于当前时间并且不在redis的秒杀商品需要更新到redis中。
     */
    @Scheduled(cron = "0/3 * * * * ?")
    public void refreshSeckillGoods(){
        //1、查询redis中的秒杀商品id集合
        Set idsSet = redisTemplate.boundHashOps(SECKILL_GOODS).keys();
        List idsList = new ArrayList(idsSet);

        //2、根据条件查询
        /**
         * select * from tb_seckill_goods where status='1' and stock_count>0
         * and start_time<=? and end_time>? and id not in(redis中的商品集合?,?,....)
         */
        Example example = new Example(TbSeckillGoods.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("status", "1")
                .andGreaterThan("stockCount", 0)
                .andLessThanOrEqualTo("startTime", new Date())
                .andGreaterThan("endTime", new Date());
        if (idsList != null && idsList.size() > 0) {
            criteria.andNotIn("id", idsList);
        }
        List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);
        //3、将新的秒杀商品列表设置到redis
        if (seckillGoodsList != null && seckillGoodsList.size() > 0) {
            for (TbSeckillGoods tbSeckillGoods : seckillGoodsList) {
                redisTemplate.boundHashOps(SECKILL_GOODS).put(tbSeckillGoods.getId(), tbSeckillGoods);
            }
            System.out.println("更新了 " + seckillGoodsList.size() + " 条秒杀商品到缓存中...");
        }
    }

    /**
     * 将在redis中结束时间小于等于当前时间的秒杀商品需要从redis中移除并更新到mysql数据库中。
     */
    @Scheduled(cron = "0/2 * * * * ?")
    public void removeSeckillGoods(){
        //1. 获取在redis中的所有秒杀商品；
        List<TbSeckillGoods> seckillGoodsList = redisTemplate.boundHashOps(SECKILL_GOODS).values();

        //2. 遍历判断每个商品的结束时间是否小于等于当前时间，如果是则将该商品移除；将商品同步回mysql数据库
        if (seckillGoodsList != null && seckillGoodsList.size() > 0) {
            for (TbSeckillGoods tbSeckillGoods : seckillGoodsList) {
                if (tbSeckillGoods.getEndTime().getTime() <= System.currentTimeMillis()) {
                    //已过秒杀时间
                    seckillGoodsMapper.updateByPrimaryKeySelective(tbSeckillGoods);

                    //删除
                    redisTemplate.boundHashOps(SECKILL_GOODS).delete(tbSeckillGoods.getId());
                    System.out.println("秒杀商品id为：" + tbSeckillGoods.getId() + " 已经被移除redis...");
                }
            }
        }
    }
}
