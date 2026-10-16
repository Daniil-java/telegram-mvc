package com.kuklin.telegrammvc.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * What the user will see in reply: text, inline keyboard and how to deliver it.
 * Handlers return a view instead of calling the Telegram API. Immutable.
 *
 * <pre>
 * return View.text("Choose an action:")
 *         .row().button("Show time", "time:show").button("About", "about:show")
 *         .row().urlButton("GitHub", "https://github.com/...")
 *         .build();
 * </pre>
 */
public final class View {

    private final String text;
    private final List<List<Button>> keyboard;
    private final RenderStrategy strategy;

    private View(String text, List<List<Button>> keyboard, RenderStrategy strategy) {
        this.text = text;
        this.keyboard = keyboard;
        this.strategy = strategy;
    }

    public static Builder text(String text) {
        return new Builder(text);
    }

    public String text() {
        return text;
    }

    /**
     * Rows of inline buttons, top to bottom. Empty when the view has no keyboard.
     */
    public List<List<Button>> keyboard() {
        return keyboard;
    }

    public RenderStrategy strategy() {
        return strategy;
    }

    @Override
    public String toString() {
        return "View[text=" + text + ", keyboard=" + keyboard + ", strategy=" + strategy + "]";
    }

    public static final class Builder {

        private final String text;
        private final List<List<Button>> rows = new ArrayList<>();
        private RenderStrategy strategy = RenderStrategy.AUTO;

        private Builder(String text) {
            this.text = Objects.requireNonNull(text, "view text");
        }

        /**
         * Starts a new row of buttons. A button added before the first {@code row()} starts one too.
         */
        public Builder row() {
            if (rows.isEmpty() || !rows.get(rows.size() - 1).isEmpty()) {
                rows.add(new ArrayList<>());
            }
            return this;
        }

        /**
         * Adds a button to the current row; pressing it sends {@code callbackData} to the bot.
         */
        public Builder button(String text, String callbackData) {
            return add(Button.callback(text, callbackData));
        }

        /**
         * Adds a button to the current row that opens {@code url}.
         */
        public Builder urlButton(String text, String url) {
            return add(Button.url(text, url));
        }

        public Builder strategy(RenderStrategy strategy) {
            this.strategy = Objects.requireNonNull(strategy, "strategy");
            return this;
        }

        public View build() {
            List<List<Button>> keyboard = rows.stream()
                    .filter(row -> !row.isEmpty())
                    .map(List::copyOf)
                    .toList();
            return new View(text, keyboard, strategy);
        }

        private Builder add(Button button) {
            if (rows.isEmpty()) {
                rows.add(new ArrayList<>());
            }
            rows.get(rows.size() - 1).add(button);
            return this;
        }
    }
}
