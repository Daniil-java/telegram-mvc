package com.kuklin.telegrammvc.render;

import com.kuklin.telegrammvc.context.BotContext;
import com.kuklin.telegrammvc.context.UpdateContextFactory;
import com.kuklin.telegrammvc.support.RecordingBotClient;
import com.kuklin.telegrammvc.support.TestUpdates;
import com.kuklin.telegrammvc.view.RenderStrategy;
import com.kuklin.telegrammvc.view.View;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.Map;

import static com.kuklin.telegrammvc.support.TestUpdates.CHAT_ID;
import static com.kuklin.telegrammvc.support.TestUpdates.MESSAGE_ID;
import static com.kuklin.telegrammvc.support.TestUpdates.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;

class ViewRendererTest {

    private final RecordingBotClient client = new RecordingBotClient("simple");
    private final ViewRenderer renderer = new ViewRenderer(name -> client);
    private final UpdateContextFactory contexts = new UpdateContextFactory(Map.of());

    private final View menu = View.text("Menu")
            .row().button("Time", "time:show")
            .row().urlButton("Site", "https://example.com")
            .build();

    @Test
    void sendsNewMessageForCommand() throws Exception {
        renderer.render(ctx(TestUpdates.text("/start")), menu);

        SendMessage message = (SendMessage) client.calls().get(0);
        assertThat(message.getChatId()).isEqualTo(String.valueOf(CHAT_ID));
        assertThat(message.getText()).isEqualTo("Menu");
        InlineKeyboardMarkup markup = (InlineKeyboardMarkup) message.getReplyMarkup();
        assertThat(markup.getKeyboard()).hasSize(2);
        assertThat(markup.getKeyboard().get(0).get(0).getCallbackData()).isEqualTo("time:show");
        assertThat(markup.getKeyboard().get(1).get(0).getUrl()).isEqualTo("https://example.com");
    }

    @Test
    void sendsNoMarkupWithoutButtons() throws Exception {
        renderer.render(ctx(TestUpdates.text("/start")), View.text("Hi").build());

        assertThat(((SendMessage) client.calls().get(0)).getReplyMarkup()).isNull();
    }

    @Test
    void editsMessageWithButtonForPress() throws Exception {
        renderer.render(ctx(TestUpdates.press("nav:main")), menu);

        EditMessageText edit = (EditMessageText) client.calls().get(0);
        assertThat(edit.getChatId()).isEqualTo(String.valueOf(CHAT_ID));
        assertThat(edit.getMessageId()).isEqualTo(MESSAGE_ID);
        assertThat(edit.getText()).isEqualTo("Menu");
        assertThat(edit.getReplyMarkup().getKeyboard()).hasSize(2);
    }

    @Test
    void removesButtonsWhenEditedViewHasNone() throws Exception {
        renderer.render(ctx(TestUpdates.press("done")), View.text("Done").build());

        EditMessageText edit = (EditMessageText) client.calls().get(0);
        assertThat(edit.getReplyMarkup().getKeyboard()).isEmpty();
    }

    @Test
    void sendsNewMessageForPressWhenStrategyIsNew() throws Exception {
        renderer.render(ctx(TestUpdates.press("nav:main")), View.text("Hi").strategy(RenderStrategy.NEW).build());

        assertThat(client.calls().get(0)).isInstanceOf(SendMessage.class);
    }

    @Test
    void sendsNewMessageWhenThereIsNothingToEdit() throws Exception {
        renderer.render(ctx(TestUpdates.pressWithoutMessage("nav:main")), menu);
        renderer.render(ctx(TestUpdates.text("/start")), View.text("Hi").strategy(RenderStrategy.EDIT).build());

        assertThat(client.calls(SendMessage.class)).hasSize(2);
        assertThat(client.calls(SendMessage.class).get(0).getChatId()).isEqualTo(String.valueOf(USER_ID));
    }

    @Test
    void answersCallbackOfPress() {
        renderer.answerCallback(ctx(TestUpdates.press("nav:main")));

        AnswerCallbackQuery answer = (AnswerCallbackQuery) client.calls().get(0);
        assertThat(answer.getCallbackQueryId()).isEqualTo("callback-1");
        assertThat(answer.getText()).isNull();
    }

    @Test
    void doesNotAnswerCallbackForMessages() {
        renderer.answerCallback(ctx(TestUpdates.text("/start")));

        assertThat(client.calls()).isEmpty();
    }

    @Test
    void skipsUpdateWithoutChat() throws Exception {
        renderer.render(ctx(new Update()), menu);

        assertThat(client.calls()).isEmpty();
    }

    private BotContext ctx(Update update) {
        return contexts.create("simple", update);
    }
}
