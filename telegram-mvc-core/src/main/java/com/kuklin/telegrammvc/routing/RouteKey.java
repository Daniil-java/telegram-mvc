package com.kuklin.telegrammvc.routing;

import com.kuklin.telegrammvc.context.UpdateType;

/**
 * Key of a route inside one bot: {@code (COMMAND, "/start")} or {@code (ACTION, "nav:main")}.
 */
public record RouteKey(UpdateType type, String value) {

    @Override
    public String toString() {
        return (type == UpdateType.COMMAND ? "command '" : "action '") + value + "'";
    }
}
