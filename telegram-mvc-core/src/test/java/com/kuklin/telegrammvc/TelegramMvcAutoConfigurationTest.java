package com.kuklin.telegrammvc;

import com.kuklin.telegrammvc.autoconfigure.TelegramMvcAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class TelegramMvcAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(TelegramMvcAutoConfiguration.class));

    @Test
    void contextStarts() {
        contextRunner.run(context -> assertThat(context).hasSingleBean(TelegramMvcAutoConfiguration.class));
    }
}
