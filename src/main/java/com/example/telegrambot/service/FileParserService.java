package com.example.telegrambot.service;

import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;

/**
 * A service responsible for parsing files (like CVs) and extracting text content.
 */
public interface FileParserService {

    /**
     * Parses the given Telegram Document and extracts its text content.
     * It handles different file types like PDF and DOCX.
     *
     * @param document The Document object received from Telegram.
     * @return The extracted text as a String.
     */
    String parse(Document document) throws IOException, TelegramApiException;
}