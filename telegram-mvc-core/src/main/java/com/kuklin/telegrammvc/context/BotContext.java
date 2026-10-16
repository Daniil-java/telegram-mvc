package com.kuklin.telegrammvc.context;

import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Parsed update: everything a handler usually needs without touching {@link Update}.
 *
 * @param botName   name of the bot as configured under {@code telegram-mvc.bots}
 * @param type      kind of the update
 * @param chatId    chat to answer to, {@code null} for updates without a chat
 * @param user      sender, {@code null} for updates without one (channel posts)
 * @param messageId incoming message, or the message with the pressed button; may be {@code null}
 * @param routeKey  command without arguments ({@code "/start"}) or callback data; {@code null} for other types
 * @param args      text after the command, trimmed; empty when there is none or for other types
 * @param update    raw update, for anything not covered above
 */
public record BotContext(
        String botName,
        UpdateType type,
        Long chatId,
        ChatUser user,
        Integer messageId,
        String routeKey,
        String args,
        Update update) {
}
