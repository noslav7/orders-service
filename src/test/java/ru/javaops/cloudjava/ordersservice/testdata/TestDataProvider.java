package ru.javaops.cloudjava.ordersservice.testdata;

import okhttp3.mockwebserver.MockResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.ResourceUtils;
import ru.javaops.cloudjava.ordersservice.dto.Address;
import ru.javaops.cloudjava.ordersservice.dto.CreateOrderRequest;
import ru.javaops.cloudjava.ordersservice.storage.model.MenuLineItem;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static ru.javaops.cloudjava.ordersservice.testdata.TestConstants.*;

public class TestDataProvider {

    public static CreateOrderRequest createOrderRequest() {
        return CreateOrderRequest.builder()
                .address(Address.builder()
                        .city(CITY_ONE)
                        .street(STREET_ONE)
                        .house(HOUSE_ONE)
                        .apartment(APARTMENT_ONE)
                        .build())
                .nameToQuantity(Map.of(
                        MENU_ONE, MENU_CREATE_ONE_QUANTITY,
                        MENU_TWO, MENU_CREATE_TWO_QUANTITY,
                        MENU_THREE, MENU_CREATE_THREE_QUANTITY
                )).build();
    }

    public static CreateOrderRequest createOrderInvalidRequest() {
        return CreateOrderRequest.builder()
                .address(Address.builder()
                        .city("")
                        .street("")
                        .house(-1)
                        .apartment(-1)
                        .build())
                .nameToQuantity(Map.of(
                        MENU_ONE, MENU_CREATE_ONE_QUANTITY,
                        MENU_TWO, MENU_CREATE_TWO_QUANTITY,
                        MENU_THREE, MENU_CREATE_THREE_QUANTITY
                )).build();
    }

    public static MenuLineItem firstExisting() {
        return MenuLineItem.builder()
                .menuItemName(MENU_ONE)
                .price(MENU_ONE_PRICE)
                .quantity(MENU_ONE_QUANTITY)
                .build();
    }

    public static MenuLineItem secondExisting() {
        return MenuLineItem.builder()
                .menuItemName(MENU_TWO)
                .price(MENU_TWO_PRICE)
                .quantity(MENU_TWO_QUANTITY)
                .build();
    }

    public static MenuLineItem thirdExisting() {
        return MenuLineItem.builder()
                .menuItemName(MENU_THREE)
                .price(MENU_THREE_PRICE)
                .quantity(MENU_THREE_QUANTITY)
                .build();
    }

    public static List<MenuLineItem> existingItems() {
        return List.of(
                firstExisting(),
                secondExisting(),
                thirdExisting()
        );
    }

    public static MenuLineItem firstCreatedItem() {
        return MenuLineItem.builder()
                .menuItemName(MENU_ONE)
                .price(MENU_CREATE_ONE_PRICE)
                .quantity(MENU_CREATE_ONE_QUANTITY)
                .build();
    }

    public static MenuLineItem secondCreatedItem() {
        return MenuLineItem.builder()
                .menuItemName(MENU_TWO)
                .price(MENU_CREATE_TWO_PRICE)
                .quantity(MENU_CREATE_TWO_QUANTITY)
                .build();
    }

    public static MenuLineItem thirdCreatedItem() {
        return MenuLineItem.builder()
                .menuItemName(MENU_THREE)
                .price(MENU_CREATE_THREE_PRICE)
                .quantity(MENU_CREATE_THREE_QUANTITY)
                .build();
    }

    public static List<MenuLineItem> createdItems() {
        return List.of(
                firstCreatedItem(),
                secondCreatedItem(),
                thirdCreatedItem()
        );
    }

    /**
     * Возвращает результат запроса о доступности и ценах блюд.
     * Одно из возвращаемых блюд недоступно для заказа.
     */
    public static MockResponse partialSuccessResponse() {
        return new MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(readPartiallySuccessfulResponse());
    }

    public static MockResponse successResponse() {
        return new MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.OK.value())
                .setBody(readSuccessfulResponse());
    }

    public static String readSuccessfulResponse() {
        return readFileToString("wiremock/success-response.json");
    }

    public static String readPartiallySuccessfulResponse() {
        return readFileToString("wiremock/partially-success-response.json");
    }

    private static String readFileToString(String filePath) {
        try {
            Path path = ResourceUtils.getFile("classpath:" + filePath).toPath();
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}