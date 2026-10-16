package com.kuklin.telegrammvc.render;

import com.kuklin.telegrammvc.context.BotContext;
import com.kuklin.telegrammvc.context.UpdateType;
import com.kuklin.telegrammvc.transport.BotClient;
import com.kuklin.telegrammvc.transport.BotClients;
import com.kuklin.telegrammvc.view.Button;
import com.kuklin.telegrammvc.view.RenderStrategy;
import com.kuklin.telegrammvc.view.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

/**
 * Turns a {@link View} into Telegram API calls.
 */
public class ViewRenderer {

    private static final Logger log = LoggerFactory.getLogger(ViewRenderer.class);

    private final BotClients clients;

    public ViewRenderer(BotClients clients) {
        this.clients = clients;
    }

    /**
     * Sends or edits a message according to the view's strategy.
     */
    public void render(BotContext ctx, View view) throws TelegramApiException {
        if (ctx.chatId() == null) {
            log.warn("Bot '{}': cannot render a view for an update without a chat: {}", ctx.botName(), view);
            return;
        }
        BotClient client = clients.client(ctx.botName());
        if (resolve(ctx, view.strategy()) == RenderStrategy.EDIT) {
            client.execute(edit(ctx, view));
        } else {
            client.execute(send(ctx, view));
        }
    }

    /**
     * Answers the callback query of a button press so the button stops showing a loading indicator.
     * Telegram expects exactly one answer per press; the dispatcher calls this once for every action.
     */
    public void answerCallback(BotContext ctx) {
        if (ctx.type() != UpdateType.ACTION) {
            return;
        }
        AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                .callbackQueryId(ctx.update().getCallbackQuery().getId())
                .build();
        try {
            clients.client(ctx.botName()).execute(answer);
        } catch (TelegramApiException e) {
            log.warn("Bot '{}': failed to answer callback: {}", ctx.botName(), e.getMessage());
        }
    }

    static RenderStrategy resolve(BotContext ctx, RenderStrategy strategy) {
        RenderStrategy resolved = strategy == RenderStrategy.AUTO
                ? (ctx.type() == UpdateType.ACTION ? RenderStrategy.EDIT : RenderStrategy.NEW)
                : strategy;
        if (resolved == RenderStrategy.EDIT && (ctx.type() != UpdateType.ACTION || ctx.messageId() == null)) {
            // nothing to edit: a command, or a button under an inline message
            return RenderStrategy.NEW;
        }
        return resolved;
    }

    private static SendMessage send(BotContext ctx, View view) {
        SendMessage message = SendMessage.builder()
                .chatId(ctx.chatId())
                .text(view.text())
                .build();
        if (!view.keyboard().isEmpty()) {
            message.setReplyMarkup(keyboard(view));
        }
        return message;
    }

    private static EditMessageText edit(BotContext ctx, View view) {
        // always set the markup: an empty keyboard removes the buttons of the edited message
        return EditMessageText.builder()
                .chatId(ctx.chatId())
                .messageId(ctx.messageId())
                .text(view.text())
                .replyMarkup(keyboard(view))
                .build();
    }

    private static InlineKeyboardMarkup keyboard(View view) {
        List<List<InlineKeyboardButton>> rows = view.keyboard().stream()
                .map(row -> row.stream().map(ViewRenderer::button).toList())
                .toList();
        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }

    private static InlineKeyboardButton button(Button button) {
        return InlineKeyboardButton.builder()
                .text(button.text())
                .callbackData(button.callbackData())
                .url(button.url())
                .build();
    }
}
