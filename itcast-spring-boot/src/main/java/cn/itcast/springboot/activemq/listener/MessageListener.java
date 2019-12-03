package cn.itcast.springboot.activemq.listener;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 可以监听各个队列的消息
 */
@Component
public class MessageListener {

    /**
     * 接收activeMQ的队列名为spring.boot.queue的消息
     * @param map 接收的消息
     */
    @JmsListener(destination = "spring.boot.queue")
    public void receiveMsg(Map<String, Object> map){
        System.out.println("接收到的消息为：" + map);
    }
}
