package ru.javaops.cloudjava.ordersservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import ru.javaops.cloudjava.OrderDispatchedEvent;
import ru.javaops.cloudjava.ordersservice.storage.model.OrderStatus;
import ru.javaops.cloudjava.ordersservice.storage.repositories.MenuOrderRepository;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaOrderDispatchListener {

    private final MenuOrderRepository menuOrderRepository;
    @Value("${kafkaprops.nack-sleep-duration}")
    private Duration nackSleepDuration;

    @KafkaListener(topics = {"${kafkaprops.order-dispatch-topic}"})
    public void consumeOrderDispatchEvent(OrderDispatchedEvent event,
                                          @Header(KafkaHeaders.RECEIVED_KEY) String key,
                                          @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
                                          @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                          Acknowledgment acknowledgment) {
        log.info("Received OrderDispatchedEvent from Kafka: {}. Key: {}. Partition: {}. Topic: {}", event, key, partition, topic);
        try {
            menuOrderRepository
                    .updateStatusById(event.getOrderId(), OrderStatus.fromString(event.getStatus().name()))
                    .block();
            log.info("Successfully updated OrderStatus to {} for order with ID={}", event.getStatus(), event.getOrderId());
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Failed to update OrderStatus to {} for order with ID={}", event.getStatus(), event.getOrderId());
            // don't acknowledge
            acknowledgment.nack(nackSleepDuration);
        }
    }
}