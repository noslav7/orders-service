package ru.javaops.cloudjava.ordersservice;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;
import ru.javaops.cloudjava.ordersservice.config.props.OrderServiceProps;

@TestConfiguration
public class TestWebClientConfig {

    private final OrderServiceProps props;

    public TestWebClientConfig(OrderServiceProps props) {
        this.props = props;
    }

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.baseUrl(props.getMenuServiceUrl()).build();
    }
}
