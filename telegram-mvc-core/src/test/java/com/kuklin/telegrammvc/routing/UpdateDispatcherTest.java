package com.kuklin.telegrammvc.routing;

import com.kuklin.telegrammvc.annotation.ActionHandler;
import com.kuklin.telegrammvc.annotation.BotController;
import com.kuklin.telegrammvc.annotation.CommandHandler;
import com.kuklin.telegrammvc.context.BotContext;
import com.kuklin.telegrammvc.context.UpdateContextFactory;
import com.kuklin.telegrammvc.render.ViewRenderer;
import com.kuklin.telegrammvc.support.RecordingBotClient;
import com.kuklin.telegrammvc.support.TestUpdates;
import com.kuklin.telegrammvc.view.View;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateDispatcherTest {

    private final RecordingBotClient client = new RecordingBotClient("simple");
    private final MenuController controller = new MenuController();
    private final UpdateDispatcher dispatcher;

    UpdateDispatcherTest() {
        HandlerRegistry registry = new HandlerRegistry(Set.of("simple"));
        registry.register(controller);
        dispatcher = new UpdateDispatcher(new UpdateContextFactory(Map.of("simple", "simple_bot")), registry,
                new ViewRenderer(name -> client));
    }

    @Test
    void answersCommandWithNewMessage() {
        dispatcher.consume("simple", TestUpdates.text("/start"));

        assertThat(client.calls()).singleElement().isInstanceOf(SendMessage.class);
        assertThat(client.calls(SendMessage.class).get(0).getText()).isEqualTo("Choose an action:");
    }

    @Test
    void passesContextToHandler() {
        dispatcher.consume("simple", TestUpdates.text("/echo hello world"));

        assertThat(client.calls(SendMessage.class).get(0).getText()).isEqualTo("hello world");
    }

    @Test
    void editsMessageOnPressAndAnswersCallbackOnce() {
        dispatcher.consume("simple", TestUpdates.press("time:show"));

        assertThat(client.calls()).hasSize(2);
        assertThat(client.calls().get(0)).isInstanceOf(EditMessageText.class);
        assertThat(client.calls().get(1)).isInstanceOf(AnswerCallbackQuery.class);
    }

    @Test
    void answersCallbackOfPressWithoutHandler() {
        dispatcher.consume("simple", TestUpdates.press("stale:button"));

        assertThat(client.calls()).singleElement().isInstanceOf(AnswerCallbackQuery.class);
    }

    @Test
    void answersCallbackWhenHandlerFails() {
        dispatcher.consume("simple", TestUpdates.press("broken"));

        assertThat(client.calls()).singleElement().isInstanceOf(AnswerCallbackQuery.class);
    }

    @Test
    void sendsNothingForVoidHandler() {
        dispatcher.consume("simple", TestUpdates.press("silent"));

        assertThat(controller.silentCalls).isEqualTo(1);
        assertThat(client.calls()).singleElement().isInstanceOf(AnswerCallbackQuery.class);
    }

    @Test
    void ignoresMessagesWithoutHandler() {
        dispatcher.consume("simple", TestUpdates.text("/unknown"));
        dispatcher.consume("simple", TestUpdates.text("just text"));
        dispatcher.consume("simple", TestUpdates.text("/start@other_bot"));

        assertThat(client.calls()).isEmpty();
    }

    @BotController(bot = "simple")
    static class MenuController {

        int silentCalls;

        @CommandHandler("/start")
        public View start() {
            return View.text("Choose an action:").row().button("Show time", "time:show").build();
        }

        @CommandHandler("/echo")
        public View echo(BotContext ctx) {
            return View.text(ctx.args()).build();
        }

        @ActionHandler("time:show")
        public View time() {
            return View.text("Now: 12:00").row().button("Back", "nav:main").build();
        }

        @ActionHandler("broken")
        public View broken() {
            throw new IllegalStateException("handler failed");
        }

        @ActionHandler("silent")
        public void silent() {
            silentCalls++;
        }
    }
}
