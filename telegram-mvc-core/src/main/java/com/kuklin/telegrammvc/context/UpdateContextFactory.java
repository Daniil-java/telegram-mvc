package com.kuklin.telegrammvc.context;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.HashMap;
import java.util.Map;

/**
 * Turns an {@link Update} into a {@link BotContext}.
 */
public class UpdateContextFactory {

    private final Map<String, String> usernames;

    /**
     * @param usernames bot usernames by bot name; used to tell {@code /start@this_bot} from {@code /start@other_bot}.
     *                  Bots without a username accept a command with any {@code @} suffix.
     */
    public UpdateContextFactory(Map<String, String> usernames) {
        Map<String, String> known = new HashMap<>();
        usernames.forEach((bot, username) -> {
            if (username != null && !username.isBlank()) {
                known.put(bot, username);
            }
        });
        this.usernames = Map.copyOf(known);
    }

    public BotContext create(String botName, Update update) {
        if (update.hasCallbackQuery()) {
            return action(botName, update);
        }
        if (update.hasMessage()) {
            return message(botName, update);
        }
        return new BotContext(botName, UpdateType.OTHER, null, null, null, null, "", update);
    }

    private BotContext action(String botName, Update update) {
        CallbackQuery callback = update.getCallbackQuery();
        MaybeInaccessibleMessage message = callback.getMessage();
        Long chatId = message != null ? message.getChatId() : callback.getFrom().getId();
        Integer messageId = message != null ? message.getMessageId() : null;
        return new BotContext(botName, UpdateType.ACTION, chatId, user(callback.getFrom()), messageId,
                callback.getData(), "", update);
    }

    private BotContext message(String botName, Update update) {
        Message message = update.getMessage();
        ChatUser user = user(message.getFrom());
        if (message.hasText() && message.getText().startsWith("/")) {
            return command(botName, update, message, user);
        }
        UpdateType type = message.hasText() ? UpdateType.TEXT
                : hasMedia(message) ? UpdateType.MEDIA
                : UpdateType.OTHER;
        return new BotContext(botName, type, message.getChatId(), user, message.getMessageId(), null, "", update);
    }

    private BotContext command(String botName, Update update, Message message, ChatUser user) {
        String text = message.getText().strip();
        int space = indexOfWhitespace(text);
        String command = space < 0 ? text : text.substring(0, space);
        String args = space < 0 ? "" : text.substring(space).strip();

        int at = command.indexOf('@');
        if (at >= 0) {
            String addressee = command.substring(at + 1);
            command = command.substring(0, at);
            String username = usernames.get(botName);
            if (username != null && !username.equalsIgnoreCase(addressee)) {
                // a command for another bot in the same group chat
                return new BotContext(botName, UpdateType.OTHER, message.getChatId(), user, message.getMessageId(),
                        null, "", update);
            }
        }
        return new BotContext(botName, UpdateType.COMMAND, message.getChatId(), user, message.getMessageId(),
                command, args, update);
    }

    private static int indexOfWhitespace(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (Character.isWhitespace(text.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    private static boolean hasMedia(Message message) {
        return message.hasPhoto() || message.hasDocument() || message.hasVoice() || message.hasVideo()
                || message.hasAudio() || message.hasVideoNote() || message.hasAnimation() || message.hasSticker();
    }

    private static ChatUser user(User from) {
        if (from == null) {
            return null;
        }
        return new ChatUser(from.getId(), from.getUserName(), from.getFirstName(), from.getLastName(),
                from.getLanguageCode());
    }
}
