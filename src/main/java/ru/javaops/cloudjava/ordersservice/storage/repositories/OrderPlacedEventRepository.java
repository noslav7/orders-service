package ru.javaops.cloudjava.ordersservice.storage.repositories;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import ru.javaops.cloudjava.ordersservice.storage.model.OrderPlacedEvent;

public interface OrderPlacedEventRepository extends ReactiveCrudRepository<OrderPlacedEvent, Long> {
}