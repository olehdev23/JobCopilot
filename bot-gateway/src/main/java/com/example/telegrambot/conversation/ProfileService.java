package com.example.telegrambot.conversation;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public interface ProfileService {

    /**
     * Initiates the profile setup process for a new user.
     *
     * @param chatId The unique ID of the user's chat.
     * @return A SendMessage object to be sent to the user.
     */
    SendMessage startProfileSetup(long chatId);

    /**
     * Handles the user's CV text during the profile setup.
     *
     * @param chatId The unique ID of the user's chat.
     * @param cvText The text content of the user's CV.
     * @return A SendMessage object with the next instruction for the user.
     */
    SendMessage handleCvText(long chatId, String cvText);

    /**
     * Handles the user's job preferences text.
     *
     * @param chatId The unique ID of the user's chat.
     * @param preferencesText The text description
     *                        of the user's job preferences.
     * @return A SendMessage object indicating the profile is complete.
     */
    SendMessage handlePreferences(long chatId, String preferencesText);
}
