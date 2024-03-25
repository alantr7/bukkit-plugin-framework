package com.github.alantr7.bukkitplugin.commands.factory;

import com.github.alantr7.bukkitplugin.commands.executor.CommandContext;
import com.github.alantr7.bukkitplugin.commands.executor.Evaluator;
import com.github.alantr7.bukkitplugin.commands.registry.Parameter;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class ParameterBuilder<T> {

    final String name;

    Evaluator<T> evaluator;

    BiConsumer<CommandContext, String> ifEvalFail;

    Function<CommandContext, T> defaultValue;

    Class<T> type;

    Consumer<CommandContext> ifMissingAction;

    List<Map.Entry<Predicate<String>, BiConsumer<CommandContext, String>>> testsBefore = new LinkedList<>();

    List<Map.Entry<Predicate<T>, BiConsumer<CommandContext, T>>> testsAfter = new LinkedList<>();

    Function<String[], Collection<String>> tabComplete;

    public ParameterBuilder(String name) {
        this(name, null);
    }

    public ParameterBuilder(String name, Class<T> type) {
        this.name = name;
        this.type = type;
    }

    public ParameterBuilder<T> evaluator(Evaluator<T> evaluator) {
        this.evaluator = evaluator;
        return this;
    }

    public ParameterBuilder<T> evaluator(Evaluator<T> evaluator, BiConsumer<CommandContext, String> failHandler) {
        this.evaluator = evaluator;
        this.ifEvalFail = failHandler;
        return this;
    }

    public ParameterBuilder<T> defaultValue(Function<CommandContext, T> value) {
        this.defaultValue = value;
        return this;
    }

    public ParameterBuilder<T> tabComplete(String... elements) {
        return tabComplete(args -> Arrays.asList(elements));
    }

    public ParameterBuilder<T> tabComplete(Function<String[], Collection<String>> elements) {
        this.tabComplete = elements;
        return this;
    }

    public ParameterBuilder<T> ifNotProvided(Consumer<CommandContext> message) {
        this.ifMissingAction = message;
        return this;
    }

    public ParameterBuilder<T> assertBefore(Predicate<String> test, BiConsumer<CommandContext, String> context) {
        testsBefore.add(new AbstractMap.SimpleEntry<>(test, context));
        return this;
    }

    public ParameterBuilder<T> assertAfter(Predicate<T> test, BiConsumer<CommandContext, T> context) {
        testsAfter.add(new AbstractMap.SimpleEntry<>(test, context));
        return this;
    }

    Parameter build() {
        boolean isVariable = name.startsWith("{") && name.endsWith("}");
        var literal = isVariable ? name.replace("{", "").replace("}", "") : name;

        return new Parameter(literal, !isVariable, (Evaluator<Object>) evaluator, (Function<CommandContext, Object>) defaultValue, ifMissingAction, ifEvalFail, testsBefore.toArray(Map.Entry[]::new), testsAfter.toArray(Map.Entry[]::new), tabComplete);
    }

    public static Consumer<CommandContext> respond(String message) {
        return ctx -> ctx.getExecutor().sendMessage(message);
    }

}
