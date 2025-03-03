package ru.javaops.cloudjava.ordersservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Address {
    @NotBlank(message = "Название города не может быть пустым.")
    private String city;
    @NotBlank(message = "Название улицы не может быть пустым.")
    private String street;
    @Positive(message = "Номер дома должен быть > 0")
    private int house;
    @Positive(message = "Номер квартиры должен быть > 0")
    private int apartment;
}
