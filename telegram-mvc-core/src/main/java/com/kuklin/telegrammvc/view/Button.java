package com.kuklin.telegrammvc.view;

import java.util.Objects;

/**
 * Inline keyboard button: either sends callback data to the bot or opens a URL.
 *
 * @param text         label
 * @param callbackData data sent to the bot on press, {@code null} for a URL button
 * @param url          link to open, {@code null} for a callback button
 */
public record Button(String text, String callbackData, String url) {

    public Button {
        Objects.requireNonNull(text, "button text");
        if ((callbackData == null) == (url == null)) {
            throw new IllegalArgumentException("Button '" + text + "' needs exactly one of callback data or URL");
        }
    }

    public static Button callback(String text, String callbackData) {
        return new Button(text, callbackData, null);
    }

    public static Button url(String text, String url) {
        return new Button(text, null, url);
    }
}
