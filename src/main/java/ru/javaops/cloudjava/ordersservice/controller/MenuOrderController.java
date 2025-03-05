package ru.javaops.cloudjava.ordersservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.javaops.cloudjava.ordersservice.service.MenuOrderService;

@Tag(name = "MenuOrderController", description = "REST API для работы с заказами.")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/menu-orders")
public class MenuOrderController {

    public static final String USER_HEADER = "X-User-Name";

    private final MenuOrderService menuOrderService;

}
