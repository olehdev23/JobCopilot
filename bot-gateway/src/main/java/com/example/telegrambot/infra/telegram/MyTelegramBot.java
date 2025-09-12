package com.example.telegrambot.infra.telegram;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Component
public class MyTelegramBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {
    private final String botToken;
    private final BotGatewayService botGatewayService;
    private final TelegramClient telegramClient;

    /**
     * Constructor for dependency injection.
     *
     * @param botToken The bot's token, injected from application properties.
     */
    public MyTelegramBot(@Value("${telegram.bot.token}") String botToken,
                         BotGatewayService botGatewayService, TelegramClient telegramClient) {
        this.botToken = botToken;
        this.botGatewayService = botGatewayService;
        this.telegramClient = telegramClient;
    }

    @PostConstruct
    public void init() {
        try {
            List<BotCommand> commands = new ArrayList<>();
            commands.add(new BotCommand("start", "Start / reload bot"));
            commands.add(new BotCommand("prepare_application",
                    "Get Cover Letter & Interview Tips"));

            telegramClient.execute(new SetMyCommands(commands));
            log.info("Menu was successfully set.");
        } catch (TelegramApiException e) {
            log.error("Exception with menu set: {}", e.getMessage());
        }
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
     * Consumes a single update from Telegram. This is the main entry point
     * for all user interactions.
     * @param update The update received from Telegram.
     */

    @Override
    public void consume(Update update) {
        if (update.hasMessage()) {
            botGatewayService.processMessage(update.getMessage());
        } else {
            log.warn("Received an update without a message.");
        }
    }
}
