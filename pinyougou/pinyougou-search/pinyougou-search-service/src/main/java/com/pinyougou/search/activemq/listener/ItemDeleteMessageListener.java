package com.pinyougou.search.activemq.listener;

import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.adapter.AbstractAdaptableMessageListener;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import java.io.Serializable;

/**
 * 根据商品spu id 数组删除es中对应的商品sku
 */
public class ItemDeleteMessageListener extends AbstractAdaptableMessageListener {

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message, Session session) throws JMSException {
        ObjectMessage objectMessage = (ObjectMessage) message;
        //获取商品spu id数组
        Long[] goodsIds = (Long[]) objectMessage.getObject();

        //删除
        itemSearchService.deleteByGoodsIds(goodsIds);
    }
}
