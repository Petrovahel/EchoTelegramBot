package com.example.telegrambot.service;

import com.example.telegrambot.config.BotConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    private static final String FIRST_BUTTON = "Кнопка 1";
    private static final String SECOND_BUTTON = "Кнопка 2";
    public static final String MENU_1 = "menu1";
    public static final String MENU_2 = "menu2";

    private final BotConfig config;

    public TelegramBot(BotConfig config) {
        super(config.getToken());
        this.config = config;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            if (messageText.equals("/start")) {
                sendTextMessage(chatId, "Привіт! Ось меню:");
                sendMenu(chatId);
            } else {
                sendTextMessage(chatId, "Я не розумію цю команду.");
            }
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update);
        }
    }

    private void handleCallbackQuery(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();

        switch (callbackData) {
            case MENU_1 -> sendMenu1(chatId);
            case MENU_2 -> sendMenu2(chatId);
            default -> sendTextMessage(chatId, callbackData);
        }
    }

    private void sendMenu(Long chatId) {
        SendMessage message = createMessageWithKeyboard(chatId, "Оберіть кнопку",
                Collections.singletonList(
                        Arrays.asList(createButton("Меню 1", MENU_1), createButton("Меню 2", MENU_2))
                ));
        sendMessage(message);
    }


    private void sendMenu1(Long chatId) {
        SendMessage message = createMessageWithKeyboard(chatId, "Меню 1: Оберіть кнопку",
                Arrays.asList(
                        Arrays.asList(createButton(FIRST_BUTTON, FIRST_BUTTON), createButton(SECOND_BUTTON, SECOND_BUTTON)),
                        Collections.singletonList(createButton("Далі", MENU_2))
                ));
        sendMessage(message);
    }

    private void sendMenu2(Long chatId) {
        SendMessage message = createMessageWithKeyboard(chatId, "Меню 2: Оберіть кнопку",
                Arrays.asList(
                        Arrays.asList(createButton(FIRST_BUTTON, FIRST_BUTTON), createButton(SECOND_BUTTON, SECOND_BUTTON)),
                        Collections.singletonList(createButton("Назад", MENU_1))
                ));
        sendMessage(message);
    }

    private InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton(text);
        button.setCallbackData(callbackData);
        return button;
    }

    private SendMessage createMessageWithKeyboard(Long chatId, String text, List<List<InlineKeyboardButton>> keyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        message.setReplyMarkup(markup);
        return message;
    }

    private void sendTextMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        sendMessage(message);
    }

    private void sendMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Помилка відправки повідомлення: {}", e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

}
