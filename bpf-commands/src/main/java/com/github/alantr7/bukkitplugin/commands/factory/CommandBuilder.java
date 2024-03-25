package com.github.alantr7.bukkitplugin.commands.factory;

import com.github.alantr7.bukkitplugin.commands.executor.CommandContext;
import com.github.alantr7.bukkitplugin.commands.executor.Evaluator;
import com.github.alantr7.bukkitplugin.commands.executor.ExecutorType;
import com.github.alantr7.bukkitplugin.commands.registry.Command;
import com.github.alantr7.bukkitplugin.commands.registry.Parameter;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class CommandBuilder {

    private final String command;

    private int matches = -1;

    private ExecutorType[] executors = { ExecutorType.ALL };

    private String permission;

    private final List<ParameterBuilder<Object>> parameters = new LinkedList<>();

    private Function<CommandContext, Object> handler;

    private CommandBuilder(String command) {
        this.command = command;
    }

    public CommandBuilder requireMatches(int count) {
        this.matches = count;
        return this;
    }

    public CommandBuilder forExecutors(ExecutorType... executors) {
        this.executors = executors;
        return this;
    }

    public CommandBuilder permission(String permission) {
        this.permission = permission;
        return this;
    }

    public CommandBuilder permission(String permission, String errorMessage) {
        this.permission = permission;
        return this;
    }

    @SuppressWarnings("all")
    public CommandBuilder parameter(String value) {
        this.parameters.add(new ParameterBuilder<>(value).evaluator((Evaluator) Evaluator.STRING));
        return this;
    }

    @SuppressWarnings({"all"})
    public CommandBuilder parameter(String value, Consumer<ParameterBuilder<String>> proc) {
        var processor = new ParameterBuilder<>(value);
        processor.evaluator((Evaluator) Evaluator.STRING);
        this.parameters.add(processor);

        proc.accept((ParameterBuilder) processor);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> CommandBuilder parameter(String value, Class<T> type, Consumer<ParameterBuilder<T>> proc) {
        var processor = new ParameterBuilder<>(value);
        this.parameters.add(processor);

        proc.accept((ParameterBuilder<T>) processor);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> CommandBuilder parameter(String value, Evaluator<T> evaluator, Consumer<ParameterBuilder<T>> proc) {
        var processor = new ParameterBuilder<T>(value);
        processor.evaluator = evaluator;

        this.parameters.add((ParameterBuilder<Object>) processor);

        proc.accept(processor);
        return this;
    }

    public Command executes(Consumer<CommandContext> ctx) {
        this.handler = context -> {
            ctx.accept(context);
            return null;
        };
        return build();
    }

    public Command executes(Function<CommandContext, Object> ctx) {
        this.handler = ctx;
        return build();
    }

    public static CommandBuilder using(String name) {
        return new CommandBuilder(name);
    }

    private Command build() {
        var parameters = this.parameters.stream().map(ParameterBuilder::build).toArray(Parameter[]::new);
        var indices = new LinkedHashMap<String, Integer>();

        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isVariable()) {
                indices.put(parameters[i].getName(), i);
            }
        }

        return new Command(command, parameters, matches == -1 ? parameters.length : matches, indices, handler);
    }

}
