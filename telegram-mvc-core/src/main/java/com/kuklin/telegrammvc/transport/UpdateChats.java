package com.kuklin.telegrammvc.transport;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

/**
 * Finds the chat an update belongs to, so updates of one chat can be handled in order.
 */
final class UpdateChats {

    private UpdateChats() {
    }

    /**
     * @return chat id, or the user id for updates without a chat (inline queries, payments),
     * or {@code null} when the update has neither
     */
    static Long chatId(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getChatId();
        }
        if (update.hasCallbackQuery()) {
            if (update.getCallbackQuery().getMessage() != null) {
                return update.getCallbackQuery().getMessage().getChatId();
            }
            return userId(update.getCallbackQuery().getFrom());
        }
        if (update.hasEditedMessage()) {
            return update.getEditedMessage().getChatId();
        }
        if (update.hasChannelPost()) {
            return update.getChannelPost().getChatId();
        }
        if (update.hasEditedChannelPost()) {
            return update.getEditedChannelPost().getChatId();
        }
        if (update.hasMyChatMember()) {
            return update.getMyChatMember().getChat().getId();
        }
        if (update.hasChatMember()) {
            return update.getChatMember().getChat().getId();
        }
        if (update.hasChatJoinRequest()) {
            return update.getChatJoinRequest().getChat().getId();
        }
        if (update.hasInlineQuery()) {
            return userId(update.getInlineQuery().getFrom());
        }
        if (update.hasChosenInlineQuery()) {
            return userId(update.getChosenInlineQuery().getFrom());
        }
        if (update.hasShippingQuery()) {
            return userId(update.getShippingQuery().getFrom());
        }
        if (update.hasPreCheckoutQuery()) {
            return userId(update.getPreCheckoutQuery().getFrom());
        }
        if (update.hasPollAnswer()) {
            return userId(update.getPollAnswer().getUser());
        }
        return null;
    }

    private static Long userId(User user) {
        return user == null ? null : user.getId();
    }
}
