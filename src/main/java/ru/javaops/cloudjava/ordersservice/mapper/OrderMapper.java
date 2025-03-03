package ru.javaops.cloudjava.ordersservice.mapper;

import org.springframework.stereotype.Component;
import ru.javaops.cloudjava.ordersservice.dto.CreateOrderRequest;
import ru.javaops.cloudjava.ordersservice.dto.GetMenuInfoResponse;
import ru.javaops.cloudjava.ordersservice.dto.OrderResponse;
import ru.javaops.cloudjava.ordersservice.storage.model.MenuOrder;

@Component
public class OrderMapper {

    public MenuOrder mapToOrder(CreateOrderRequest request,
                                String username,
                                GetMenuInfoResponse infoResponse) {
        // TODO
        return null;
    }

    public OrderResponse mapToResponse(MenuOrder order) {
        // TODO
        return null;
    }
}
