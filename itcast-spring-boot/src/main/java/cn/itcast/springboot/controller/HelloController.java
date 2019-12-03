package cn.itcast.springboot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/test")
@RestController
public class HelloController {

    @Value("${url}")
    private String url;

    @Autowired
    private Environment environment;

    @GetMapping("/info2")
    public String info(){
        System.out.println("url = " + environment.getProperty("url"));

        return "Hello Spring Boot！ url = " + url;
    }
}
