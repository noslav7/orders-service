package ru.javaops.cloudjava.ordersservice;

import io.r2dbc.spi.ConnectionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import static ru.javaops.cloudjava.ordersservice.testdata.TestConstants.DEFAULT_TIMEOUT_STR;

public abstract class BaseTest {

    @Autowired
    private ConnectionFactory connectionFactory;

    static final PostgreSQLContainer<?> container = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16.1"))
            .withReuse(true)
            .withDatabaseName("test_database")
            .withUsername("user")
            .withPassword("password");

    static {
        container.start();
        System.setProperty("spring.r2dbc.url", "r2dbc:postgresql://" + container.getHost() + ":" +
                container.getFirstMappedPort() + "/test_database");
        System.setProperty("spring.r2dbc.username", container.getUsername());
        System.setProperty("spring.r2dbc.password", container.getPassword());
        System.setProperty("spring.flyway.url", container.getJdbcUrl());
        System.setProperty("external.default-timeout", DEFAULT_TIMEOUT_STR);
    }

    @BeforeEach
    void populateDb(@Value("classpath:insert-orders.sql") Resource script) {
        executeScriptBlocking(script);
    }

    @AfterEach
    void clearDb(@Value("classpath:delete-orders.sql") Resource script) {
        executeScriptBlocking(script);
    }

    // https://stackoverflow.com/a/73233121
    private void executeScriptBlocking(final Resource sqlScript) {
        var populator = new ResourceDatabasePopulator();
        populator.addScript(sqlScript);
        populator.populate(connectionFactory).block();
    }
}