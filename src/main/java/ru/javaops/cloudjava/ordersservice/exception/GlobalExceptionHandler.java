package ru.javaops.cloudjava.ordersservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обработчик срабатывает, когда присутствуют ошибки валидации в теле запроса.
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleValidationExceptions(WebExchangeBindException ex, ServerHttpRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getGlobalErrors().forEach(e -> {
            errors.put(e.getObjectName(), e.getDefaultMessage());
        });
        ex.getBindingResult().getFieldErrors().forEach(e -> {
            errors.put(e.getField(), e.getDefaultMessage());
        });
        log.error("Intercepted validation exception. Errors: {}", errors);

        var pd = createProblemDetail(ex.getMessage(), HttpStatus.BAD_REQUEST, request);
        pd.setProperty("invalid_params", errors);
        return Mono.just(new ResponseEntity<>(pd, HttpStatus.BAD_REQUEST));
    }

    /**
     * Обработчик срабатывает, когда не удается прочесть тело входящего запроса.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, ServerHttpRequest request) {
        log.error("Intercepted HttpMessageNotReadableException. Message: {}", ex.getMessage());
        var badRequest = HttpStatus.BAD_REQUEST;
        ProblemDetail pd = createProblemDetail(ex.getMessage(), badRequest, request);
        return Mono.just(new ResponseEntity<>(pd, badRequest));
    }

    /**
     * Обработчик срабатывает, когда в методах restController-а есть параметры, помеченные @Valid,
     * например: @PathVariable("id") @Positive(message = "id должен быть > 0.") @Valid Long id,
     * но проверку они не проходят.
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleHandlerMethodValidationException(HandlerMethodValidationException ex, ServerHttpRequest request) {
        var pd = ex.getBody();
        Map<String, String> errors = new HashMap<>();
        ex.getAllValidationResults().forEach(result -> {
            result.getResolvableErrors().forEach(e -> {
                errors.put(result.getMethodParameter().getParameterName(), e.getDefaultMessage());
            });
        });
        log.error("Intercepted HandlerMethodValidationException. Errors: {}", errors);
        pd.setProperty("invalid_params", errors);
        pd.setStatus(HttpStatus.BAD_REQUEST);
        pd.setInstance(request.getURI());
        return Mono.just(new ResponseEntity<>(pd, HttpStatus.BAD_REQUEST));
    }

    /**
     * Обработчик срабатывает на выбрасываемое сервисом исключение [OrderServiceException].
     */
    @ExceptionHandler(OrderServiceException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleOrderServiceException(OrderServiceException ex, ServerHttpRequest request) {
        log.error("Intercepted OrderServiceException. Status: {}, Message: {}", ex.getStatus(), ex.getMessage());
        var pd = createProblemDetail(ex.getMessage(), ex.getStatus(), request);
        return Mono.just(new ResponseEntity<>(pd, ex.getStatus()));
    }

    private static ProblemDetail createProblemDetail(String message, HttpStatus status, ServerHttpRequest request) {
        var pd = ProblemDetail.forStatusAndDetail(status, message);
        pd.setProperty("timestamp", Instant.now());
        pd.setInstance(request.getURI());
        return pd;
    }
}
