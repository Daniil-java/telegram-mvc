package com.kuklin.telegrammvc.view;

/**
 * How a {@link View} is delivered.
 */
public enum RenderStrategy {

    /** {@link #EDIT} for a button press, {@link #NEW} for everything else. */
    AUTO,

    /** Send a new message. */
    NEW,

    /** Edit the message with the pressed button; falls back to {@link #NEW} when there is no such message. */
    EDIT
}
