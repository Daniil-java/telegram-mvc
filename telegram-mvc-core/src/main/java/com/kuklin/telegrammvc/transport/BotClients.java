package com.kuklin.telegrammvc.transport;

/**
 * Finds the {@link BotClient} of a bot by name.
 */
@FunctionalInterface
public interface BotClients {

    /**
     * @throws IllegalArgumentException if there is no enabled bot with this name
     */
    BotClient client(String botName);
}
