package com.kuklin.telegrammvc.view;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ViewTest {

    @Test
    void buildsRowsOfButtons() {
        View view = View.text("Menu")
                .row().button("A", "a").button("B", "b")
                .row().urlButton("Site", "https://example.com")
                .build();

        assertThat(view.text()).isEqualTo("Menu");
        assertThat(view.strategy()).isEqualTo(RenderStrategy.AUTO);
        assertThat(view.keyboard()).containsExactly(
                List.of(Button.callback("A", "a"), Button.callback("B", "b")),
                List.of(Button.url("Site", "https://example.com")));
    }

    @Test
    void startsRowForButtonWithoutRow() {
        View view = View.text("Menu").button("A", "a").build();

        assertThat(view.keyboard()).containsExactly(List.of(Button.callback("A", "a")));
    }

    @Test
    void dropsEmptyRows() {
        View view = View.text("Menu").row().row().button("A", "a").row().build();

        assertThat(view.keyboard()).hasSize(1);
    }

    @Test
    void hasNoKeyboardByDefault() {
        assertThat(View.text("Hi").build().keyboard()).isEmpty();
    }

    @Test
    void isNotChangedByBuilderAfterBuild() {
        View.Builder builder = View.text("Menu").button("A", "a");
        View view = builder.build();

        builder.button("B", "b");

        assertThat(view.keyboard().get(0)).hasSize(1);
        assertThatThrownBy(() -> view.keyboard().get(0).add(Button.callback("C", "c")))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void keepsStrategy() {
        assertThat(View.text("Hi").strategy(RenderStrategy.NEW).build().strategy()).isEqualTo(RenderStrategy.NEW);
    }

    @Test
    void rejectsButtonWithBothDataAndUrl() {
        assertThatThrownBy(() -> new Button("A", "a", "https://example.com"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
