package com.kuklin.telegrammvc.autoconfigure;

import com.kuklin.telegrammvc.annotation.BotController;
import com.kuklin.telegrammvc.context.UpdateContextFactory;
import com.kuklin.telegrammvc.render.ViewRenderer;
import com.kuklin.telegrammvc.routing.HandlerRegistry;
import com.kuklin.telegrammvc.routing.UpdateDispatcher;
import com.kuklin.telegrammvc.transport.BotClients;
import com.kuklin.telegrammvc.transport.BotSessionFactory;
import com.kuklin.telegrammvc.transport.TelegramBots;
import com.kuklin.telegrammvc.transport.UpdateConsumer;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.function.SingletonSupplier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Entry point of the library: transport, routing and rendering beans.
 */
@AutoConfiguration
@EnableConfigurationProperties(TelegramMvcProperties.class)
public class TelegramMvcAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public BotSessionFactory botSessionFactory() {
        return BotSessionFactory.telegramBotsApi();
    }

    /**
     * The consumer is looked up on the first update: the dispatcher needs the bot clients,
     * so taking it in the constructor would be a dependency cycle.
     */
    @Bean
    public TelegramBots telegramBots(TelegramMvcProperties properties, ObjectProvider<UpdateConsumer> updateConsumer,
                                     BotSessionFactory botSessionFactory) {
        Supplier<UpdateConsumer> consumer = SingletonSupplier.of(updateConsumer::getObject);
        return new TelegramBots(properties, (botName, update) -> consumer.get().consume(botName, update),
                botSessionFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public UpdateContextFactory updateContextFactory(TelegramMvcProperties properties) {
        Map<String, String> usernames = new HashMap<>();
        properties.bots().forEach((name, bot) -> usernames.put(name, bot.username()));
        return new UpdateContextFactory(usernames);
    }

    @Bean
    public HandlerRegistry handlerRegistry(TelegramMvcProperties properties) {
        return new HandlerRegistry(properties.bots().keySet());
    }

    /**
     * Fills the registry after all beans are created and before polling starts.
     */
    @Bean
    public SmartInitializingSingleton botControllerScanner(HandlerRegistry handlerRegistry,
                                                           ListableBeanFactory beanFactory) {
        return () -> beanFactory.getBeansWithAnnotation(BotController.class).values()
                .forEach(handlerRegistry::register);
    }

    @Bean
    @ConditionalOnMissingBean
    public ViewRenderer viewRenderer(BotClients botClients) {
        return new ViewRenderer(botClients);
    }

    /**
     * An application can replace routing entirely by declaring its own {@link UpdateConsumer}.
     */
    @Bean
    @ConditionalOnMissingBean(UpdateConsumer.class)
    public UpdateDispatcher updateDispatcher(UpdateContextFactory updateContextFactory,
                                             HandlerRegistry handlerRegistry, ViewRenderer viewRenderer) {
        return new UpdateDispatcher(updateContextFactory, handlerRegistry, viewRenderer);
    }
}
