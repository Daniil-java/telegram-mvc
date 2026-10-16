package com.kuklin.telegrammvc.transport;

import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Receives every update of every registered bot. Called from the handling pool, never from the polling thread.
 */
@FunctionalInterface
public interface UpdateConsumer {

    void consume(String botName, Update update);
}
