package com.example.telegrambot.infra.telegram;

import java.io.IOException;
import java.io.InputStream;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface FileDownloaderService {
    /**
     * Downloads a file from Telegram servers by its file ID.
     *
     * @param fileId The unique file identifier.
     * @return An InputStream to read the file's content.
     */
    InputStream downloadFile(String fileId) throws TelegramApiException, IOException;
}
