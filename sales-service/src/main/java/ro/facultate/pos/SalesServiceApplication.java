package ro.facultate.pos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@EnableDiscoveryClient
@EnableFeignClients
public class SalesServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SalesServiceApplication.class, args);
    }
}
