package com.kuklin.telegrammvc.transport;

import com.kuklin.telegrammvc.autoconfigure.TelegramMvcProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;

/**
 * Enabled bots from the configuration: their clients, polling sessions and the shared handling pool.
 * Polling starts after the application context is ready and stops first on shutdown.
 */
public class TelegramBots implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(TelegramBots.class);

    private final BotSessionFactory sessionFactory;
    private final ChatOrderedExecutor executor;
    private final Duration shutdownTimeout;
    private final Map<String, PollingBot> bots = new LinkedHashMap<>();
    private final Map<String, BotClient> clients = new LinkedHashMap<>();
    private final List<BotSession> sessions = new ArrayList<>();
    private volatile boolean running;

    public TelegramBots(TelegramMvcProperties properties, UpdateConsumer consumer, BotSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        TelegramMvcProperties.Executor executorProperties = properties.executor();
        this.executor = new ChatOrderedExecutor(Executors.newFixedThreadPool(
                executorProperties.threads(), new CustomizableThreadFactory("telegram-mvc-")));
        this.shutdownTimeout = executorProperties.shutdownTimeout();
        properties.bots().forEach((name, bot) -> {
            if (!bot.enabled()) {
                log.info("Bot '{}' is disabled", name);
                return;
            }
            PollingBot pollingBot = new PollingBot(name, bot.token(), bot.username(), executor, consumer);
            bots.put(name, pollingBot);
            clients.put(name, new DefaultBotClient(name, pollingBot));
        });
    }

    /**
     * Names of the enabled bots.
     */
    public Set<String> botNames() {
        return Collections.unmodifiableSet(clients.keySet());
    }

    /**
     * @throws IllegalArgumentException if there is no enabled bot with this name
     */
    public BotClient client(String botName) {
        BotClient client = clients.get(botName);
        if (client == null) {
            throw new IllegalArgumentException("No enabled bot '" + botName + "'. Enabled bots: " + clients.keySet());
        }
        return client;
    }

    @Override
    public synchronized void start() {
        for (PollingBot bot : bots.values()) {
            try {
                sessions.add(sessionFactory.start(bot));
                log.info("Bot '{}' started long polling", bot.botName());
            } catch (TelegramApiException e) {
                stopSessions();
                throw new IllegalStateException("Bot '" + bot.botName() + "' failed to start: " + e.getMessage(), e);
            }
        }
        running = true;
    }

    @Override
    public synchronized void stop() {
        stopSessions();
        executor.shutdown(shutdownTimeout);
        running = false;
        log.info("Telegram bots stopped");
    }

    private void stopSessions() {
        for (BotSession session : sessions) {
            if (session.isRunning()) {
                session.stop();
            }
        }
        sessions.clear();
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
