package com.ayush.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CorrelationIdGlobalFilter implements GlobalFilter, Ordered {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final String CORRELATION_ID_KEY = "correlationId";
    private static final Logger log = LoggerFactory.getLogger(CorrelationIdGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String correlationId = resolveCorrelationId(exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER));
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(CORRELATION_ID_HEADER, correlationId)
                .build();

        exchange.getResponse().getHeaders().set(CORRELATION_ID_HEADER, correlationId);

        return chain.filter(exchange.mutate().request(mutatedRequest).build())
                .contextWrite(context -> context.put(CORRELATION_ID_KEY, correlationId))
                .doFirst(() -> logWithCorrelationId(correlationId,
                        "Incoming request {} {}",
                        mutatedRequest.getMethod(),
                        mutatedRequest.getURI().getPath()))
                .doFinally(signalType -> logWithCorrelationId(correlationId,
                        "Completed request {} {} with status {}",
                        mutatedRequest.getMethod(),
                        mutatedRequest.getURI().getPath(),
                        exchange.getResponse().getStatusCode()));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private String resolveCorrelationId(String correlationId) {
        if (correlationId == null || correlationId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return correlationId;
    }

    private void logWithCorrelationId(String correlationId, String message, Object... arguments) {
        MDC.put(CORRELATION_ID_KEY, correlationId);
        try {
            log.info(message, arguments);
        } finally {
            MDC.remove(CORRELATION_ID_KEY);
        }
    }
}
