package com.dj.eureka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Auther: steven
 * @Date: 2020/10/5
 * @Description: Eureka Client
 */
@SpringBootApplication
@EnableDiscoveryClient
//@EnableEurekaClient
@RestController
public class EurekaClientApplication {
    @Value("${server.port}")
    String port;

    public static void main(String[] args) {
        SpringApplication.run(EurekaClientApplication.class, args);
    }

    @RequestMapping("/helloEureka")
    public String helloEureka(@RequestParam String name) {
        return "hello "+name+",i am from port:" +port;
    }

}