package ru.javaops.cloudjava.ordersservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import reactor.core.publisher.Hooks;

@SpringBootApplication
@ConfigurationPropertiesScan
public class OrdersServiceApplication {

    public static void main(String[] args) {
        Hooks.enableAutomaticContextPropagation();
        SpringApplication.run(OrdersServiceApplication.class, args);
    }
}
