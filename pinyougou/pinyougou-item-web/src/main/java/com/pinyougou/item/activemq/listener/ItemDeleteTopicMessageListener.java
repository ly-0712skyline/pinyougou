package com.pinyougou.item.activemq.listener;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.listener.adapter.AbstractAdaptableMessageListener;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import java.io.File;

/**
 * 接收商品spu id数组的发布与订阅消息；接收到消息之后删除指定路径下的商品详情静态html页面。
 */
public class ItemDeleteTopicMessageListener extends AbstractAdaptableMessageListener {

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
                String fileName = itemHtmlPath+goodsId+".html";
                File file = new File(fileName);
                if (file.exists()) {
                    file.delete();
                }
            }
        }
    }
}
