package com.kuklin.telegrammvc.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Handles a command: a message starting with {@code /}, for example {@code /start}.
 * The {@code @botname} suffix and arguments are cut off before matching, so {@code /start@my_bot ref_42}
 * matches {@code @CommandHandler("/start")}.
 * <p>
 * By default the returned view is sent as a new message.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CommandHandler {

    /**
     * Command with the leading slash, for example {@code "/start"}.
     */
    String value();

    /**
     * Description for the bot command menu and {@code /help}.
     */
    String description() default "";

    /**
     * Hidden commands still work but are not listed in the command menu and {@code /help}.
     */
    boolean hidden() default false;
}
