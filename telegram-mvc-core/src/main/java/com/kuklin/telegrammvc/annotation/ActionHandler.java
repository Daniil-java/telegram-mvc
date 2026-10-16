package com.kuklin.telegrammvc.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Handles a press of an inline button whose callback data equals {@link #value()}.
 * <p>
 * By default the returned view edits the message with the button.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ActionHandler {

    /**
     * Callback data of the button, for example {@code "nav:main"}.
     */
    String value();
}
