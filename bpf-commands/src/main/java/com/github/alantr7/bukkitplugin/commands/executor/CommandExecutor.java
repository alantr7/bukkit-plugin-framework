package com.github.alantr7.bukkitplugin.commands.executor;

import com.github.alantr7.bukkitplugin.BukkitPlugin;
import com.github.alantr7.bukkitplugin.commands.registry.Command;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class CommandExecutor implements org.bukkit.command.CommandExecutor, TabCompleter {

    private final BukkitPlugin plugin;

    // Commands that are registered
    // Key is the 'base' of the command, value is a list of commands with the same command name
    private final Map<String, List<Command>> registeredCommands = new HashMap<>();

    private final Map<String, Function<String, Object>> resolversByName = new HashMap<>();

    private final Map<Class<?>, Function<String, Object>> resolversByClass = new HashMap<>();

    public CommandExecutor(BukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args) {
        var handler = match(command.getName(), args);
        if (handler == null) {
            sender.sendMessage(ChatColor.RED + "Invalid command syntax, please check your arguments.");
            return false;
        }

        if (!handler.isPermitted(sender)) {
            sender.sendMessage(ChatColor.RED + "You do not have access to that command.");
            return false;
        }

        execute(handler, sender, args);
        return false;
    }

    private void execute(Command handle, CommandSender sender, String[] provided) {
        var map = handle.getParameters();
        var context = new CommandContext(sender, handle, provided, Math.max(map.length, provided.length));

        for (int i = 0; i < map.length; i++) {

            var parameter = handle.getParameters()[i];

            // Try to evaluate the parameter
            if (i < provided.length) {
                if (Evaluator.STRING.equals(parameter.getEvaluator())) {
                    context.setArgument(i, provided[i]);

                    for (var test : parameter.getTestsAfter()) {
                        if (!test.getKey().test(provided[i])) {
                            test.getValue().accept(context, provided[i]);
                            return;
                        }
                    }
                    continue;
                }

                for (var test : parameter.getTestsBefore()) {
                    if (!test.getKey().test(provided[i])) {
                        test.getValue().accept(context, provided[i]);
                        return;
                    }
                }

                try {
                    var value = parameter.getEvaluator().evaluate(provided[i]);
                    context.setArgument(i, value);

                    for (var test : parameter.getTestsAfter()) {
                        if (!test.getKey().test(value)) {
                            test.getValue().accept(context, value);
                            return;
                        }
                    }
                } catch (Exception e) {
                    if (parameter.getEvalFailResponse() != null) {
                        parameter.getEvalFailResponse().accept(context, provided[i]);
                    }
                    return;
                }
            }

            // Get default values, or send 'argument not provided' message
            else {
                if (parameter.getDefaultValueProvider() != null) {
                    context.setArgument(i, parameter.getDefaultValueProvider().apply(sender, context));
                    continue;
                }

                var missingParameterResponse = handle.getParameters()[i].getMissingParameterResponse();
                if (missingParameterResponse != null) {
                    missingParameterResponse.accept(context);
                }

                return;
            }

        }

        for (int i = map.length; i < provided.length; i++) {
            context.setArgument(i, provided[i]);
        }

        handle.execute(context);
    }

    /**
     * Attempts to find a command of which the map matches the arguments provided
     *
     * @param command Command name
     * @param args    Command parameters
     * @return Command that matched the provided arguments. If there are no results, {@code null} will be returned instead
     */
    public Command match(String command, String[] args) {
        var handlers = new ArrayList<>(registeredCommands.getOrDefault(command, Collections.emptyList()));
        int pos = 0;

        handlers.removeIf(handler -> handler.getMatches() > args.length);

        for (; pos < args.length; pos++) {

            boolean isConstantMatched = false;
            int minimumParametersRequired = 0;

            for (var handler : handlers) {

                var matches = handler.getMatches();
                var parameters = handler.getParameters();

                // If handler has fewer parameters than current argument count,
                // then it's 100% a match
                if (matches <= pos)
                    continue;

                var parameter = parameters[pos];

                if (parameter.isConstant()) {
                    if (!parameter.getName().equalsIgnoreCase(args[pos])) {
                        continue;
                    }
                    isConstantMatched = true;
                }

                if (matches - 1 == pos) {
                    // Set a minimum for parameters, so that all handlers with fewer parameters are skipped
                    if (matches > minimumParametersRequired) {
                        minimumParametersRequired = matches;
                    }
                }

            }

            var iterator = handlers.iterator();
            while (iterator.hasNext()) {
                var handler = iterator.next();

                // Remove all handlers with fewer parameters
                if (handler.getMatches() <= pos)
                    continue;

                // Remove all non-matching constants
                if (handler.getParameters()[pos].isConstant() && !handler.getParameters()[pos].getName().equalsIgnoreCase(args[pos]))
                    iterator.remove();

                // Remove all handlers with a variable because constant is a priority
                if (isConstantMatched && (handler.getParameters()[pos].isVariable()))
                    iterator.remove();

                // Remove all handlers with fewer required parameters
                if (handler.getMatches() < minimumParametersRequired)
                    iterator.remove();
            }

        }

        handlers.sort(Comparator.comparingInt(Command::getMatches).reversed());
        return handlers.isEmpty() ? null : handlers.get(0);
    }

    public void registerCommand(Command command) {
        registeredCommands.computeIfAbsent(command.getName(), v -> new LinkedList<>())
                .add(command);
    }

    public boolean isRegistered(String command) {
        return registeredCommands.containsKey(command);
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull org.bukkit.command.Command command, @NotNull String s, @NotNull String[] args) {

        // Temporary solution: Only returns tab complete for static parameters
        var cmd = this.registeredCommands.get(command.getName());
        if (cmd == null)
            return null;

        var open = new LinkedList<>(cmd);
        for (int i = 0; i < args.length - 1; i++) {
            var iterator = open.iterator();
            while (iterator.hasNext()) {
                var handler = iterator.next();
                if (handler.getParameters().length <= i) {
                    iterator.remove();
                    continue;
                }

                var parameter = handler.getParameters()[i];
                if ((parameter.isVariable() && parameter.getTabComplete() == null) || (parameter.isConstant() && !parameter.getName().equalsIgnoreCase(args[i]))) {
                    iterator.remove();
                }
            }
        }

        if (open.isEmpty())
            return Collections.emptyList();

        var tabComplete = new LinkedList<String>();
        open.forEach(handler -> {
            if (args.length > handler.getParameters().length)
                return;

            if (handler.getParameters()[args.length - 1].isConstant()) {
                if (handler.getParameters()[args.length - 1].getName().startsWith(args[args.length - 1]))
                    tabComplete.add(handler.getParameters()[args.length - 1].getName());
            } else {
                var handlerTabComplete = handler.getParameters()[args.length - 1].getTabComplete();
                if (handlerTabComplete == null)
                    return;

                var completions = handlerTabComplete.apply(commandSender, args);
                if (completions != null && !completions.isEmpty())
                    for (String completion : completions) {
                        if (completion.startsWith(args[args.length - 1]))
                            tabComplete.add(completion);
                    }
            }
        });

        return tabComplete;
    }

}
