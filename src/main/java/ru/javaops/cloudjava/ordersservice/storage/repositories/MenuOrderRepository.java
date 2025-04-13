package ru.javaops.cloudjava.ordersservice.storage.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.javaops.cloudjava.ordersservice.storage.model.MenuOrder;
import ru.javaops.cloudjava.ordersservice.storage.model.OrderStatus;

public interface MenuOrderRepository extends ReactiveCrudRepository<MenuOrder, Long> {

    Flux<MenuOrder> findAllByCreatedBy(String username, Pageable pageable);

    @Transactional
    @Modifying
    @Query("UPDATE orders SET status = :newStatus, updated_at = CURRENT_TIMESTAMP WHERE id = :orderId")
    Mono<Void> updateStatusById(Long orderId, OrderStatus newStatus);
}