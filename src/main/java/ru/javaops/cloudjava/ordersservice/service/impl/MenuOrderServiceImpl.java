package ru.javaops.cloudjava.ordersservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.javaops.cloudjava.ordersservice.client.MenuClient;
import ru.javaops.cloudjava.ordersservice.dto.CreateOrderRequest;
import ru.javaops.cloudjava.ordersservice.dto.OrderResponse;
import ru.javaops.cloudjava.ordersservice.dto.SortBy;
import ru.javaops.cloudjava.ordersservice.mapper.OrderMapper;
import ru.javaops.cloudjava.ordersservice.service.MenuOrderService;
import ru.javaops.cloudjava.ordersservice.storage.repositories.MenuOrderRepository;

@Service
@RequiredArgsConstructor
public class MenuOrderServiceImpl implements MenuOrderService {

    private final MenuOrderRepository repository;
    private final MenuClient menuClient;
    private final OrderMapper orderMapper;

    @Override
    public Mono<OrderResponse> createOrder(CreateOrderRequest request, String username) {
        // TODO
        // 1. получаем Mono<GetMenuInfoResponse> из Menu Service
        // 2. маппим полученные данные и входящий запрос на сущность MenuOrder
        // 3. сохраняем ее в базе и возвращаем Mono<MenuOrder>, чтобы избавиться от
        // вложенности (Mono<Mono<MenuOrder>>) используем оператор flatMap
        // 4. маппим MenuOrder на OrderResponse
        return null;
    }

    @Override
    public Flux<OrderResponse> getOrdersOfUser(String username, SortBy sortBy, int from, int size) {
        // TODO
        // 1. формируем PageRequest из параметров from, size
        // 2. применяем сортировку к PageRequest
        // 3. ищем заказы в базе данных
        // 4. маппим MenuOrder на OrderResponse
        return null;
    }
}
