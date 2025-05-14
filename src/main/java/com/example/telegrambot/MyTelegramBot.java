package com.example.telegrambot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct; // Якщо використовуєте @PostConstruct

import java.util.List;

@Component
public class MyTelegramBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {
    private static final Logger log = LoggerFactory.getLogger(MyTelegramBot.class);
    private final String botToken;    // Токен вашого бота (інжектується з application.properties)
    private final String botUsername; // Ім'я користувача вашого бота (інжектується з application.properties)
    private TelegramClient telegramClient; // Клієнт для взаємодії з Telegram API

    /**
     * Конструктор для ін'єкції залежностей Spring.
     * Отримує токен та ім'я користувача бота з файлу application.properties.
     *
     * @param botToken    Токен бота.
     * @param botUsername Ім'я користувача бота.
     */
    public MyTelegramBot(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.username}") String botUsername
    ) {
        this.botToken = botToken;
        this.botUsername = botUsername;
    }

    /**
     * Метод, що викликається після ініціалізації біна Spring.
     * Використовується для створення екземпляра TelegramClient,
     * оскільки botToken вже буде доступний на цьому етапі.
     */
    @PostConstruct
    public void initClient() {
        this.telegramClient = new OkHttpTelegramClient(this.botToken);
        log.info("TelegramClient ініціалізовано для бота '{}'", this.botUsername);
    }

    /**
     * Повертає токен бота.
     * Цей метод вимагається інтерфейсом {@link SpringLongPollingBot}.
     *
     * @return Токен бота.
     */
    @Override
    public String getBotToken() {
        return this.botToken;
    }

    /**
     * Повертає обробник оновлень (тобто цей самий екземпляр класу).
     * Цей метод вимагається інтерфейсом {@link SpringLongPollingBot}.
     * Стартер використовуватиме цей обробник для передачі оновлень.
     *
     * @return Екземпляр {@link LongPollingUpdateConsumer}, який оброблятиме оновлення.
     */
    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this; // Поточний екземпляр класу є обробником оновлень
    }

    // Метод getBotPath() не вимагається інтерфейсом org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot,
    // тому його реалізація тут не потрібна. Стартер подбає про необхідні налаштування.

    /**
     * Обробляє одне оновлення (Update), отримане від Telegram.
     * Цей метод вимагається інтерфейсом {@link LongPollingSingleThreadUpdateConsumer}.
     * Бібліотека автоматично викликатиме цей метод для кожного оновлення,
     * отриманого для вашого бота, завдяки стандартній обробці списку оновлень
     * в {@link LongPollingUpdateConsumer}.
     *
     * @param update Оновлення від Telegram.
     */
    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            log.info("Отримано повідомлення: \"{}\" від чату ID: {}", messageText, chatId);

            String responseText;
            if ("/start".equals(messageText)) {
                responseText = "Привіт! Я твій бот, адаптований під Spring Boot!";
            } else {
                responseText = "Spring Boot бот почув: " + messageText;
            }
            sendTextMessage(chatId, responseText);
        }
    }

    /**
     * Допоміжний метод для надсилання текстових повідомлень користувачу.
     *
     * @param chatId ID чату, куди надсилати повідомлення.
     * @param text   Текст повідомлення.
     */
    private void sendTextMessage(long chatId, String text) {
        SendMessage message = new SendMessage(String.valueOf(chatId), text);
        try {
            if (this.telegramClient == null) {
                // Ця перевірка більше для підстраховки, оскільки @PostConstruct має викликати initClient()
                log.error("TelegramClient не ініціалізовано! Повідомлення не може бути надіслане.");
                return;
            }
            this.telegramClient.execute(message);
            log.info("Надіслано повідомлення: \"{}\" до чату ID: {}", text, chatId);
        } catch (TelegramApiException e) {
            log.error("Помилка надсилання повідомлення: '{}'", e.getMessage(), e);
        }
    }
}