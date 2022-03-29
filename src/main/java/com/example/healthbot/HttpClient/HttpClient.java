package com.example.healthbot.HttpClient;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriUtils;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


@Service
@AllArgsConstructor
@Slf4j
public class HttpClient {
    private final WebClient webClient;

    public Mono<String> getPage(String uri, MultiValueMap<String, String> map) {
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
                .bodyToMono(String.class);
    }
}
