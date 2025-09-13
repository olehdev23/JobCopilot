package com.example.telegrambot.infra.telegram;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Service
@Slf4j
public class FileDownloaderServiceImpl implements FileDownloaderService {

    private final String botToken;
    private final TelegramClient telegramClient;

    public FileDownloaderServiceImpl(@Value("${telegram.bot.token}") String botToken,
                                     TelegramClient telegramClient) {
        this.botToken = botToken;
        this.telegramClient = telegramClient;
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
