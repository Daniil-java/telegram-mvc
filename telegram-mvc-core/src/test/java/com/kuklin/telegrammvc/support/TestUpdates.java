package com.kuklin.telegrammvc.support;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;

/**
 * Builds updates the way Telegram sends them, with only the fields the library reads.
 */
public final class TestUpdates {

    public static final long CHAT_ID = 100L;
    public static final long USER_ID = 7L;
    public static final int MESSAGE_ID = 55;

    private TestUpdates() {
    }

    public static Update text(String text) {
        Message message = message();
        message.setText(text);
        Update update = new Update();
        update.setMessage(message);
        return update;
    }

    public static Update photo() {
        Message message = message();
        message.setPhoto(List.of(new PhotoSize("file", "unique", 90, 90, 1000, null)));
        Update update = new Update();
        update.setMessage(message);
        return update;
    }

    public static Update press(String data) {
        CallbackQuery callback = new CallbackQuery();
        callback.setId("callback-1");
        callback.setFrom(user());
        callback.setData(data);
        callback.setMessage(message());
        Update update = new Update();
        update.setCallbackQuery(callback);
        return update;
    }

    /**
     * A press of a button under an inline-mode message: Telegram sends no message to edit.
     */
    public static Update pressWithoutMessage(String data) {
        Update update = press(data);
        update.getCallbackQuery().setMessage(null);
        return update;
    }

    private static Message message() {
        Chat chat = new Chat();
        chat.setId(CHAT_ID);
        chat.setType("private");
        Message message = new Message();
        message.setMessageId(MESSAGE_ID);
        message.setChat(chat);
        message.setFrom(user());
        return message;
    }

    private static User user() {
        User user = new User();
        user.setId(USER_ID);
        user.setFirstName("Ann");
        user.setUserName("ann");
        user.setLanguageCode("ru");
        return user;
    }
}
