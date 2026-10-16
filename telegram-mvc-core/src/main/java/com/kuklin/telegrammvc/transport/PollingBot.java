package com.kuklin.telegrammvc.transport;

import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Long polling bot that hands every update to the handling pool, keyed by bot and chat.
 */
final class PollingBot extends TelegramLongPollingBot {

    private final String botName;
    private final String username;
    private final ChatOrderedExecutor executor;
    private final UpdateConsumer consumer;

    PollingBot(String botName, String token, String username, ChatOrderedExecutor executor, UpdateConsumer consumer) {
        super(new DefaultBotOptions(), token);
        this.botName = botName;
        this.username = username;
        this.executor = executor;
        this.consumer = consumer;
    }

    String botName() {
        return botName;
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public void onUpdateReceived(Update update) {
        Long chatId = UpdateChats.chatId(update);
        Object key = chatId == null ? null : new ChatKey(botName, chatId);
        executor.execute(key, () -> consumer.consume(botName, update));
    }

    private record ChatKey(String botName, long chatId) {
    }
}
