package com.kuklin.telegrammvc.routing;

import com.kuklin.telegrammvc.annotation.ActionHandler;
import com.kuklin.telegrammvc.annotation.BotController;
import com.kuklin.telegrammvc.annotation.CommandHandler;
import com.kuklin.telegrammvc.autoconfigure.TelegramMvcAutoConfiguration;
import com.kuklin.telegrammvc.context.BotContext;
import com.kuklin.telegrammvc.context.UpdateType;
import com.kuklin.telegrammvc.transport.BotSessionFactory;
import com.kuklin.telegrammvc.view.View;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HandlerRegistryTest {

    private final HandlerRegistry registry = new HandlerRegistry(Set.of("simple", "other"));

    @Test
    void findsCommandsAndActions() {
        registry.register(new MenuController());

        assertThat(registry.find("simple", UpdateType.COMMAND, "/start")).get()
                .hasToString("MenuController#start()");
        assertThat(registry.find("simple", UpdateType.ACTION, "nav:main")).get()
                .hasToString("MenuController#main(BotContext)");
        assertThat(registry.routes("simple")).hasSize(2);
    }

    @Test
    void doesNotMixTypesBotsAndUnknownKeys() {
        registry.register(new MenuController());

        assertThat(registry.find("simple", UpdateType.ACTION, "/start")).isEmpty();
        assertThat(registry.find("other", UpdateType.COMMAND, "/start")).isEmpty();
        assertThat(registry.find("simple", UpdateType.COMMAND, "/unknown")).isEmpty();
        assertThat(registry.find("simple", UpdateType.TEXT, null)).isEmpty();
    }

    @Test
    void allowsSameRouteInDifferentBots() {
        registry.register(new MenuController());
        registry.register(new OtherBotController());

        assertThat(registry.find("other", UpdateType.COMMAND, "/start")).isPresent();
    }

    @Test
    void failsOnDuplicateRouteNamingBothMethods() {
        registry.register(new MenuController());

        assertThatThrownBy(() -> registry.register(new DuplicateController()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Duplicate command '/start' in bot 'simple': MenuController#start() and "
                        + "DuplicateController#alsoStart()");
    }

    @Test
    void failsOnUnknownBot() {
        assertThatThrownBy(() -> registry.register(new UnknownBotController()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("refers to bot 'missing'")
                .hasMessageContaining("no telegram-mvc.bots.missing");
    }

    @Test
    void failsOnInvalidCommand() {
        assertThatThrownBy(() -> registry.register(new InvalidCommandController()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("@CommandHandler(\"start\")");
    }

    @ParameterizedTest
    @ValueSource(strings = {"/start", "/q", "/my_command2", "/START"})
    void acceptsValidCommands(String command) {
        assertThat(HandlerRegistry.isCommand(command)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/", "start", "//start", "/start me", "/старт", "/start-now", "/start@bot"})
    void rejectsInvalidCommands(String command) {
        assertThat(HandlerRegistry.isCommand(command)).isFalse();
    }

    @Test
    void failsOnUnsupportedParameter() {
        assertThatThrownBy(() -> registry.register(new UnsupportedParameterController()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("UnsupportedParameterController#start(Update)")
                .hasMessageContaining("unsupported parameter 'Update");
    }

    @Test
    void failsOnUnsupportedReturnType() {
        assertThatThrownBy(() -> registry.register(new UnsupportedReturnController()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("UnsupportedReturnController#start()")
                .hasMessageContaining("returns unsupported type 'Integer'");
    }

    @Test
    void registersControllersFromApplicationContext() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(TelegramMvcAutoConfiguration.class))
                .withBean(BotSessionFactory.class, () -> bot -> null)
                .withPropertyValues("telegram-mvc.bots.simple.enabled=false")
                .withBean(MenuController.class)
                .run(context -> assertThat(context.getBean(HandlerRegistry.class)
                        .find("simple", UpdateType.COMMAND, "/start")).isPresent());
    }

    @Test
    void stopsApplicationOnControllerMistake() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(TelegramMvcAutoConfiguration.class))
                .withBean(BotSessionFactory.class, () -> bot -> null)
                .withBean(UnknownBotController.class)
                .run(context -> assertThat(context).getFailure()
                        .hasMessageContaining("refers to bot 'missing'"));
    }

    @BotController(bot = "simple")
    static class MenuController {

        @CommandHandler("/start")
        public View start() {
            return View.text("Menu").build();
        }

        @ActionHandler("nav:main")
        public View main(BotContext ctx) {
            return View.text("Menu").build();
        }

        public View notAHandler() {
            return View.text("never routed").build();
        }
    }

    @BotController(bot = "other")
    static class OtherBotController {

        @CommandHandler("/start")
        public View start() {
            return View.text("Other").build();
        }
    }

    @BotController(bot = "simple")
    static class DuplicateController {

        @CommandHandler("/start")
        public View alsoStart() {
            return View.text("Duplicate").build();
        }
    }

    @BotController(bot = "missing")
    static class UnknownBotController {
    }

    @BotController(bot = "simple")
    static class InvalidCommandController {

        @CommandHandler("start")
        public View start() {
            return View.text("No slash").build();
        }
    }

    @BotController(bot = "simple")
    static class UnsupportedParameterController {

        @CommandHandler("/start")
        public View start(Update update) {
            return View.text("Raw update").build();
        }
    }

    @BotController(bot = "simple")
    static class UnsupportedReturnController {

        @CommandHandler("/start")
        public Integer start() {
            return 42;
        }
    }
}
