package cn.itcast.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/**
 * 如果是一个spring boot工程则必定有一个启动引导类
 * 需要在该类上面添加如下组合注解
 * 其中包含ComponentScan可以扫描本包及其子包中的注解
 */
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
