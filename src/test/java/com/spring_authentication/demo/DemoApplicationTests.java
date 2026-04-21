package com.spring_authentication.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DemoApplicationTests {

    @LocalServerPort
    private int port;

    @Test
    void shouldReturn401ForHelloWithoutAuthentication() {
        WebClientResponseException exception = assertThrows(
                WebClientResponseException.class,
                () -> webClient().get()
                        .uri("/api/authn/hello?name=jim")
                        .retrieve()
                        .bodyToMono(String.class)
                        .block()
        );
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(401));
    }

    @Test
    void shouldReturnHelloWorldForHelloWithValidBasicAuth() {
        String response = webClient().get()
                .uri("/api/authn/hello?name=jim")
                .header(HttpHeaders.AUTHORIZATION, basicAuth())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        assertThat(response).isEqualTo("hello, jim :caller=user");
    }

    @Test
    void shouldReturnHelloWorldForPublicEndpointWithoutAuthentication() {
        String response = webClient().get()
                .uri("/api/anonymous/hello?name=jim")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        assertThat(response).isEqualTo("hello, jim :caller=(null)");
    }

    private String basicAuth() {
        String token = "user" + ":" + "password";
        String encoded = Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }

    private WebClient webClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
    }
}
