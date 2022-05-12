package com.example.healthbot;


import com.example.healthbot.data.entity.Address;
import com.example.healthbot.data.entity.Medicine;
import com.example.healthbot.data.entity.User;
import com.example.healthbot.data.repository.AddressRepository;
import com.example.healthbot.data.repository.MedicineRepository;
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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
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
    private final MedicineRepository medicineRepository;
    private final AddressRepository addressRepository;

    public HealthServiceTelegramBot(TelegramBotsApi telegramBotsApi, String botUsername, String botToken,
                                    Logic logic, UserRepository userRepository, MedicineRepository medicineRepository,
                                    AddressRepository addressRepository) throws TelegramApiException {
        this.botUsername = botUsername;
        this.botToken = botToken;
        this.logic = logic;
        this.userRepository = userRepository;
        this.medicineRepository = medicineRepository;
        this.addressRepository = addressRepository;

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
            case Answer.Text t -> {
                var userId = request.getMessage().getChatId();
                List<Medicine> list = medicineRepository.findByIdUser(userId);
                setKeyboardButtons(response, list.stream().map(Medicine::getMedicine).toList());
                response.setText(t.text());
            }
            case Answer.MedicinesChoice m -> {
                setInlineKeyboard(response, m.medicines());
                response.setText(m.text());
            }
            case Answer.AddressChoice a -> {
                var userId = request.getCallbackQuery().getMessage().getChatId();
                List<Address> list = addressRepository.findByIdUser(userId);
                var result = list.stream().map(Address::getAddress).toList();
                var buttons = new ArrayList<>(List.of("Выбор района"));
                buttons.addAll(result);
                setKeyboardButtons(response, buttons);
                response.setText(a.text());
            }
            case Answer.DistrictChoice d -> {
                setInlineKeyboard(response, d.districts());
                response.setText(d.text());
            }
            case Answer.SearchResult r -> {
//                setKeyboardButtons(response, List.of());
                response.setText(r.info());
            }
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

    private void setKeyboardButtons(SendMessage response, List<String> info)
    {
        var replyKeyboard = new ReplyKeyboardMarkup();
        replyKeyboard.setResizeKeyboard(true);
        replyKeyboard.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();
        var row = new KeyboardRow();
        info.forEach(button -> row.add(new KeyboardButton(button)));
        keyboard.add(row);

        replyKeyboard.setKeyboard(keyboard);
        response.setReplyMarkup(replyKeyboard);
    }
}
