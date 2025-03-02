package ru.javaops.cloudjava.ordersservice.storage.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.postgresql.codec.Json;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.http.HttpStatus;
import ru.javaops.cloudjava.ordersservice.exception.OrderServiceException;
import ru.javaops.cloudjava.ordersservice.storage.model.MenuLineItem;

import java.util.List;

@WritingConverter
@RequiredArgsConstructor
public class MenuLineItemCollectionWriteConverter implements Converter<List<MenuLineItem>, Json> {

    private final ObjectMapper objectMapper;

    @Override
    public Json convert(@NotNull List<MenuLineItem> menuLineItems) {
        try {
            return Json.of(objectMapper.writeValueAsString(menuLineItems));
        } catch (JsonProcessingException e) {
            var msg = String.format("Failed to convert MenuLineItemCollection %s to JSON", menuLineItems);
            throw new OrderServiceException(msg, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}