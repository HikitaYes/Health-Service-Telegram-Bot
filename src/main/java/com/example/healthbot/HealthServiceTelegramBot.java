package com.example.healthbot;


import com.example.healthbot.data.entity.User;
import com.example.healthbot.data.repository.UserRepository;
import com.example.healthbot.logic.*;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Getter
@Component
public class HealthServiceTelegramBot extends TelegramLongPollingBot {

    private final String botUsername;
    private final String botToken;

    private final Logic logic;

    private final UserRepository userRepository;

    public HealthServiceTelegramBot(TelegramBotsApi telegramBotsApi, String botUsername, String botToken,
                                    Logic logic, UserRepository userRepository) throws TelegramApiException {
        this.botUsername = botUsername;
        this.botToken = botToken;
        this.logic = logic;
        this.userRepository = userRepository;

        telegramBotsApi.registerBot(this);
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update request) {
        var response = new SendMessage();
        var requestMessage = "";

        if (request.hasMessage()) {
            var message = request.getMessage();
            requestMessage = message.getText();
            response.setChatId(message.getChatId().toString());
            log.info("User text[{}]", requestMessage);
        }
        else if (request.hasCallbackQuery()) {
            var callback = request.getCallbackQuery();
            requestMessage = callback.getData();
            response.setChatId(callback.getMessage().getChatId().toString());
            log.info("Callback text[{}]", requestMessage);
        }

        Answer answer = logic.getAnswer(requestMessage);
        switch (answer) {
            case Answer.Text t -> response.setText(t.text());
            case Answer.MedicinesChoice m -> {
                setInlineKeyboard(response, m.medicines());
                response.setText(m.text());
            }
            case Answer.DistrictChoice d -> {
                setInlineKeyboard(response, d.districts());
                response.setText(d.text());
            }
            case Answer.SearchResult r -> response.setText(r.info());
        }
        execute(response);
    }

    private void setInlineKeyboard(SendMessage response, Map<String, String> info) {
        var inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();

        for (var entry : info.entrySet())
        {
            List<InlineKeyboardButton> row = new ArrayList<>();
            var button = new InlineKeyboardButton();
            button.setText(entry.getValue());
            button.setCallbackData(entry.getKey());
            row.add(button);
            rowList.add(row);
        }

        inlineKeyboard.setKeyboard(rowList);
        response.setReplyMarkup(inlineKeyboard);

    }
}
