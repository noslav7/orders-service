package ru.javaops.cloudjava.ordersservice.testdata;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;

public class TestConstants {

    public static final String BASE_URL = "/v1/menu-orders";
    public static final String MENU_INFO_PATH = "/v1/menu-items/menu-info";

    public static final String CITY_ONE = "CityOne";
    public static final String STREET_ONE = "StreetOne";
    public static final int HOUSE_ONE = 1;
    public static final int APARTMENT_ONE = 1;
    public static final String MENU_ONE = "One";
    public static final String MENU_TWO = "Two";
    public static final String MENU_THREE = "Three";
    public static final int MENU_ONE_QUANTITY = 10;
    public static final int MENU_TWO_QUANTITY = 5;
    public static final int MENU_THREE_QUANTITY = 5;
    public static final BigDecimal MENU_ONE_PRICE = BigDecimal.valueOf(4);
    public static final BigDecimal MENU_TWO_PRICE = BigDecimal.valueOf(3);
    public static final BigDecimal MENU_THREE_PRICE = BigDecimal.valueOf(3);
    public static final int MENU_CREATE_ONE_QUANTITY = 10;
    public static final int MENU_CREATE_TWO_QUANTITY = 20;
    public static final int MENU_CREATE_THREE_QUANTITY = 30;
    public static final String USERNAME_ONE = "username1";
    public static final BigDecimal SUCCESS_TOTAL_PRICE = BigDecimal.valueOf(1414.0);
    public static final BigDecimal MENU_CREATE_ONE_PRICE = BigDecimal.valueOf(10.1);
    public static final BigDecimal MENU_CREATE_TWO_PRICE = BigDecimal.valueOf(20.2);
    public static final BigDecimal MENU_CREATE_THREE_PRICE = BigDecimal.valueOf(30.3);
    public static final LocalDateTime ORDER_ONE_DATE = LocalDateTime.of(2024, Month.FEBRUARY, 18, 10, 23, 54);
    public static final LocalDateTime ORDER_TWO_DATE = LocalDateTime.of(2024, Month.FEBRUARY, 20, 10, 23, 54);
    public static final LocalDateTime ORDER_THREE_DATE = LocalDateTime.of(2024, Month.FEBRUARY, 22, 10, 23, 54);
}