package com.kuklin.telegrammvc.transport;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;

/**
 * {@link BotClient} backed by the telegrambots sender of a registered bot.
 */
final class DefaultBotClient implements BotClient {

    private final String botName;
    private final AbsSender sender;

    DefaultBotClient(String botName, AbsSender sender) {
        this.botName = botName;
        this.sender = sender;
    }

    @Override
    public String botName() {
        return botName;
    }

    @Override
    public <T extends Serializable, M extends BotApiMethod<T>> T execute(M method) throws TelegramApiException {
        return sender.execute(method);
    }

    @Override
    public Message execute(SendPhoto method) throws TelegramApiException {
        return sender.execute(method);
    }

    @Override
    public Message execute(SendDocument method) throws TelegramApiException {
        return sender.execute(method);
    }

    @Override
    public Serializable execute(EditMessageMedia method) throws TelegramApiException {
        return sender.execute(method);
    }
}
