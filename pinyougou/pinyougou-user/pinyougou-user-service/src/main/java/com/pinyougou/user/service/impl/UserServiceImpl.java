package com.pinyougou.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.UserMapper;
import com.pinyougou.pojo.TbUser;
import com.pinyougou.user.service.UserService;
import com.pinyougou.service.impl.BaseServiceImpl;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import tk.mybatis.mapper.entity.Example;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends BaseServiceImpl<TbUser> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private ActiveMQQueue itcastSmsQueue;

    @Value("${signName}")
    private String signName;

    @Value("${templateCode}")
    private String templateCode;

    @Override
    public PageInfo<TbUser> search(Integer pageNum, Integer pageSize, TbUser user) {
        //设置分页
        PageHelper.startPage(pageNum, pageSize);
        //创建查询对象
        Example example = new Example(TbUser.class);

        //创建查询条件对象
        Example.Criteria criteria = example.createCriteria();

        //模糊查询
        /**if (StringUtils.isNotBlank(user.getProperty())) {
            criteria.andLike("property", "%" + user.getProperty() + "%");
        }*/

        List<TbUser> list = userMapper.selectByExample(example);
        return new PageInfo<>(list);
    }

    @Override
    public void sendSmsCode(String phone) {
        //- 1、生成6位随机数字作为要发送的验证码; Math.random()生成0-1之间的小数 TODO
        String code = (long)(Math.random()*1000000)+"";
        System.out.println("验证码为：" + code);

        //- 2、将验证码存入到redis中，手机号为key；并设置过期时间5分钟
        redisTemplate.boundValueOps(phone).set(code);
        redisTemplate.boundValueOps(phone).expire(5, TimeUnit.MINUTES);

        //- 3、利用JmsTemplate发送（mobile手机号，signName签名，templateCode模版编号，templateParam模版参数）到ActiveMQ的 itcast_sms_queue
        jmsTemplate.send(itcastSmsQueue, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                MapMessage mapMessage = session.createMapMessage();
                mapMessage.setString("mobile", phone);
                mapMessage.setString("signName", signName);
                mapMessage.setString("templateCode", templateCode);
                //{"code":123123}
                mapMessage.setString("templateParam", "{\"code\":"+code+"}");
                return mapMessage;
            }
        });
    }

    @Override
    public boolean checkSmsCode(String phone, String smsCode) {
        //1、获取redis中验证码
        String code = (String) redisTemplate.boundValueOps(phone).get();
        //2、对比验证码
        if(smsCode.equals(code)) {
            //如果是一致的验证码则删除
            redisTemplate.delete(phone);
            return true;
        }
        return false;
    }

}
