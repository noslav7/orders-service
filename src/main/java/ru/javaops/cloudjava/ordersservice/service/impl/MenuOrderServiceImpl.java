package ru.javaops.cloudjava.ordersservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.javaops.cloudjava.ordersservice.client.MenuClient;
import ru.javaops.cloudjava.ordersservice.dto.CreateOrderRequest;
import ru.javaops.cloudjava.ordersservice.dto.GetMenuInfoRequest;
import ru.javaops.cloudjava.ordersservice.dto.OrderResponse;
import ru.javaops.cloudjava.ordersservice.dto.SortBy;
import ru.javaops.cloudjava.ordersservice.exception.OrderServiceException;
import ru.javaops.cloudjava.ordersservice.mapper.OrderMapper;
import ru.javaops.cloudjava.ordersservice.mapper.OrderOutboxMapper;
import ru.javaops.cloudjava.ordersservice.service.MenuOrderService;
import ru.javaops.cloudjava.ordersservice.storage.repositories.MenuOrderRepository;
import ru.javaops.cloudjava.ordersservice.storage.repositories.OrderPlacedEventRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class MenuOrderServiceImpl implements MenuOrderService {

    private final MenuOrderRepository repository;
    private final MenuClient menuClient;
    private final OrderMapper orderMapper;
    private final OrderOutboxMapper orderOutboxMapper;
    private final OrderPlacedEventRepository orderPlacedEventRepository;

    @Transactional
    @Override
    public Mono<OrderResponse> createOrder(CreateOrderRequest request, String username) {
        var getInfoRequest = new GetMenuInfoRequest(request.getNameToQuantity().keySet());
        return menuClient
                .getMenuInfo(getInfoRequest)
                .map(response -> orderMapper.mapToOrder(request, username, response))
                .flatMap(repository::save)
                .zipWhen(menuOrder -> {
                    var outbox = orderOutboxMapper.toOrderOutbox(menuOrder);
                    return orderPlacedEventRepository.save(outbox);
                })
                .map(tuple -> orderMapper.mapToResponse(tuple.getT1()))
                .doOnError(e -> log.error("Error saving MenuOrder: {}", e.getMessage()))
                .onErrorMap(this::handleThrowable);
    }

    @Override
    public Flux<OrderResponse> getOrdersOfUser(String username, SortBy sortBy, int from, int size) {
        var pageRequest = PageRequest.of(from, size)
                .withSort(sortBy.getSort());
        return repository.findAllByCreatedBy(username, pageRequest)
                .map(orderMapper::mapToResponse);
    }

    private Throwable handleThrowable(Throwable t) {
        return (t instanceof OrderServiceException) ? t :
                new OrderServiceException(t.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
