package ru.javaops.cloudjava.ordersservice.mapper;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import ru.javaops.cloudjava.ordersservice.dto.*;
import ru.javaops.cloudjava.ordersservice.exception.OrderServiceException;
import ru.javaops.cloudjava.ordersservice.storage.model.MenuLineItem;
import ru.javaops.cloudjava.ordersservice.storage.model.MenuOrder;
import ru.javaops.cloudjava.ordersservice.storage.model.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

@Component
public class OrderMapper {

    public MenuOrder mapToOrder(CreateOrderRequest request,
                                String username,
                                GetMenuInfoResponse infoResponse) {
        var infos = infoResponse.getMenuInfos();
        throwIfHasUnavailableMenuItems(infos);

        List<MenuLineItem> menuLineItems = getMenuLineItems(request, infos);
        var totalPrice = calculateTotalPrice(menuLineItems);

        return MenuOrder.builder()
                .totalPrice(totalPrice)
                .city(request.getAddress().getCity())
                .street(request.getAddress().getStreet())
                .house(request.getAddress().getHouse())
                .apartment(request.getAddress().getApartment())
                .status(OrderStatus.NEW)
                .createdBy(username)
                .menuLineItems(menuLineItems)
                .build();
    }

    public OrderResponse mapToResponse(MenuOrder order) {
        return OrderResponse.builder()
                .orderId(order.getId())
                .totalPrice(order.getTotalPrice())
                .menuLineItems(order.getMenuLineItems())
                .address(Address.builder()
                        .city(order.getCity())
                        .street(order.getStreet())
                        .house(order.getHouse())
                        .apartment(order.getApartment())
                        .build())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }

    private void throwIfHasUnavailableMenuItems(List<MenuInfo> infos) {
        boolean hasUnavailable = infos.stream().anyMatch(m -> !m.getIsAvailable());
        if (hasUnavailable) {
            var msg = String.format("Cannot create order, because some menu items are not available: %s",
                    infos);
            throw new OrderServiceException(msg, HttpStatus.NOT_FOUND);
        }
    }

    private List<MenuLineItem> getMenuLineItems(CreateOrderRequest request, List<MenuInfo> infos) {
        return infos.stream()
                .map(info -> {
                    int quantity = request.getNameToQuantity().get(info.getName());
                    return MenuLineItem.builder()
                            .menuItemName(info.getName())
                            .price(info.getPrice())
                            .quantity(quantity)
                            .build();
                }).toList();
    }

    private BigDecimal calculateTotalPrice(List<MenuLineItem> menuLineItems) {
        return menuLineItems.stream()
                .map(mi -> mi.getPrice().multiply(BigDecimal.valueOf(mi.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
