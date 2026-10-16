package com.kuklin.telegrammvc.context;

/**
 * What kind of user action an update is.
 */
public enum UpdateType {

    /** Message starting with {@code /}. */
    COMMAND,

    /** Press of an inline button. */
    ACTION,

    /** Text message without a command. */
    TEXT,

    /** Photo, document, voice and other media messages. */
    MEDIA,

    /** Everything else: edited messages, chat member changes, commands addressed to another bot. */
    OTHER
}
