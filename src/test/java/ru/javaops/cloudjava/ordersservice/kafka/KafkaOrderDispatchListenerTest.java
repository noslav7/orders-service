package ru.javaops.cloudjava.ordersservice.kafka;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;
import ru.javaops.cloudjava.OrderDispatchStatus;
import ru.javaops.cloudjava.OrderDispatchedEvent;
import ru.javaops.cloudjava.ordersservice.BaseTest;
import ru.javaops.cloudjava.ordersservice.SchemaRegistryContainer;
import ru.javaops.cloudjava.ordersservice.storage.model.MenuOrder;
import ru.javaops.cloudjava.ordersservice.storage.model.OrderStatus;
import ru.javaops.cloudjava.ordersservice.storage.repositories.MenuOrderRepository;

import java.time.Duration;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
class KafkaOrderDispatchListenerTest extends BaseTest {

    public static final String CONFLUENT_VERSION = "7.5.2";

    private static final String ORDER_DISPATCH_TOPIC = "v1.orders_dispatch";

    private static final Network NETWORK = Network.newNetwork();

    public static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.2"))
            .withKraft()
            .withNetwork(NETWORK);

    public static final SchemaRegistryContainer SCHEMA_REGISTRY =
            new SchemaRegistryContainer(CONFLUENT_VERSION);

    @BeforeAll
    static void setup() {
        KAFKA.start();
        SCHEMA_REGISTRY.withKafka(KAFKA).start();

        System.setProperty("spring.kafka.bootstrap-servers", KAFKA.getBootstrapServers());
        System.setProperty("spring.kafka.consumer.properties.schema.registry.url", "http://localhost:" + SCHEMA_REGISTRY.getFirstMappedPort());
        System.setProperty("spring.kafka.producer.key-serializer", "org.apache.kafka.common.serialization.StringSerializer");
        System.setProperty("spring.kafka.producer.value-serializer", "io.confluent.kafka.serializers.KafkaAvroSerializer");
        System.setProperty("spring.kafka.producer.properties.schema.registry.url", "http://localhost:" + SCHEMA_REGISTRY.getFirstMappedPort());
    }

    @Autowired
    private KafkaTemplate<String, OrderDispatchedEvent> kafkaTemplate;

    @Autowired
    private MenuOrderRepository menuOrderRepository;

    @Test
    void consumeOrderDispatchEvent_consumesEventAndUpdatesMenuOrderStatus() throws Exception {
        MenuOrder order = menuOrderRepository.findAll().blockFirst();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.NEW);

        OrderDispatchedEvent event = OrderDispatchedEvent.newBuilder()
                .setOrderId(order.getId())
                .setStatus(OrderDispatchStatus.ACCEPTED)
                .build();
        kafkaTemplate.send(ORDER_DISPATCH_TOPIC, String.valueOf(order.getId()), event).get();

        Awaitility
                .await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    MenuOrder updated = menuOrderRepository.findById(order.getId()).block();
                    assertThat(updated.getStatus()).isEqualTo(OrderStatus.ACCEPTED);
                });
    }
}