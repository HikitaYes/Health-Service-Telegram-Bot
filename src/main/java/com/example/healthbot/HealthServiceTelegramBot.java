package com.example.healthbot;

import com.example.healthbot.HttpClient.HttpClient;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import reactor.core.publisher.Mono;

@Slf4j
@Getter
@Component
public class HealthServiceTelegramBot extends TelegramLongPollingBot {

    private Message requestMessage = new Message();
    private final SendMessage response = new SendMessage();

    private final String botUsername;
    private final String botToken;

    private final HttpClient httpClient;

    public HealthServiceTelegramBot(TelegramBotsApi telegramBotsApi, String botUsername, String botToken,
                                    HttpClient httpClient) throws TelegramApiException {
        this.botUsername = botUsername;
        this.botToken = botToken;
        this.httpClient = httpClient;

        telegramBotsApi.registerBot(this);
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update request) {
        requestMessage = request.getMessage();
        response.setChatId(requestMessage.getChatId().toString());

        //Do http request and print raw html data
        Mono<String> data = this.httpClient.getPage();
        data.subscribe(System.out::println);

        if (requestMessage.getText().equals("/start"))
            defaultMsg(response, "ААААА наконец то я разговариваю");
        else
            defaultMsg(response, "Ты че ты че, сюда заходи!");

        log.info("User text[{}]", requestMessage.getText());

    }

    private void defaultMsg(SendMessage response, String msg) throws TelegramApiException {
        response.setText(msg);
        execute(response);
    }
}
