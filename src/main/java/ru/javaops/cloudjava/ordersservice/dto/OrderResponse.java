package ru.javaops.cloudjava.ordersservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.javaops.cloudjava.ordersservice.storage.model.MenuLineItem;
import ru.javaops.cloudjava.ordersservice.storage.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {
    private Long orderId;
    private BigDecimal totalPrice;
    private List<MenuLineItem> menuLineItems;
    private Address address;
    private OrderStatus status;
    private LocalDateTime createdAt;
}
