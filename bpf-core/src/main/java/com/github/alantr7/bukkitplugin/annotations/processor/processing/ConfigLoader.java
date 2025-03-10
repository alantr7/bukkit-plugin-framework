package com.github.alantr7.bukkitplugin.annotations.processor.processing;

import com.github.alantr7.bukkitplugin.annotations.config.Config;
import com.github.alantr7.bukkitplugin.annotations.config.ConfigOption;
import com.github.alantr7.bukkitplugin.annotations.processor.meta.FieldMeta;
import com.github.alantr7.bukkitplugin.annotations.processor.processing.executable.ProvidedValue;
import org.bukkit.configuration.file.FileConfiguration;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class ConfigLoader {

    private static final BiFunction<FileConfiguration, String, Integer> INTEGER = FileConfiguration::getInt;

    private static final BiFunction<FileConfiguration, String, Long> LONG = FileConfiguration::getLong;

    private static final BiFunction<FileConfiguration, String, String> STRING = FileConfiguration::getString;

    private static final Map<Class<?>, BiFunction<FileConfiguration, String, ?>> LOADERS = new HashMap<>();

    static {
        LOADERS.put(int.class, INTEGER);
        LOADERS.put(Integer.class, INTEGER);

        LOADERS.put(long.class, LONG);
        LOADERS.put(Long.class, LONG);

        LOADERS.put(String.class, STRING);
    }

    public static ProvidedValue loadOption(FileConfiguration config, Class<?> fieldType, Object defaultValue, ConfigOption annotation) {
        String path = annotation.path();
        if (!config.contains(path)) {
            config.set(path, defaultValue);
            return ProvidedValue.skip();
        }

        BiFunction<FileConfiguration, String, ?> loader = getLoader(fieldType);
        if (loader != null) {
            return ProvidedValue.of(loader.apply(config, path));
        } else {
            return ProvidedValue.skip();
        }
    }

    public static BiFunction<FileConfiguration, String, ?> getLoader(Class<?> type) {
        var loader = LOADERS.get(type);
        if (loader != null)
            return loader;

        return null;
    }

}
