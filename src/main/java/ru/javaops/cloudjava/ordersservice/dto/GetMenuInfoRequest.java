package ru.javaops.cloudjava.ordersservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class GetMenuInfoRequest {
    private Set<String> menuNames;
}
