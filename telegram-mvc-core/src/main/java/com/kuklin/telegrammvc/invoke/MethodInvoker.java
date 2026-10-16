package com.kuklin.telegrammvc.invoke;

import com.kuklin.telegrammvc.context.BotContext;
import com.kuklin.telegrammvc.view.View;
import org.springframework.util.ReflectionUtils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

/**
 * Calls one handler method. Everything that can be checked is checked once at startup:
 * parameter types, return type, access. Invocation goes through a {@link MethodHandle}
 * bound to the controller bean.
 */
public final class MethodInvoker {

    private final MethodHandle handle;
    private final int parameterCount;
    private final boolean returnsView;

    private MethodInvoker(MethodHandle handle, int parameterCount, boolean returnsView) {
        this.handle = handle;
        this.parameterCount = parameterCount;
        this.returnsView = returnsView;
    }

    /**
     * @throws IllegalStateException if the method has an unsupported parameter or return type
     */
    public static MethodInvoker prepare(Object bean, Method method) {
        String name = describe(method);
        for (Parameter parameter : method.getParameters()) {
            if (parameter.getType() != BotContext.class) {
                throw new IllegalStateException("Handler " + name + " has unsupported parameter '"
                        + parameter.getType().getSimpleName() + " " + parameter.getName()
                        + "'. Supported parameters: BotContext.");
            }
        }
        Class<?> returnType = method.getReturnType();
        if (returnType != View.class && returnType != void.class) {
            throw new IllegalStateException("Handler " + name + " returns unsupported type '"
                    + returnType.getSimpleName() + "'. Supported return types: View, void.");
        }
        ReflectionUtils.makeAccessible(method);
        try {
            MethodHandle handle = MethodHandles.lookup().unreflect(method).bindTo(bean);
            return new MethodInvoker(handle, method.getParameterCount(), returnType == View.class);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Handler " + name + " is not accessible", e);
        }
    }

    /**
     * @return the view returned by the handler, or {@code null} for {@code void} handlers and {@code null} results
     */
    public View invoke(BotContext ctx) throws Exception {
        Object[] args = new Object[parameterCount];
        Arrays.fill(args, ctx);
        Object result;
        try {
            result = handle.invokeWithArguments(args);
        } catch (Exception | Error e) {
            throw e;
        } catch (Throwable t) {
            throw new IllegalStateException(t);
        }
        return returnsView ? (View) result : null;
    }

    /**
     * {@code ClassName#method(ParamType, ...)}, for error messages.
     */
    public static String describe(Method method) {
        StringBuilder sb = new StringBuilder(method.getDeclaringClass().getSimpleName())
                .append('#').append(method.getName()).append('(');
        Class<?>[] types = method.getParameterTypes();
        for (int i = 0; i < types.length; i++) {
            sb.append(i == 0 ? "" : ", ").append(types[i].getSimpleName());
        }
        return sb.append(')').toString();
    }
}
