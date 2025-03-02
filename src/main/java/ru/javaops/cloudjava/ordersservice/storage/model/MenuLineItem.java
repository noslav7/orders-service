package ru.javaops.cloudjava.ordersservice.storage.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MenuLineItem {
    private String menuItemName;
    private BigDecimal price;
    private Integer quantity;
}