package com.kuklin.telegrammvc;

import com.kuklin.telegrammvc.autoconfigure.TelegramMvcAutoConfiguration;
import com.kuklin.telegrammvc.autoconfigure.TelegramMvcProperties;
import com.kuklin.telegrammvc.transport.BotSessionFactory;
import com.kuklin.telegrammvc.transport.TelegramBots;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.telegram.telegrambots.meta.generics.BotOptions;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.meta.generics.LongPollingBot;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TelegramMvcAutoConfigurationTest {

    private final FakeSessions sessions = new FakeSessions();

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(TelegramMvcAutoConfiguration.class))
            .withBean(BotSessionFactory.class, () -> sessions);

    @Test
    void startsWithoutBots() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context.getBean(TelegramBots.class).botNames()).isEmpty();
        });
    }

    @Test
    void appliesDefaults() {
        contextRunner
                .withPropertyValues("telegram-mvc.bots.simple.token=123:abc")
                .run(context -> {
                    TelegramMvcProperties properties = context.getBean(TelegramMvcProperties.class);
                    assertThat(properties.bots().get("simple").enabled()).isTrue();
                    assertThat(properties.executor().threads()).isEqualTo(4);
                    assertThat(properties.executor().shutdownTimeout()).isEqualTo(Duration.ofSeconds(10));
                });
    }

    @Test
    void startsPollingForEnabledBots() {
        contextRunner
                .withPropertyValues(
                        "telegram-mvc.bots.first.token=1:a",
                        "telegram-mvc.bots.first.username=first_bot",
                        "telegram-mvc.bots.second.token=2:b")
                .run(context -> {
                    assertThat(sessions.startedUsernames()).containsExactly("first_bot", null);
                    TelegramBots bots = context.getBean(TelegramBots.class);
                    assertThat(bots.botNames()).containsExactly("first", "second");
                    assertThat(bots.client("first").botName()).isEqualTo("first");
                });
    }

    @Test
    void doesNotStartDisabledBot() {
        contextRunner
                .withPropertyValues(
                        "telegram-mvc.bots.on.token=1:a",
                        "telegram-mvc.bots.off.enabled=false")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getBean(TelegramBots.class).botNames()).containsExactly("on");
                    assertThat(sessions.started).hasSize(1);
                });
    }

    @Test
    void failsOnEmptyTokenOfEnabledBot() {
        contextRunner
                .withPropertyValues("telegram-mvc.bots.simple.username=simple_bot")
                .run(context -> assertThat(context).getFailure()
                        .rootCause()
                        .hasMessageContaining("Bot 'simple' is enabled but telegram-mvc.bots.simple.token is empty"));
    }

    @Test
    void failsOnZeroThreads() {
        contextRunner
                .withPropertyValues("telegram-mvc.executor.threads=0")
                .run(context -> assertThat(context).getFailure()
                        .rootCause()
                        .hasMessageContaining("telegram-mvc.executor.threads must be at least 1"));
    }

    @Test
    void stopsPollingOnShutdown() {
        contextRunner
                .withPropertyValues("telegram-mvc.bots.simple.token=1:a")
                .run(context -> assertThat(sessions.started).allMatch(FakeSession::isRunning));
        assertThat(sessions.started).hasSize(1).noneMatch(FakeSession::isRunning);
    }

    @Test
    void doesNotPrintTokens() {
        TelegramMvcProperties.Bot bot = new TelegramMvcProperties.Bot("123:secret", "simple_bot", true);
        assertThat(bot.toString()).doesNotContain("secret");
    }

    private static final class FakeSessions implements BotSessionFactory {

        final List<FakeSession> started = new ArrayList<>();

        @Override
        public BotSession start(LongPollingBot bot) {
            FakeSession session = new FakeSession(bot.getBotUsername());
            started.add(session);
            return session;
        }

        List<String> startedUsernames() {
            return started.stream().map(s -> s.username).toList();
        }
    }

    private static final class FakeSession implements BotSession {

        final String username;
        boolean running = true;

        FakeSession(String username) {
            this.username = username;
        }

        @Override
        public void setOptions(BotOptions options) {
        }

        @Override
        public void setToken(String token) {
        }

        @Override
        public void setCallback(LongPollingBot callback) {
        }

        @Override
        public void start() {
            running = true;
        }

        @Override
        public void stop() {
            running = false;
        }

        @Override
        public boolean isRunning() {
            return running;
        }
    }
}
