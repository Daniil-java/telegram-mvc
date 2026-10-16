package com.kuklin.telegrammvc.transport;

import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateChatsTest {

    @Test
    void takesChatOfMessage() {
        Update update = new Update();
        update.setMessage(message(42L));

        assertThat(UpdateChats.chatId(update)).isEqualTo(42L);
    }

    @Test
    void takesChatOfMessageWithButton() {
        CallbackQuery callback = new CallbackQuery();
        callback.setMessage(message(42L));
        callback.setFrom(user(7L));
        Update update = new Update();
        update.setCallbackQuery(callback);

        assertThat(UpdateChats.chatId(update)).isEqualTo(42L);
    }

    @Test
    void takesUserOfCallbackWithoutMessage() {
        CallbackQuery callback = new CallbackQuery();
        callback.setFrom(user(7L));
        Update update = new Update();
        update.setCallbackQuery(callback);

        assertThat(UpdateChats.chatId(update)).isEqualTo(7L);
    }

    @Test
    void takesUserOfInlineQuery() {
        InlineQuery query = new InlineQuery();
        query.setFrom(user(7L));
        Update update = new Update();
        update.setInlineQuery(query);

        assertThat(UpdateChats.chatId(update)).isEqualTo(7L);
    }

    @Test
    void returnsNullForEmptyUpdate() {
        assertThat(UpdateChats.chatId(new Update())).isNull();
    }

    private static Message message(long chatId) {
        Chat chat = new Chat();
        chat.setId(chatId);
        Message message = new Message();
        message.setChat(chat);
        return message;
    }

    private static User user(long id) {
        User user = new User();
        user.setId(id);
        return user;
    }
}
