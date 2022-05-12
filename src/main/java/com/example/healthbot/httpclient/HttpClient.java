package com.example.healthbot.httpclient;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Service
@AllArgsConstructor
@Slf4j
public class HttpClient {
    private final WebClient webClient;

    public String getPage(String uri, MultiValueMap<String, String> map) {
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                    .path(uri)
                    .queryParams(map)
                    .build()
                )
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError,
                        error -> Mono.error(new RuntimeException("Client side error while request")))
                .onStatus(HttpStatus::is5xxServerError,
                        error -> Mono.error(new RuntimeException("Server side error while request")))
                .bodyToMono(String.class)
                .block();
    }
}
