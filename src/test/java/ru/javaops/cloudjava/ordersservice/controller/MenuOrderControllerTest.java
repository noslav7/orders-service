package ru.javaops.cloudjava.ordersservice.controller;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import ru.javaops.cloudjava.ordersservice.BaseIntegrationTest;
import ru.javaops.cloudjava.ordersservice.dto.OrderResponse;
import ru.javaops.cloudjava.ordersservice.storage.model.OrderStatus;
import ru.javaops.cloudjava.ordersservice.testdata.AuthToken;

import java.util.Comparator;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.web.reactive.function.BodyInserters.fromFormData;
import static ru.javaops.cloudjava.ordersservice.testdata.TestConstants.*;
import static ru.javaops.cloudjava.ordersservice.testdata.TestDataProvider.*;

@AutoConfigureWebTestClient(timeout = "20000")
@EnableAutoConfiguration(exclude = {KafkaAutoConfiguration.class})
class MenuOrderControllerTest extends BaseIntegrationTest {

    private static final KeycloakContainer KEYCLOAK = new KeycloakContainer("quay.io/keycloak/keycloak:24.0")
            .withRealmImportFile("/cloud-java-realm.json");

    static {
        KEYCLOAK.start();
    }

    private static AuthToken admin;
    private static AuthToken userWithOrders;
    private static AuthToken userNoOrders;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> KEYCLOAK.getAuthServerUrl() + "/realms/cloud-java");
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", () -> KEYCLOAK.getAuthServerUrl() + "/realms/cloud-java/protocol/openid-connect/certs");
    }

    @BeforeAll
    static void setup() {
        WebClient webClient = WebClient.builder()
                .baseUrl(KEYCLOAK.getAuthServerUrl() + "/realms/cloud-java/protocol/openid-connect/token")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
        admin = createToken(webClient, ADMIN_USERNAME, "password");
        userWithOrders = createToken(webClient, USERNAME_ONE, "password");
        userNoOrders = createToken(webClient, USERNAME_NO_ORDERS, "password");
    }

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
                .headers(h -> h.setBearerAuth(userWithOrders.getAccessToken()))
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
                .headers(h -> h.setBearerAuth(userWithOrders.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getOrdersOfUser_returnsUnauthorized_whenUserIsNotAuthenticated() {
        webTestClient.get()
                .uri(BASE_URL + "?from=0&size=10&sortBy=date_asc")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void getOrdersOfUser_returnsForbidden_whenUserHasNoRights() {
        webTestClient.get()
                .uri(BASE_URL + "?from=0&size=10&sortBy=date_asc")
                .headers(h -> h.setBearerAuth(admin.getAccessToken()))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void getOrdersOfUser_returnsEmptyListOfOrders_whenUserHasNoOrders() {
        webTestClient.get()
                .uri(BASE_URL + "?from=0&size=10&sortBy=date_asc")
                .headers(h -> h.setBearerAuth(userNoOrders.getAccessToken()))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(OrderResponse.class)
                .value(orders -> {
                    assertThat(orders).isEmpty();
                });
    }

    @Test
    void getOrdersOfUser_returnsCorrectlySortedListOfOrders() {
        webTestClient.get()
                .uri(BASE_URL + "?from=0&size=10&sortBy=date_asc")
                .headers(h -> h.setBearerAuth(userWithOrders.getAccessToken()))
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
                .headers(h -> h.setBearerAuth(userWithOrders.getAccessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void submitMenuOrder_returnsUnauthorized_whenUserIsNotAuthenticated() {
        var validRequest = createOrderRequest();
        webTestClient.post()
                .uri(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validRequest)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void submitMenuOrder_returnsForbidden_whenUserHasNoRights() {
        var validRequest = createOrderRequest();
        webTestClient.post()
                .uri(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(h -> h.setBearerAuth(admin.getAccessToken()))
                .bodyValue(validRequest)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void submitMenuOrder_returnsServiceUnavailableOnTimeout() {
        prepareStubForSuccessWithTimeout();
        var validRequest = createOrderRequest();
        webTestClient.post()
                .uri(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(h -> h.setBearerAuth(userWithOrders.getAccessToken()))
                .bodyValue(validRequest)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void getOrdersOfUser_returnsBadRequestForInvalidParams() {
        webTestClient.get()
                .uri(BASE_URL + "?from=-1&size=10")
                .headers(h -> h.setBearerAuth(userWithOrders.getAccessToken()))
                .exchange()
                .expectStatus().isBadRequest();
    }

    private static AuthToken createToken(WebClient webClient, String username, String password) {
        return webClient.post()
                .body(fromFormData("grant_type", "password")
                        .with("client_id", "cloud-java-gateway")
                        .with("username", username)
                        .with("password", password)
                        .with("client_secret", "iaDMVOKEGssvW5XRaaqZN4EO3lkvdRu6")
                )
                .retrieve()
                .bodyToMono(AuthToken.class)
                .block();
    }
}