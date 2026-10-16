package com.kuklin.telegrammvc.transport;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.meta.generics.LongPollingBot;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

/**
 * Starts long polling for a bot. Replaced in tests to avoid network calls.
 */
@FunctionalInterface
public interface BotSessionFactory {

    BotSession start(LongPollingBot bot) throws TelegramApiException;

    /**
     * Registers the bot in {@link TelegramBotsApi}: clears its webhook and starts a {@link DefaultBotSession}.
     */
    static BotSessionFactory telegramBotsApi() {
        return bot -> new TelegramBotsApi(DefaultBotSession.class).registerBot(bot);
    }
}
