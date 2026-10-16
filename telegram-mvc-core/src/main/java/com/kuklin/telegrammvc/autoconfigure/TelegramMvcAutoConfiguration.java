package com.kuklin.telegrammvc.autoconfigure;

import com.kuklin.telegrammvc.transport.BotSessionFactory;
import com.kuklin.telegrammvc.transport.TelegramBots;
import com.kuklin.telegrammvc.transport.UpdateConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Entry point of the library. Registry, dispatcher and renderer beans will be declared here.
 */
@AutoConfiguration
@EnableConfigurationProperties(TelegramMvcProperties.class)
public class TelegramMvcAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(TelegramMvcAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public UpdateConsumer updateConsumer() {
        return (botName, update) -> log.debug("Bot '{}' received update {}", botName, update.getUpdateId());
    }

    @Bean
    @ConditionalOnMissingBean
    public BotSessionFactory botSessionFactory() {
        return BotSessionFactory.telegramBotsApi();
    }

    @Bean
    public TelegramBots telegramBots(TelegramMvcProperties properties, UpdateConsumer updateConsumer,
                                     BotSessionFactory botSessionFactory) {
        return new TelegramBots(properties, updateConsumer, botSessionFactory);
    }
}
