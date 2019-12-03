package cn.itcast.redis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Set;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/spring-redis.xml")
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    //字符串
    @Test
    public void stringTest(){
        redisTemplate.boundValueOps("string_key").set("hi i_am_ljb can we chat.传智播客");
        Object obj = redisTemplate.boundValueOps("string_key").get();
        System.out.println(obj);
    }

    //散列
    @Test
    public void hashTest(){
        redisTemplate.boundHashOps("h_key").put("f_1", "a");
        redisTemplate.boundHashOps("h_key").put("f_2", "b");
        redisTemplate.boundHashOps("h_key").put("f_3", "c");
        Object obj = redisTemplate.boundHashOps("h_key").values();
        System.out.println(obj);
    }
    //列表
    @Test
    public void listTest(){
        redisTemplate.boundListOps("l_key").leftPush("b");
        redisTemplate.boundListOps("l_key").leftPush("c");
        redisTemplate.boundListOps("l_key").rightPush("a");
        //-1 表示最后一个元素
        List list = redisTemplate.boundListOps("l_key").range(0, -1);
        System.out.println(list);
    }
    //集合
    @Test
    public void setTest(){
        redisTemplate.boundSetOps("s_key").add("a", "b", "c");
        Set set = redisTemplate.boundSetOps("s_key").members();
        System.out.println(set);
    }
    //有序集合；根据元素是分值升序排序
    @Test
    public void sortedSetTest(){
        redisTemplate.boundZSetOps("z_key").add("b", 10);
        redisTemplate.boundZSetOps("z_key").add("a", 20);
        redisTemplate.boundZSetOps("z_key").add("c", 5);
        Set set = redisTemplate.boundZSetOps("z_key").range(0, -1);
        System.out.println(set);
    }
}
