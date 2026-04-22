package com.spring_authentication.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DemoApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    @Qualifier("authnUser")
    private RestTemplate authenticatedRestTemplate;

    @Test
    void shouldReturn401ForHelloWithoutAuthentication() {
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class,
                () -> restTemplate.getForObject(url("/api/authn/hello?name=jim"), String.class));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(401));
    }

    @Test
    void shouldReturnHelloWorldForHelloWithValidBasicAuth() {
        ResponseEntity<String> response = authenticatedRestTemplate.exchange(url("/api/authn/hello?name=jim"),
                HttpMethod.GET, null, String.class);

        assertThat(response.getBody()).isEqualTo("hello, jim :caller=user1");
    }

    @Test
    void shouldReturnHelloWorldForPublicEndpointWithoutAuthentication() {
        String response = restTemplate.getForObject(url("/api/anonymous/hello?name=jim"), String.class);

        assertThat(response).isEqualTo("hello, jim :caller=(null)");
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @TestConfiguration
    static class RestTemplateTestConfig {

        @Bean
        public ClientHttpRequestFactory requestFactory() {
            return new SimpleClientHttpRequestFactory();
        }

        @Bean
        public RestTemplate restTemplate(ClientHttpRequestFactory requestFactory) {
            return new RestTemplate(requestFactory);
        }

        @Bean(name = "authnUser")
        public RestTemplate authnUser(ClientHttpRequestFactory requestFactory) {
            RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(requestFactory));
            restTemplate.setInterceptors(
                    List.of(new BasicAuthenticationInterceptor("user1", "password1"), new RestTemplateLoggingFilter()));
            return restTemplate;
        }
    }

    static class RestTemplateLoggingFilter implements ClientHttpRequestInterceptor {

        @Override
        public ClientHttpResponse intercept(org.springframework.http.HttpRequest request, byte[] body,
                ClientHttpRequestExecution execution) throws IOException {
            return execution.execute(request, body);
        }
    }
}
