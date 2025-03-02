package ru.javaops.cloudjava.ordersservice.storage.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.javaops.cloudjava.ordersservice.storage.model.MenuOrder;

public interface MenuOrderRepository extends ReactiveCrudRepository<MenuOrder, Long> {

    Flux<MenuOrder> findAllByCreatedBy(String username, Pageable pageable);
}