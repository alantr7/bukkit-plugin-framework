package com.github.alantr7.bukkitplugin.commands.registry;

import com.github.alantr7.bukkitplugin.commands.executor.CommandContext;
import com.github.alantr7.bukkitplugin.commands.executor.Evaluator;
import lombok.Getter;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.Map;
import java.util.function.*;

public class Parameter {

    @Getter
    private final String name;

    private final boolean isConstant;

    @Getter
    private final Evaluator<Object> evaluator;

    @Getter
    private final BiFunction<CommandSender, CommandContext, Object> defaultValueProvider;

    @Getter
    private Consumer<CommandContext> missingParameterResponse;

    @Getter
    private BiConsumer<CommandContext, String> evalFailResponse;

    @Getter
    private Map.Entry<Predicate<String>, BiConsumer<CommandContext, String>>[] testsBefore;

    @Getter
    private Map.Entry<Predicate<Object>, BiConsumer<CommandContext, Object>>[] testsAfter;

    @Getter
    private BiFunction<CommandSender, String[], Collection<String>> tabComplete;

    public Parameter(String name, boolean isConstant, Evaluator<Object> evaluator, BiFunction<CommandSender, CommandContext, Object> defaultValueProvider, Consumer<CommandContext> missingParameterResponse, BiConsumer<CommandContext, String> evalFailResponse, Map.Entry<Predicate<String>, BiConsumer<CommandContext, String>>[] testsBefore, Map.Entry<Predicate<Object>, BiConsumer<CommandContext, Object>>[] testsAfter, BiFunction<CommandSender, String[], Collection<String>> tabComplete) {
        this.name = name;
        this.isConstant = isConstant;
        this.evaluator = evaluator;

        this.defaultValueProvider = defaultValueProvider;
        this.missingParameterResponse = missingParameterResponse;
        this.evalFailResponse = evalFailResponse;
        this.testsBefore = testsBefore;
        this.testsAfter = testsAfter;

        this.tabComplete = tabComplete;
    }

    public boolean isVariable() {
        return !isConstant;
    }

    public boolean isConstant() {
        return isConstant;
    }

    public static boolean isParameter(String value) {
        return value.startsWith("{") && value.endsWith("}");
    }

}
