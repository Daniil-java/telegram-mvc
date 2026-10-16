package com.kuklin.telegrammvc.routing;

import com.kuklin.telegrammvc.context.BotContext;
import com.kuklin.telegrammvc.context.UpdateContextFactory;
import com.kuklin.telegrammvc.context.UpdateType;
import com.kuklin.telegrammvc.render.ViewRenderer;
import com.kuklin.telegrammvc.transport.UpdateConsumer;
import com.kuklin.telegrammvc.view.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;

/**
 * Handles an update end to end: context → handler lookup → invocation → rendering.
 * Every button press gets exactly one callback answer, whatever happens in between.
 */
public class UpdateDispatcher implements UpdateConsumer {

    private static final Logger log = LoggerFactory.getLogger(UpdateDispatcher.class);

    private final UpdateContextFactory contextFactory;
    private final HandlerRegistry registry;
    private final ViewRenderer renderer;

    public UpdateDispatcher(UpdateContextFactory contextFactory, HandlerRegistry registry, ViewRenderer renderer) {
        this.contextFactory = contextFactory;
        this.registry = registry;
        this.renderer = renderer;
    }

    @Override
    public void consume(String botName, Update update) {
        BotContext ctx = contextFactory.create(botName, update);
        try {
            dispatch(ctx);
        } catch (Exception e) {
            // replaced by exception resolvers in stage 5
            log.error("Bot '{}': handling {} {} in chat {} failed", botName, ctx.type(), ctx.routeKey(),
                    ctx.chatId(), e);
        } finally {
            if (ctx.type() == UpdateType.ACTION) {
                renderer.answerCallback(ctx);
            }
        }
    }

    private void dispatch(BotContext ctx) throws Exception {
        Optional<HandlerMethod> handler = registry.find(ctx.botName(), ctx.type(), ctx.routeKey());
        if (handler.isEmpty()) {
            log.debug("Bot '{}': no handler for {} {}", ctx.botName(), ctx.type(), ctx.routeKey());
            return;
        }
        View view = handler.get().invoke(ctx);
        if (view != null) {
            renderer.render(ctx, view);
        }
    }
}
