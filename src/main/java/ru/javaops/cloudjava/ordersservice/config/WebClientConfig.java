package ru.javaops.cloudjava.ordersservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;
import ru.javaops.cloudjava.ordersservice.config.props.OrderServiceProps;

@RequiredArgsConstructor
@Configuration
@Profile("!test")
public class WebClientConfig {

    private final OrderServiceProps props;
    private final ReactorLoadBalancerExchangeFilterFunction lbFunction;

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder
                .filter(lbFunction)
                .baseUrl(props.getMenuServiceUrl())
                .build();
    }
}