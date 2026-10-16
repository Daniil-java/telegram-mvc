package com.kuklin.telegrammvc.transport;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;

/**
 * Telegram API calls of one bot. The only place in the library where requests are executed,
 * so tests can replace it with a recording fake.
 */
public interface BotClient {

    /**
     * Name of the bot as configured under {@code telegram-mvc.bots}.
     */
    String botName();

    <T extends Serializable, M extends BotApiMethod<T>> T execute(M method) throws TelegramApiException;

    Message execute(SendPhoto method) throws TelegramApiException;

    Message execute(SendDocument method) throws TelegramApiException;

    Serializable execute(EditMessageMedia method) throws TelegramApiException;
}
