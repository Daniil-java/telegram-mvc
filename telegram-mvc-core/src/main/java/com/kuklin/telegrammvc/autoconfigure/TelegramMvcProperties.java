package com.kuklin.telegrammvc.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Configuration under the {@code telegram-mvc} prefix.
 *
 * <pre>
 * telegram-mvc:
 *   bots:
 *     simple:
 *       token: ${SIMPLE_BOT_TOKEN}
 *       username: simple_bot
 *   executor:
 *     threads: 4
 * </pre>
 *
 * @param bots     bots by name; the name is what {@code @BotController(bot = ...)} refers to
 * @param executor pool that runs update handling
 */
@ConfigurationProperties("telegram-mvc")
public record TelegramMvcProperties(Map<String, Bot> bots, @DefaultValue Executor executor) {

    public TelegramMvcProperties {
        bots = bots == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(bots));
        bots.forEach((name, bot) -> {
            if (bot.enabled() && !StringUtils.hasText(bot.token())) {
                throw new IllegalStateException("Bot '" + name + "' is enabled but telegram-mvc.bots."
                        + name + ".token is empty. Set the token or add 'enabled: false'.");
            }
        });
    }

    /**
     * @param token    token from @BotFather
     * @param username bot username without {@code @}
     * @param enabled  a disabled bot is not registered and its token is not required
     */
    public record Bot(String token, String username, @DefaultValue("true") boolean enabled) {

        @Override
        public String toString() {
            return "Bot[username=" + username + ", enabled=" + enabled + ", token=***]";
        }
    }

    /**
     * @param threads          number of threads handling updates; updates of one chat are still handled in order
     * @param shutdownTimeout  how long to wait for running handlers on shutdown
     */
    public record Executor(@DefaultValue("4") int threads, @DefaultValue("10s") Duration shutdownTimeout) {

        public Executor {
            if (threads < 1) {
                throw new IllegalStateException("telegram-mvc.executor.threads must be at least 1, got " + threads);
            }
        }
    }
}
