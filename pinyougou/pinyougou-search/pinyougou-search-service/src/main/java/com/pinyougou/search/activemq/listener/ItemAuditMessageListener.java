package com.pinyougou.search.activemq.listener;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.adapter.AbstractAdaptableMessageListener;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.List;

/**
 * 接收 pinyougou_item_es_queue的商品sku列表字符串并转换为列表并调用搜索业务方法更新es数据
 */
public class ItemAuditMessageListener extends AbstractAdaptableMessageListener {

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message, Session session) throws JMSException {
        TextMessage textMessage = (TextMessage) message;
        //- 转换消息为商品sku列表
        List<TbItem> itemList = JSON.parseArray(textMessage.getText(), TbItem.class);
        //- 调用搜索系统方法更新
        itemSearchService.importItemList(itemList);
    }
}
