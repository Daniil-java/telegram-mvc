package com.kuklin.telegrammvc.examples.simple;

import com.kuklin.telegrammvc.annotation.ActionHandler;
import com.kuklin.telegrammvc.annotation.BotController;
import com.kuklin.telegrammvc.annotation.CommandHandler;
import com.kuklin.telegrammvc.context.BotContext;
import com.kuklin.telegrammvc.view.View;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * {@code /start} sends a menu; every button edits that same message.
 */
@BotController(bot = "simple")
public class MenuController {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    @CommandHandler(value = "/start", description = "Main menu")
    public View start() {
        return menu();
    }

    @ActionHandler("nav:main")
    public View main() {
        return menu();
    }

    @ActionHandler("time:show")
    public View time() {
        return View.text("Now: " + LocalTime.now().format(TIME))
                .row().button("Refresh", "time:show").button("Back", "nav:main")
                .build();
    }

    @ActionHandler("about:show")
    public View about(BotContext ctx) {
        return View.text("Hi, " + ctx.user().firstName() + "! This bot is built with telegram-mvc.")
                .row().urlButton("Source code", "https://github.com/Daniil-java/telegram-mvc")
                .row().button("Back", "nav:main")
                .build();
    }

    private static View menu() {
        return View.text("Choose an action:")
                .row().button("Show time", "time:show").button("About", "about:show")
                .build();
    }
}
