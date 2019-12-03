package com.pinyougou.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.vo.Goods;
import com.pinyougou.vo.Result;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.jms.*;
import java.util.List;

@RequestMapping("/goods")
@RestController
public class GoodsController {

    @Reference
    private GoodsService goodsService;

   @Autowired
   private JmsTemplate jmsTemplate;

   @Autowired
   private ActiveMQQueue itemEsQueue;

   @Autowired
   private ActiveMQQueue itemEsDeleteQueue;

   @Autowired
   private ActiveMQTopic itemTopic;

   @Autowired
   private ActiveMQTopic itemDeleteTopic;

    /**
     * 批量修改商品审核状态
     * @param ids 商品spu id数组
     * @param status 商品的审核状态
     * @return 操作结果
     */
    @GetMapping("/updateStatus")
    public Result updateStatus(Long[] ids, String status){
        try {
            goodsService.updateStatus(ids, status);
            //审核通过，需要更新搜索系统数据
            if ("2".equals(status)) {
                //1、根据spu id数组查询已启用的商品数据
                List<TbItem> itemList = goodsService.findGoodsByIdsAndStatus(ids, "1");
                //2、调用搜索系统的业务对象导入数据到es中
                //itemSearchService.importItemList(itemList);
                jmsTemplate.send(itemEsQueue, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        TextMessage textMessage = session.createTextMessage();
                        textMessage.setText(JSON.toJSONString(itemList));
                        return textMessage;
                    }
                });

                //发送商品审核通过的主题消息到MQ
                sendMQMsg(itemTopic, ids);
            }
            return Result.ok("修改商品状态成功！");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("修改商品状态失败！");
    }

    /**
     * 新增
     * @param goods 商品信息（基本、描述、sku列表）
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result add(@RequestBody Goods goods){
        try {
            //设置卖家
            String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
            goods.getGoods().setSellerId(sellerId);
            //未审核
            goods.getGoods().setAuditStatus("0");
            goodsService.addGoods(goods);

            return Result.ok("新增商品成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("新增商品失败");
    }

    /**
     * 根据主键查询商品vo
     * @param id 主键
     * @return 商品vo（基本、描述、sku列表）
     */
    @GetMapping("/findOne/{id}")
    public Goods findOne(@PathVariable Long id){
        return goodsService.findGoodsById(id);
    }

    /**
     * 修改
     * @param goods 商品信息（基本、描述、sku列表）
     * @return 操作结果
     */
    @PostMapping("/update")
    public Result update(@RequestBody Goods goods){
        try {
            //查询原来的商品商家
            TbGoods oldGoods = goodsService.findOne(goods.getGoods().getId());

            String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();

            if (!oldGoods.getSellerId().equals(sellerId)) {
                return Result.fail("非法操作");
            }
            goodsService.updateGoods(goods);
            return Result.ok("修改成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("修改失败");
    }

    /**
     * 根据主键数组批量删除
     * @param ids 主键数组
     * @return 实体
     */
    @GetMapping("/delete")
    public Result delete(Long[] ids){
        try {
            goodsService.deleteGoodsByIds(ids);
            //同步更新搜索系统
            //itemSearchService.deleteByGoodsIds(ids);
            sendMQMsg(itemEsDeleteQueue, ids);

            //发送商品商品的主题消息
            sendMQMsg(itemDeleteTopic, ids);
            return Result.ok("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("删除失败");
    }

    /**
     * 发送消息到MQ
     * @param destination 主题或者队列
     * @param ids 商品spu id数组
     */
    private void sendMQMsg(Destination destination, Long[] ids) {
        jmsTemplate.send(destination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                ObjectMessage objectMessage = session.createObjectMessage();
                objectMessage.setObject(ids);
                return objectMessage;
            }
        });
    }

    /**
     * 根据条件搜索
     * @param pageNum 页号
     * @param pageSize 页面大小
     * @param goods 搜索条件
     * @return 分页信息
     */
    @PostMapping("/search")
    public PageInfo<TbGoods> search(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                             @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                           @RequestBody TbGoods goods) {
        return goodsService.search(pageNum, pageSize, goods);
    }

}
