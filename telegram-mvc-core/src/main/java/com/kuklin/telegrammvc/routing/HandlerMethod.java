package com.kuklin.telegrammvc.routing;

import com.kuklin.telegrammvc.context.BotContext;
import com.kuklin.telegrammvc.invoke.MethodInvoker;
import com.kuklin.telegrammvc.view.View;

import java.lang.reflect.Method;

/**
 * A controller method registered for a route, ready to be called.
 */
public final class HandlerMethod {

    private final Method method;
    private final MethodInvoker invoker;

    HandlerMethod(Object bean, Method method) {
        this.method = method;
        this.invoker = MethodInvoker.prepare(bean, method);
    }

    public Method method() {
        return method;
    }

    public View invoke(BotContext ctx) throws Exception {
        return invoker.invoke(ctx);
    }

    @Override
    public String toString() {
        return MethodInvoker.describe(method);
    }
}
