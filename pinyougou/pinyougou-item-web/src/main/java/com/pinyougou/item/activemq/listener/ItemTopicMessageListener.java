package com.pinyougou.item.activemq.listener;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.sellergoods.service.ItemCatService;
import com.pinyougou.vo.Goods;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.listener.adapter.AbstractAdaptableMessageListener;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * 接收到消息之后更新指定路径下的商品详情静态html页面
 */
public class ItemTopicMessageListener extends AbstractAdaptableMessageListener {


    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Reference
    private GoodsService goodsService;

    @Reference
    private ItemCatService itemCatService;

    @Value("${ITEM_HTML_PATH}")
    private String itemHtmlPath;

    @Override
    public void onMessage(Message message, Session session) throws JMSException {
        //1、接收消息
        ObjectMessage objectMessage = (ObjectMessage) message;
        Long[] goodsIds = (Long[]) objectMessage.getObject();
        //2、根据商品spu id数组遍历每个spu id生成对应的静态页面到指定路径
        if (goodsIds != null && goodsIds.length > 0) {
            for (Long goodsId : goodsIds) {
                genHtml(goodsId);
            }
        }
    }


    /**
     * 根据商品spu id生成静态页面到指定路径
     * @param goodsId 商品spu id
     */
    private void genHtml(Long goodsId) {
        try {
            //模版
            Configuration configuration = freeMarkerConfigurer.getConfiguration();
            Template template = configuration.getTemplate("item.ftl");

            //数据
            Map<String, Object> dataModel = new HashMap<>();
            //查询 商品vo
            Goods goods = goodsService.findGoodsByIdAndItemStatus(goodsId, "1");

            //goods 商品基本信息
            dataModel.put("goods", goods.getGoods());
            //goodsDesc 商品描述信息
            dataModel.put("goodsDesc", goods.getGoodsDesc());
            //itemList 商品sku列表
            dataModel.put("itemList", goods.getItemList());
            //itemCat1 第1级商品分类中文名称
            TbItemCat itemCat1 = itemCatService.findOne(goods.getGoods().getCategory1Id());
            dataModel.put("itemCat1", itemCat1.getName());
            //itemCat2 第2级商品分类中文名称
            TbItemCat itemCat2 = itemCatService.findOne(goods.getGoods().getCategory2Id());
            dataModel.put("itemCat2", itemCat2.getName());
            //itemCat3 第3级商品分类中文名称
            TbItemCat itemCat3 = itemCatService.findOne(goods.getGoods().getCategory3Id());
            dataModel.put("itemCat3", itemCat3.getName());

            //输出媒介
            String fileName = itemHtmlPath+goodsId+".html";
            FileWriter fileWriter = new FileWriter(fileName);

            //输出
            template.process(dataModel, fileWriter);
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
