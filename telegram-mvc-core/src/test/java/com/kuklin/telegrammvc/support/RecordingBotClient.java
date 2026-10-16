package com.kuklin.telegrammvc.support;

import com.kuklin.telegrammvc.transport.BotClient;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Records every call instead of talking to Telegram.
 */
public class RecordingBotClient implements BotClient {

    private final String botName;
    private final List<Object> calls = new ArrayList<>();

    public RecordingBotClient(String botName) {
        this.botName = botName;
    }

    public List<Object> calls() {
        return calls;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> calls(Class<T> type) {
        return calls.stream().filter(type::isInstance).map(c -> (T) c).toList();
    }

    @Override
    public String botName() {
        return botName;
    }

    @Override
    public <T extends Serializable, M extends BotApiMethod<T>> T execute(M method) {
        calls.add(method);
        return null;
    }

    @Override
    public Message execute(SendPhoto method) {
        calls.add(method);
        return null;
    }

    @Override
    public Message execute(SendDocument method) {
        calls.add(method);
        return null;
    }

    @Override
    public Serializable execute(EditMessageMedia method) {
        calls.add(method);
        return null;
    }
}
