package ru.javaops.cloudjava.ordersservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import ru.javaops.cloudjava.ordersservice.config.props.OrderServiceProps;

@RequiredArgsConstructor
@Configuration
public class WebClientConfig {

    private final OrderServiceProps props;

    @LoadBalanced
    @Bean
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public WebClient webClient(WebClient.Builder loadBalancedWebClientBuilder) {
        return loadBalancedWebClientBuilder
                .baseUrl(props.getMenuServiceUrl())
                .build();
    }
}