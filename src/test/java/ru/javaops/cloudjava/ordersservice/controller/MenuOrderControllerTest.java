package ru.javaops.cloudjava.ordersservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.javaops.cloudjava.ordersservice.BaseIntegrationTest;
import ru.javaops.cloudjava.ordersservice.dto.OrderResponse;
import ru.javaops.cloudjava.ordersservice.storage.model.OrderStatus;

import java.util.Comparator;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static ru.javaops.cloudjava.ordersservice.controller.MenuOrderController.USER_HEADER;
import static ru.javaops.cloudjava.ordersservice.testdata.TestConstants.*;
import static ru.javaops.cloudjava.ordersservice.testdata.TestDataProvider.*;

@AutoConfigureWebTestClient(timeout = "20000")
class MenuOrderControllerTest extends BaseIntegrationTest {

    @Autowired
    protected WebTestClient webTestClient;

    @Test
    void submitMenuOrder_returnsCorrectResponse() {
        prepareStubForSuccess();
        var validRequest = createOrderRequest();
        var expectedMenuItems = createdItems();
        webTestClient.post()
                .uri(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_HEADER, USERNAME_ONE)
                .bodyValue(validRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrderResponse.class)
                .value(response -> {
                    assertThat(response.getOrderId()).isNotNull();
                    assertThat(response.getMenuLineItems()).isEqualTo(expectedMenuItems);
                    assertThat(response.getStatus()).isEqualTo(OrderStatus.NEW);
                    assertThat(response.getTotalPrice()).isEqualTo(SUCCESS_TOTAL_PRICE);
                    assertThat(response.getAddress()).isEqualTo(validRequest.getAddress());
                });
    }

    @Test
    void submitMenuOrder_returnsNotFound_whenSomeMenusAreNotAvailableInMenuService() {
        prepareStubForPartialSuccess();
        var request = createOrderRequest();
        webTestClient.post()
                .uri(BASE_URL)
                .header(USER_HEADER, USERNAME_ONE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getOrdersOfUser_returnsCorrectlySortedListOfOrders() {
        webTestClient.get()
                .uri(BASE_URL + "?from=0&size=10&sortBy=date_asc")
                .header(USER_HEADER, USERNAME_ONE)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(OrderResponse.class)
                .value(orders -> {
                    assertThat(orders).hasSize(3)
                            .isSortedAccordingTo(Comparator.comparing(OrderResponse::getCreatedAt));
                });
    }

    @Test
    void submitMenuOrder_returnsBadRequest_whenOrderInvalid() {
        var invalidRequest = createOrderInvalidRequest();
        webTestClient.post()
                .uri(BASE_URL)
                .header(USER_HEADER, USERNAME_ONE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void submitMenuOrder_returnsServiceUnavailableOnTimeout() {
        prepareStubForSuccessWithTimeout();
        var validRequest = createOrderRequest();
        webTestClient.post()
                .uri(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_HEADER, USERNAME_ONE)
                .bodyValue(validRequest)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void getOrdersOfUser_returnsBadRequestForInvalidParams() {
        webTestClient.get()
                .uri(BASE_URL + "?from=-1&size=10")
                .header(USER_HEADER, USERNAME_ONE)
                .exchange()
                .expectStatus().isBadRequest();
    }
}