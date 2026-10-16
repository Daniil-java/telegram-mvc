package com.kuklin.telegrammvc.routing;

import com.kuklin.telegrammvc.annotation.ActionHandler;
import com.kuklin.telegrammvc.annotation.BotController;
import com.kuklin.telegrammvc.annotation.CommandHandler;
import com.kuklin.telegrammvc.context.UpdateType;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Routing table of every bot: {@code bot name → (type, key) → handler method}.
 * Filled once at startup; any mistake in controllers stops the application with a clear message.
 */
public class HandlerRegistry {

    private final Set<String> configuredBots;
    private final Map<String, Map<RouteKey, HandlerMethod>> routes = new HashMap<>();

    /**
     * @param configuredBots names under {@code telegram-mvc.bots}, including disabled bots
     */
    public HandlerRegistry(Set<String> configuredBots) {
        this.configuredBots = Set.copyOf(configuredBots);
    }

    /**
     * Registers every {@link CommandHandler} and {@link ActionHandler} method of a {@link BotController} bean.
     *
     * @throws IllegalStateException on an unknown bot, an invalid route or a duplicate route
     */
    public void register(Object controller) {
        Class<?> type = AopUtils.getTargetClass(controller);
        BotController annotation = AnnotatedElementUtils.findMergedAnnotation(type, BotController.class);
        if (annotation == null) {
            throw new IllegalArgumentException(type.getName() + " is not annotated with @BotController");
        }
        String bot = annotation.bot();
        if (!configuredBots.contains(bot)) {
            throw new IllegalStateException("@BotController " + type.getSimpleName() + " refers to bot '" + bot
                    + "', but there is no telegram-mvc.bots." + bot + " in the configuration. Configured bots: "
                    + configuredBots);
        }

        MethodIntrospector.selectMethods(type, (MethodIntrospector.MetadataLookup<CommandHandler>) method ->
                        AnnotatedElementUtils.findMergedAnnotation(method, CommandHandler.class))
                .forEach((method, command) -> add(bot, new RouteKey(UpdateType.COMMAND, command(method, command)),
                        controller, method));

        MethodIntrospector.selectMethods(type, (MethodIntrospector.MetadataLookup<ActionHandler>) method ->
                        AnnotatedElementUtils.findMergedAnnotation(method, ActionHandler.class))
                .forEach((method, action) -> add(bot, new RouteKey(UpdateType.ACTION, action(method, action)),
                        controller, method));
    }

    public Optional<HandlerMethod> find(String bot, UpdateType type, String key) {
        if (key == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(routes.getOrDefault(bot, Map.of()).get(new RouteKey(type, key)));
    }

    /**
     * Routes of one bot, for diagnostics and the command menu.
     */
    public Map<RouteKey, HandlerMethod> routes(String bot) {
        return Map.copyOf(routes.getOrDefault(bot, Map.of()));
    }

    private void add(String bot, RouteKey key, Object controller, Method method) {
        Method invocable = AopUtils.selectInvocableMethod(method, controller.getClass());
        HandlerMethod handler = new HandlerMethod(controller, invocable);
        HandlerMethod existing = routes.computeIfAbsent(bot, b -> new HashMap<>()).putIfAbsent(key, handler);
        if (existing != null) {
            throw new IllegalStateException("Duplicate " + key + " in bot '" + bot + "': " + existing
                    + " and " + handler);
        }
    }

    private static String command(Method method, CommandHandler annotation) {
        String value = annotation.value();
        if (!isCommand(value)) {
            throw new IllegalStateException("@CommandHandler(\"" + value + "\") on " + describe(method)
                    + ": a command is '/' followed by letters, digits or '_', for example \"/start\"");
        }
        return value;
    }

    /**
     * {@code /} followed by at least one Latin letter, digit or {@code _}: the characters Telegram allows in commands.
     */
    static boolean isCommand(String value) {
        if (value.length() < 2 || value.charAt(0) != '/') {
            return false;
        }
        for (int i = 1; i < value.length(); i++) {
            char c = value.charAt(i);
            boolean letter = (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
            boolean digit = c >= '0' && c <= '9';
            if (!letter && !digit && c != '_') {
                return false;
            }
        }
        return true;
    }

    private static String action(Method method, ActionHandler annotation) {
        String value = annotation.value();
        if (value.isBlank()) {
            throw new IllegalStateException("@ActionHandler on " + describe(method) + " has empty callback data");
        }
        return value;
    }

    private static String describe(Method method) {
        return method.getDeclaringClass().getSimpleName() + "#" + method.getName();
    }
}
