package com.github.alantr7.bukkitplugin.annotations.plugin;

import lombok.Getter;
import lombok.Setter;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PluginInfo {

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String mainClass;

    @Getter
    @Setter
    private String description;

    @Getter
    @Setter
    private String version;

    @Getter
    @Setter
    private String apiVersion;

    @Getter
    private final List<CommandDefinition> commands = new LinkedList<>();

    @Getter
    private final List<PermissionDefinition> permissions = new LinkedList<>();

    public static final PluginInfo inst = new PluginInfo();

    public String toYAML() {
        Map<String, Object> map;
        try {
            var pluginYml = new File(getClass().getClassLoader().getResource("plugin.yml").toURI());
            if (pluginYml.exists())
                map = new Yaml().load(new FileReader(pluginYml));
            else map = new HashMap<>();
        } catch (Exception e) {
            map = new HashMap<>();
        }

        map.putIfAbsent("name", name);
        map.putIfAbsent("main", mainClass);
        map.putIfAbsent("version", version);
        map.putIfAbsent("api-version", apiVersion);

        if (!map.containsKey("commands")) {
            var commandsMap = new HashMap<String, Object>();
            commands.forEach(command -> {
                var commandMap = new HashMap<String, Object>();
                commandMap.put("description", command.description());
                if (command.permission() != null) {
                    commandMap.put("permission", command.permission());
                }

                commandsMap.put(command.name(), commandMap);
            });
            map.put("commands", commandsMap);
        }

        if (!map.containsKey("permissions")) {
            var permissionsMap = new HashMap<String, Object>();
            permissions.forEach(permission -> {
                var permissionMap = new HashMap<String, Object>();
                permissionMap.put("default", permission.allowed().name().toLowerCase());

                permissionsMap.put(permission.node(), permissionMap);
            });
            map.put("permissions", permissionsMap);
        }


        var options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        return new Yaml(options).dump(map);
    }

}
