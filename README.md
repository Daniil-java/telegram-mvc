# telegram-mvc

[![Build](https://github.com/Daniil-java/telegram-mvc/actions/workflows/build.yml/badge.svg)](https://github.com/Daniil-java/telegram-mvc/actions/workflows/build.yml)

Annotation-driven Telegram bots for Spring Boot, in the style of Spring MVC.

> Work in progress. Commands and buttons work; handler parameters, media, input and error
> handling are coming — see the roadmap.

```java
@BotController(bot = "simple")
public class MenuController {

    @CommandHandler("/start")
    @ActionHandler("nav:main")
    public View start() {          // a command sends a new message
        return View.text("Choose an action:")
                .row().button("Show time", "time:show")
                .build();
    }

    @ActionHandler("time:show")
    public View time() {
        return View.text("Now: " + LocalTime.now())
                .row().button("Back", "nav:main")
                .build();   // a button press edits the same message
    }
}
```

Handlers return a `View` instead of calling the Telegram API. The library routes updates,
injects arguments, renders the view (send or edit, with fallbacks for Telegram's edit errors)
and turns exceptions into user-facing messages.

Configure the bot in `application.yaml`:

```yaml
telegram-mvc:
  bots:
    simple:                       # the name used in @BotController(bot = "simple")
      token: ${SIMPLE_BOT_TOKEN}
      username: ${SIMPLE_BOT_USERNAME}
```

Mistakes in controllers stop the application at startup: two handlers for the same command,
a controller for a bot that is not configured, an unsupported parameter or return type.

## Modules

| Module | Purpose |
|---|---|
| `telegram-mvc-core` | The library: annotations, routing, rendering, Spring Boot auto-configuration |
| `examples/simple-bot` | Minimal bot built on the library |

## Roadmap

See [docs/ROADMAP.md](docs/ROADMAP.md). Design notes (in Russian): [docs/design.md](docs/design.md).

## Build

```bash
./mvnw verify
```

Requires Java 17. Built on [TelegramBots](https://github.com/rubenlagus/TelegramBots) 6.9.

## Running the example

Create a bot with [@BotFather](https://t.me/BotFather), then:

```bash
SIMPLE_BOT_TOKEN=... SIMPLE_BOT_USERNAME=... ./mvnw -pl examples/simple-bot spring-boot:run
```

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## License

[Apache License 2.0](LICENSE)
