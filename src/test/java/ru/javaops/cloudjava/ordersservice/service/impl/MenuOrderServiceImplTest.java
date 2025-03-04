package ru.javaops.cloudjava.ordersservice.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.javaops.cloudjava.ordersservice.BaseIntegrationTest;
import ru.javaops.cloudjava.ordersservice.dto.OrderResponse;
import ru.javaops.cloudjava.ordersservice.dto.SortBy;
import ru.javaops.cloudjava.ordersservice.exception.OrderServiceException;
import ru.javaops.cloudjava.ordersservice.storage.model.MenuLineItem;
import ru.javaops.cloudjava.ordersservice.storage.model.OrderStatus;
import ru.javaops.cloudjava.ordersservice.testdata.TestConstants;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static ru.javaops.cloudjava.ordersservice.testdata.TestConstants.*;
import static ru.javaops.cloudjava.ordersservice.testdata.TestDataProvider.createOrderRequest;
import static ru.javaops.cloudjava.ordersservice.testdata.TestDataProvider.existingItems;

class MenuOrderServiceImplTest extends BaseIntegrationTest {

    @Autowired
    private MenuOrderServiceImpl menuOrderService;

    @Test
    void getOrdersOfUser_returnsCorrectFluxWhenUserHasOrders() {
        Flux<OrderResponse> orders = menuOrderService.getOrdersOfUser(USERNAME_ONE, SortBy.DATE_ASC, 0, 10);
        StepVerifier.create(orders)
                .expectNextMatches(order -> assertOrder(order, ORDER_ONE_DATE))
                .expectNextMatches(order -> assertOrder(order, ORDER_TWO_DATE))
                .expectNextMatches(order -> assertOrder(order, ORDER_THREE_DATE))
                .verifyComplete();
    }

    @Test
    void getOrdersOfUser_returnsEmptyFluxWhenNoOrders() {
        Flux<OrderResponse> orders = menuOrderService.getOrdersOfUser("Unknown", SortBy.DATE_DESC, 0, 100);
        StepVerifier.create(orders)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void createOrder_returnsError_whenServiceNotAvailable() {
        prepareStubForServiceUnavailable();

        var createOrderRequest = createOrderRequest();
        Mono<OrderResponse> order = menuOrderService.createOrder(createOrderRequest, USERNAME_ONE);
        StepVerifier.create(order)
                .expectError(OrderServiceException.class)
                .verify();
        wiremock.verify(6, postRequestedFor(urlEqualTo(MENU_INFO_PATH)));
    }

    @Test
    void createOrder_returnsError_whenTimeout() {
        prepareStubForSuccessWithTimeout();

        var createOrderRequest = createOrderRequest();
        Mono<OrderResponse> order = menuOrderService.createOrder(createOrderRequest, USERNAME_ONE);
        StepVerifier.create(order)
                .expectError(OrderServiceException.class)
                .verify();
        wiremock.verify(6, postRequestedFor(urlEqualTo(MENU_INFO_PATH)));
    }

    @Test
    void createOrder_returnsError_whenSomeMenusAreNotAvailable() {
        prepareStubForPartialSuccess();

        var createOrderRequest = createOrderRequest();
        Mono<OrderResponse> order = menuOrderService.createOrder(createOrderRequest, USERNAME_ONE);
        StepVerifier.create(order)
                .expectError(OrderServiceException.class)
                .verify();
        wiremock.verify(1, postRequestedFor(urlEqualTo(MENU_INFO_PATH)));
    }

    @Test
    void createOrder_createsOrderWhenAllMenusAreAvailable() {
        prepareStubForSuccess();

        var createOrderRequest = createOrderRequest();
        var now = LocalDateTime.now().minusNanos(1000);
        Mono<OrderResponse> response = menuOrderService.createOrder(createOrderRequest, USERNAME_ONE);
        StepVerifier.create(response)
                .expectNextMatches(orderResponse -> {
                    assertThat(orderResponse.getAddress()).isEqualTo(createOrderRequest.getAddress());
                    assertThat(orderResponse.getTotalPrice()).isEqualTo(TestConstants.SUCCESS_TOTAL_PRICE);
                    assertThat(orderResponse.getStatus()).isEqualTo(OrderStatus.NEW);
                    assertThat(orderResponse.getCreatedAt()).isAfter(now);
                    var menuItems = new ArrayList<>(orderResponse.getMenuLineItems());
                    menuItems.sort(Comparator.comparing(MenuLineItem::getPrice));
                    assertThat(menuItems)
                            .map(MenuLineItem::getMenuItemName)
                            .containsExactly(MENU_ONE, MENU_TWO, MENU_THREE);
                    assertThat(menuItems)
                            .map(MenuLineItem::getQuantity)
                            .containsExactly(MENU_CREATE_ONE_QUANTITY, MENU_CREATE_TWO_QUANTITY, MENU_CREATE_THREE_QUANTITY);
                    assertThat(menuItems)
                            .map(MenuLineItem::getPrice)
                            .containsExactly(MENU_CREATE_ONE_PRICE, MENU_CREATE_TWO_PRICE, MENU_CREATE_THREE_PRICE);
                    return orderResponse.getOrderId() != null;
                })
                .verifyComplete();

        wiremock.verify(1, postRequestedFor(urlEqualTo(MENU_INFO_PATH)));
    }

    private boolean assertOrder(OrderResponse order, LocalDateTime createdAt) {
        return order.getOrderId() != null &&
                order.getAddress().getCity().equals(CITY_ONE) &&
                order.getAddress().getStreet().equals(STREET_ONE) &&
                order.getStatus().equals(OrderStatus.NEW) &&
                order.getCreatedAt().equals(createdAt) &&
                order.getMenuLineItems().equals(existingItems());
    }
}