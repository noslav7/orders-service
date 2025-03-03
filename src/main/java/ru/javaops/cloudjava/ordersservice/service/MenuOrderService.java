package ru.javaops.cloudjava.ordersservice.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.javaops.cloudjava.ordersservice.dto.CreateOrderRequest;
import ru.javaops.cloudjava.ordersservice.dto.OrderResponse;
import ru.javaops.cloudjava.ordersservice.dto.SortBy;

public interface MenuOrderService {

    Mono<OrderResponse> createOrder(CreateOrderRequest request, String username);

    Flux<OrderResponse> getOrdersOfUser(String username, SortBy sortBy, int from, int size);
}
