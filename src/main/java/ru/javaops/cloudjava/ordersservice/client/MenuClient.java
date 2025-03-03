package ru.javaops.cloudjava.ordersservice.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import ru.javaops.cloudjava.ordersservice.config.props.OrderServiceProps;
import ru.javaops.cloudjava.ordersservice.dto.GetMenuInfoRequest;
import ru.javaops.cloudjava.ordersservice.dto.GetMenuInfoResponse;
import ru.javaops.cloudjava.ordersservice.exception.OrderServiceException;

import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
public class MenuClient {

    private final WebClient webClient;
    private final OrderServiceProps props;

    public Mono<GetMenuInfoResponse> getMenuInfo(GetMenuInfoRequest request) {
        return webClient
                .post()
                .uri(props.getMenuInfoPath())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                // при получении 500 ответа от Menu Service пробрасываем дальше исключение
                // со статусом SERVICE_UNAVAILABLE
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new OrderServiceException("Menu Service Unavailable", HttpStatus.SERVICE_UNAVAILABLE)))
                .bodyToMono(GetMenuInfoResponse.class)
                // таймаут на получение данных от Menu Service. Если с сервисом не удается
                // установить соединение или данные не приходят в течение указанного времени,
                // то выбрасывается java.util.concurrent.TimeoutException
                .timeout(props.getDefaultTimeout())
                // если оператор retryWhen стоит после оператора timeout, то тогда таймаут
                // применяется к каждой повторной попытке отправить запрос, если поменять их
                // местами, то таймаут будет один на первую попытку запроса и на все ретраи.
                .retryWhen(
                        // указываем, что интервалы между повторными попытками запросов должны расти
                        // экспоненциально. Такой подход повысит шансы Menu Service на
                        // успешное восстановление.
                        Retry.backoff(props.getRetryCount(), props.getRetryBackoff())
                                // вносим фактор неопределенности, чтобы разные инстансы Orders Service
                                // не посылали повторные попытки одновременно
                                .jitter(props.getRetryJitter())
                                // повторные попытки будут посылаться только в случае OrderServiceException,
                                // которое выбрасывается, как выше указано, при получении 500 ответа от Menu Service и
                                // java.util.concurrent.TimeoutException, которое выбрасывается при
                                // наступлении таймаута
                                .filter(t -> {
                                    return t instanceof OrderServiceException || t instanceof TimeoutException;
                                })
                                // когда все повторные попытки отправить запрос исчерпаны, выбрасываем
                                // OrderServiceException со статусом SERVICE_UNAVAILABLE.
                                .onRetryExhaustedThrow(((retryBackoffSpec, retrySignal) -> {
                                    var msg = "Failed to fetch info from Menu Service after max retry attempts";
                                    throw new OrderServiceException(msg, HttpStatus.SERVICE_UNAVAILABLE);
                                }))
                );
    }
}
