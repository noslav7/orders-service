package ru.javaops.cloudjava.ordersservice.storage.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table("orders")
public class MenuOrder {
    @Id
    private Long id;
    @Column("total_price")
    private BigDecimal totalPrice;
    private String city;
    private String street;
    private int house;
    private int apartment;
    @Column("menu_line_items")
    private List<MenuLineItem> menuLineItems;
    private OrderStatus status;
    @Column("created_by")
    private String createdBy;
    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;
    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;

}