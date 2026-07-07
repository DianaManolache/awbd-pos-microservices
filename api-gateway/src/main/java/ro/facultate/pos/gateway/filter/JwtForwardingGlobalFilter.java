package ro.facultate.pos.gateway.filter;

import org.springframework.core.Ordered;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Citeste cookie-ul AUTH_TOKEN (JWT emis de User Service la login) si il
 * ataseaza ca header Authorization: Bearer pe cererea proxy-ata catre
 * Catalog/Sales/User - acestea nu au acces la sesiunea Gateway-ului (care
 * de fapt nu exista - sesiunea traieste in User Service) si nu pot afla
 * altfel cine a facut cererea.
 */
@Component
public class JwtForwardingGlobalFilter implements GlobalFilter, Ordered {

    private static final String AUTH_COOKIE_NAME = "AUTH_TOKEN";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        HttpCookie cookie = request.getCookies().getFirst(AUTH_COOKIE_NAME);
        if (cookie == null || cookie.getValue().isBlank()) {
            return chain.filter(exchange);
        }

        ServerHttpRequest mutatedRequest = request.mutate()
                .header("Authorization", "Bearer " + cookie.getValue())
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return -2;
    }
}
