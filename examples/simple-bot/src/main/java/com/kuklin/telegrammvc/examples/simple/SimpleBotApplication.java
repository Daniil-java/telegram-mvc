package com.kuklin.telegrammvc.examples.simple;

import com.kuklin.telegrammvc.transport.UpdateConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.telegram.telegrambots.meta.api.objects.Update;

@SpringBootApplication
public class SimpleBotApplication {

    private static final Logger log = LoggerFactory.getLogger(SimpleBotApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SimpleBotApplication.class, args);
    }

    /**
     * Temporary: logs every update until controllers are routed (stage 2).
     */
    @Bean
    UpdateConsumer loggingUpdateConsumer() {
        return (botName, update) -> log.info("[{}] {}", botName, describe(update));
    }

    private static String describe(Update update) {
        if (update.hasMessage()) {
            var message = update.getMessage();
            String content = message.hasText() ? "text '" + message.getText() + "'"
                    : message.hasPhoto() ? "photo"
                    : message.hasDocument() ? "document"
                    : message.hasVoice() ? "voice"
                    : "other content";
            return "message from chat " + message.getChatId() + ": " + content;
        }
        if (update.hasCallbackQuery()) {
            return "button press '" + update.getCallbackQuery().getData() + "' from user "
                    + update.getCallbackQuery().getFrom().getId();
        }
        return "other update " + update.getUpdateId();
    }
}
