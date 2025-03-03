package ru.javaops.cloudjava.ordersservice.dto;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateOrderRequest {
    /**
     * Название заказываемого блюда и его количество.
     */
    private Map<String, Integer> nameToQuantity;
    /**
     * Адрес доставки.
     */
    @Valid
    private Address address;
}
