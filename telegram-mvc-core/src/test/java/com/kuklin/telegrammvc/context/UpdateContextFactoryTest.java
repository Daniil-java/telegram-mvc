package com.kuklin.telegrammvc.context;

import com.kuklin.telegrammvc.support.TestUpdates;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;

import static com.kuklin.telegrammvc.support.TestUpdates.CHAT_ID;
import static com.kuklin.telegrammvc.support.TestUpdates.MESSAGE_ID;
import static com.kuklin.telegrammvc.support.TestUpdates.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;

class UpdateContextFactoryTest {

    private final UpdateContextFactory factory = new UpdateContextFactory(Map.of("simple", "simple_bot"));

    @ParameterizedTest(name = "''{0}'' → {1} + ''{2}''")
    @CsvSource(delimiter = '|', textBlock = """
            /start                     | /start | ''
            /q кошки                   | /q     | кошки
            '/q   кошки и собаки  '    | /q     | кошки и собаки
            /start@simple_bot          | /start | ''
            /start@Simple_Bot ref_42   | /start | ref_42
            '/start\nвторая строка'    | /start | вторая строка
            """)
    void parsesCommands(String text, String command, String args) {
        BotContext ctx = factory.create("simple", TestUpdates.text(text.replace("\\n", "\n")));

        assertThat(ctx.type()).isEqualTo(UpdateType.COMMAND);
        assertThat(ctx.routeKey()).isEqualTo(command);
        assertThat(ctx.args()).isEqualTo(args);
        assertThat(ctx.chatId()).isEqualTo(CHAT_ID);
        assertThat(ctx.messageId()).isEqualTo(MESSAGE_ID);
    }

    @Test
    void ignoresCommandForAnotherBot() {
        BotContext ctx = factory.create("simple", TestUpdates.text("/start@other_bot"));

        assertThat(ctx.type()).isEqualTo(UpdateType.OTHER);
        assertThat(ctx.routeKey()).isNull();
    }

    @Test
    void acceptsAnySuffixWhenUsernameIsNotConfigured() {
        UpdateContextFactory withoutUsername = new UpdateContextFactory(Map.of());

        BotContext ctx = withoutUsername.create("simple", TestUpdates.text("/start@whatever_bot"));

        assertThat(ctx.type()).isEqualTo(UpdateType.COMMAND);
        assertThat(ctx.routeKey()).isEqualTo("/start");
    }

    @Test
    void parsesButtonPress() {
        Update update = TestUpdates.press("nav:main");

        BotContext ctx = factory.create("simple", update);

        assertThat(ctx.botName()).isEqualTo("simple");
        assertThat(ctx.type()).isEqualTo(UpdateType.ACTION);
        assertThat(ctx.routeKey()).isEqualTo("nav:main");
        assertThat(ctx.chatId()).isEqualTo(CHAT_ID);
        assertThat(ctx.messageId()).isEqualTo(MESSAGE_ID);
        assertThat(ctx.user()).isEqualTo(new ChatUser(USER_ID, "ann", "Ann", null, "ru"));
        assertThat(ctx.update()).isSameAs(update);
    }

    @Test
    void usesUserAsChatForPressWithoutMessage() {
        BotContext ctx = factory.create("simple", TestUpdates.pressWithoutMessage("nav:main"));

        assertThat(ctx.chatId()).isEqualTo(USER_ID);
        assertThat(ctx.messageId()).isNull();
    }

    @Test
    void parsesTextAndMedia() {
        assertThat(factory.create("simple", TestUpdates.text("привет")).type()).isEqualTo(UpdateType.TEXT);
        assertThat(factory.create("simple", TestUpdates.photo()).type()).isEqualTo(UpdateType.MEDIA);
    }

    @Test
    void treatsUnknownUpdatesAsOther() {
        BotContext ctx = factory.create("simple", new Update());

        assertThat(ctx.type()).isEqualTo(UpdateType.OTHER);
        assertThat(ctx.chatId()).isNull();
    }
}
