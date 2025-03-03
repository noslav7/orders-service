package ru.javaops.cloudjava.ordersservice.storage.repositories;

import io.r2dbc.spi.ConnectionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import ru.javaops.cloudjava.ordersservice.config.R2dbcConfig;
import ru.javaops.cloudjava.ordersservice.storage.model.MenuOrder;

import static ru.javaops.cloudjava.ordersservice.testdata.TestConstants.*;

@Import({R2dbcConfig.class})
@ImportAutoConfiguration({JacksonAutoConfiguration.class})
@DataR2dbcTest
@Testcontainers
public class MenuOrderRepositoryTest {

    @Autowired
    private MenuOrderRepository repository;
    @Autowired
    private ConnectionFactory connectionFactory;
    @Container
    static PostgreSQLContainer<?> container = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16.1"));

    @DynamicPropertySource
    static void applyProperties(DynamicPropertyRegistry registry) {
        var url = "r2dbc:postgresql://" +
                container.getHost() + ":" +
                container.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT) + "/" +
                container.getDatabaseName();

        registry.add("spring.r2dbc.url", () -> url);
        registry.add("spring.r2dbc.username", container::getUsername);
        registry.add("spring.r2dbc.password", container::getPassword);
        registry.add("spring.flyway.url", container::getJdbcUrl);
        registry.add("spring.flyway.user", container::getUsername);
        registry.add("spring.flyway.password", container::getPassword);
    }

    @BeforeEach
    void populateDb(@Value("classpath:insert-orders.sql") Resource script) {
        executeScriptBlocking(script);
    }

    @AfterEach
    void clearDb(@Value("classpath:delete-orders.sql") Resource script) {
        executeScriptBlocking(script);
    }

    @Test
    void findAllByCreatedBy_returnsCorrectSortedByDateDesc() {
        var pageRequest = PageRequest.of(0, 2)
                .withSort(Sort.by(Sort.Direction.DESC, "createdAt"));
        Flux<MenuOrder> orders = repository.findAllByCreatedBy(USERNAME_ONE, pageRequest);
        StepVerifier.create(orders)
                .expectNextMatches(order ->
                        order.getCreatedBy().equals(USERNAME_ONE) &&
                                order.getCreatedAt().equals(ORDER_THREE_DATE))
                .expectNextMatches(order ->
                        order.getCreatedBy().equals(USERNAME_ONE) &&
                                order.getCreatedAt().equals(ORDER_TWO_DATE) &&
                                order.getUpdatedAt() != null)
                .verifyComplete();
    }

    @Test
    void findAllByCreatedBy_returnsCorrectSortedByDateAsc() {
        var pageRequest = PageRequest.of(0, 2)
                .withSort(Sort.by(Sort.Direction.ASC,"createdAt"));
        Flux<MenuOrder> orders = repository.findAllByCreatedBy(USERNAME_ONE, pageRequest);
        StepVerifier.create(orders)
                .expectNextMatches(order ->
                        order.getCreatedBy().equals(USERNAME_ONE) &&
                                order.getCreatedAt().equals(ORDER_ONE_DATE))
                .expectNextMatches(order ->
                        order.getCreatedBy().equals(USERNAME_ONE) &&
                                order.getCreatedAt().equals(ORDER_TWO_DATE))
                .verifyComplete();
    }

    @Test
    void findAllByCreatedBy_returnsEmptyListWhenUserHasNoOrders() {
        var pageRequest = PageRequest.of(0, 10)
                .withSort(Sort.by(Sort.Direction.ASC,"createdAt"));
        Flux<MenuOrder> orders = repository.findAllByCreatedBy("unknown user", pageRequest);
        StepVerifier.create(orders)
                .expectNextCount(0)
                .verifyComplete();
    }

    // https://stackoverflow.com/a/73233121
    private void executeScriptBlocking(final Resource sqlScript) {
        var populator = new ResourceDatabasePopulator();
        populator.addScript(sqlScript);
        populator.populate(connectionFactory).block();
    }
}
