package com.kuklin.telegrammvc.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class whose methods handle updates of a Telegram bot.
 * Handler methods return a View instead of calling the Telegram API directly.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface BotController {

    /**
     * Name of the bot this controller belongs to, as configured under {@code telegram-mvc.bots}.
     */
    String bot();
}
