package ru.javaops.cloudjava.ordersservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.javaops.cloudjava.ordersservice.dto.CreateOrderRequest;
import ru.javaops.cloudjava.ordersservice.dto.OrderResponse;
import ru.javaops.cloudjava.ordersservice.dto.SortBy;
import ru.javaops.cloudjava.ordersservice.service.MenuOrderService;

@Tag(name = "MenuOrderController", description = "REST API для работы с заказами.")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/menu-orders")
public class MenuOrderController {

    public static final String USER_HEADER = "X-User-Name";

    private final MenuOrderService menuOrderService;

    @Operation(
            summary = "${api.submit-order.summary}",
            description = "${api.submit-order.description}"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "${api.response.submitOk}"),
            @ApiResponse(
                    responseCode = "400",
                    description = "${api.response.submitBadRequest}",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "${api.response.submitNotFound}",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "${api.response.submitInternalError}",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<OrderResponse> submitMenuOrder(@RequestBody @Valid CreateOrderRequest request,
                                               @RequestHeader(USER_HEADER) String username) {
        log.info("Received POST request to submit order: {}", request);
        return menuOrderService.createOrder(request, username);
    }

    @Operation(
            summary = "${api.get-orders.summary}",
            description = "${api.get-orders.description}"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.response.getOk}"),
            @ApiResponse(
                    responseCode = "400",
                    description = "${api.response.getBadRequest}",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @GetMapping
    public Flux<OrderResponse> getOrdersOfUser(
            @RequestParam(value = "from", defaultValue = "0")
            @PositiveOrZero(message = "Страница должна быть >= 0.")
            int from,
            @RequestParam(value = "size", defaultValue = "10")
            @Positive(message = "Размер страницы должен быть > 0.")
            int size,
            @RequestParam(value = "sortBy", defaultValue = "date_asc")
            @NotBlank(message = "Параметр сортировки не должен быть пустым.")
            String sortBy,
            @RequestHeader(USER_HEADER) String username) {
        log.info("Received request to GET orders of user with name={}", username);
        return menuOrderService.getOrdersOfUser(username, SortBy.fromString(sortBy), from, size);
    }
}
