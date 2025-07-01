package com.example.telegrambot.bot;

import com.example.telegrambot.service.RegistrationService;
import com.example.telegrambot.service.UpdateDispatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Slf4j
@Component
public class MyTelegramBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer, MessageSender, FileDownloaderService {

    private final String botToken;
    private final TelegramClient telegramClient;
    private final UpdateDispatcher updateDispatcher;
    private final RegistrationService registrationService;

    /**
     * Constructor for dependency injection.
     * @param botToken            The bot's token, injected from application properties.
     */
    public MyTelegramBot(@Value("${telegram.bot.token}") String botToken,
                         UpdateDispatcher updateDispatcher, RegistrationService registrationService) {
        this.botToken = botToken;
        this.updateDispatcher = updateDispatcher;
        this.telegramClient = new OkHttpTelegramClient(botToken);
        this.registrationService = registrationService;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    /**
     * Consumes a single update from Telegram. This is the main entry point for all user interactions.
     * @param update The update received from Telegram.
     */

    @Override
    public void consume(Update update) {
        if (!update.hasMessage()) {
            log.warn("Received an update without a message");
        }

            Message message = update.getMessage();
            String messageText = message.getText();
            long chatId = message.getChatId();

            log.info("Received message: \"{}\" from chat ID: {}", messageText, chatId);
            if ("/start".equals(messageText)) {
                registrationService.registerUser(message);
                sendMainMenu(chatId, message.getChat().getFirstName());
            } else {
                updateDispatcher.dispatch(message);
            }
        }

    /**
     * Sends the main menu with a "Setup Profile" button.
     * @param chatId   The ID of the chat to send the message to.
     * @param userName The user's first name for a personalized greeting.
     */
    private void sendMainMenu(long chatId, String userName) {
        String responseText = "Hello, " + userName + "! I've registered you.\n\n"
                + "Let's set up your profile so I can provide the best assistance.";

        SendMessage message = new SendMessage(String.valueOf(chatId), responseText);

        ReplyKeyboardMarkup keyboardMarkup = ReplyKeyboardMarkup.builder()
                .keyboardRow(new KeyboardRow("Setup Profile")) // Створюємо один ряд з однією кнопкою
                .resizeKeyboard(true)
                .oneTimeKeyboard(true)
                .build();

        message.setReplyMarkup(keyboardMarkup);

        send(message);
    }

    /**
     * A unified method for sending messages to avoid duplicating the try-catch block.
     * @param message The SendMessage object to be sent.
     */
    @Override
    public void send(SendMessage message) {
        try {
            telegramClient.execute(message);
            log.info("Sent message to chat ID: {}", message.getChatId());
        } catch (TelegramApiException e) {
            log.error("Failed to send message to chat ID: {}. Error: {}", message.getChatId(), e.getMessage());
        }
    }

    @Override
    public InputStream downloadFile(String fileId) throws TelegramApiException, IOException {
        GetFile getFileRequest = new GetFile(fileId);
        File file = telegramClient.execute(getFileRequest);
        String fileUrl = file.getFileUrl(botToken);
        log.info("Downloading file from URL: {}", fileUrl);
        return new URL(fileUrl).openStream();
    }
}