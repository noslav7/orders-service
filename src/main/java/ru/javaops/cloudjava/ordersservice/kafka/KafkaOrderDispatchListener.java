package ru.javaops.cloudjava.ordersservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.javaops.cloudjava.ordersservice.storage.repositories.MenuOrderRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaOrderDispatchListener {

    private final MenuOrderRepository menuOrderRepository;

    // TODO
    public void consumeOrderDispatchEvent() {
    }
}