package com.example.healthbot.HttpClient;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Service
@AllArgsConstructor
@Slf4j
public class HttpClient {
    private static final String URL_PATH = "/";
    private final WebClient webClient;

    public Mono<String> getPage() {
        return webClient
                .get()
                .uri(URL_PATH)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError,
                        error -> Mono.error(new RuntimeException("Client side error while request")))
                .onStatus(HttpStatus::is5xxServerError,
                        error -> Mono.error(new RuntimeException("Server side error while request")))
                .bodyToMono(String.class);
    }
}
