package com.github.alantr7.bukkitplugin.annotations.processor.processing.executable;

import com.github.alantr7.bukkitplugin.annotations.processor.meta.MethodMeta;
import com.github.alantr7.bukkitplugin.annotations.processor.meta.ParameterMeta;
import com.github.alantr7.bukkitplugin.annotations.processor.meta.Type;
import lombok.Getter;

import java.util.Arrays;

public class ArgumentsBuilder {

    @Getter
    private final MethodMeta method;

    @Getter
    private final Object container;

    @Getter
    private final Object[] arguments;

    public ArgumentsBuilder(MethodMeta method, Object container) {
        this.method = method;
        this.container = container;
        this.arguments = new Object[method.getParameters().length];
        Arrays.fill(arguments, ProvidedValue.skip());
    }

    public void setArgument(int index, Object value) {
        arguments[index] = value;
    }

    public void setArgument(Class<?> parameterType, Object value) {
        ParameterMeta[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            var parameter = parameters[i];
            if (parameter.getType().getQualifiedName().equals(parameterType.getName())) {
                arguments[i] = value;
            }
        }
    }

    public boolean isArgumentProvided(int index) {
        return arguments[index] != ProvidedValue.skip();
    }

    public boolean isComplete() {
        for (var argument : arguments)
            if (argument == ProvidedValue.skip())
                return false;
        return true;
    }

    public Type getParameterType(int index) {
        return method.getParameters()[index].getType();
    }

}
