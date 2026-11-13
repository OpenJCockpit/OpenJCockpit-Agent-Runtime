package nl.metafactory.common.oauth;

import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;

public final class ServiceRestClients {

    private ServiceRestClients() {
    }

    public static RestClient create(String baseUrl, ClientCredentialsTokens tokens, Duration connectTimeout,
                                    Duration readTimeout) {
        var factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder()
                .connectTimeout(connectTimeout).followRedirects(HttpClient.Redirect.NEVER).build());
        factory.setReadTimeout(readTimeout);
        return RestClient.builder()
                .baseUrl(normalizeBaseUrl(baseUrl))
                .requestFactory(factory)
                .requestInterceptor(bearer(tokens))
                .build();
    }

    static ClientHttpRequestInterceptor bearer(ClientCredentialsTokens tokens) {
        return (request, body, execution) -> {
            String authorization = tokens.authorization()
                    .orElseThrow(() -> new IllegalStateException("no service identity configured for internal calls"));
            request.getHeaders().set(HttpHeaders.AUTHORIZATION, authorization);
            return execution.execute(request, body);
        };
    }

    public static String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("base URL is not configured");
        }
        String trimmed = baseUrl.strip().replaceAll("/+$", "");
        URI uri = URI.create(trimmed);
        if (!("http".equals(uri.getScheme()) || "https".equals(uri.getScheme())) || uri.getHost() == null) {
            throw new IllegalArgumentException("base URL must be an absolute http(s) URL");
        }
        return trimmed;
    }
}
