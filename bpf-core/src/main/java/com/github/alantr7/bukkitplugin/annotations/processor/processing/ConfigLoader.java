package com.github.alantr7.bukkitplugin.annotations.processor.processing;

import com.github.alantr7.bukkitplugin.annotations.config.Config;
import com.github.alantr7.bukkitplugin.annotations.config.ConfigOption;
import com.github.alantr7.bukkitplugin.annotations.processor.meta.FieldMeta;
import com.github.alantr7.bukkitplugin.annotations.processor.processing.executable.ProvidedValue;
import org.bukkit.Location;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class ConfigLoader {

    private static final BiFunction<FileConfiguration, String, Boolean> BOOLEAN = MemorySection::getBoolean;
    private static final BiFunction<FileConfiguration, String, Integer> INTEGER = MemorySection::getInt;
    private static final BiFunction<FileConfiguration, String, Long> LONG = MemorySection::getLong;
    private static final BiFunction<FileConfiguration, String, String> STRING = MemorySection::getString;
    private static final BiFunction<FileConfiguration, String, List<String>> STRING_LIST = MemorySection::getStringList;
    private static final BiFunction<FileConfiguration, String, Location> LOCATION = MemorySection::getLocation;
    private static final Map<Class<?>, BiFunction<FileConfiguration, String, ?>> LOADERS = new HashMap<>();

    static {
        LOADERS.put(Boolean.TYPE, BOOLEAN);
        LOADERS.put(Boolean.class, BOOLEAN);
        LOADERS.put(Integer.TYPE, INTEGER);
        LOADERS.put(Integer.class, INTEGER);
        LOADERS.put(Long.TYPE, LONG);
        LOADERS.put(Long.class, LONG);
        LOADERS.put(String.class, STRING);
        LOADERS.put(List.class, STRING_LIST);
        LOADERS.put(Location.class, LOCATION);
    }

    public static ProvidedValue loadOption(FileConfiguration config, Class<?> fieldType, Object defaultValue, ConfigOption annotation) {
        String path = annotation.path();
        if (!config.contains(path)) {
            config.set(path, defaultValue);
            return ProvidedValue.skip();
        }

        BiFunction<FileConfiguration, String, ?> loader = getLoader(fieldType);
        return loader != null ? ProvidedValue.of(loader.apply(config, path)) : ProvidedValue.skip();
    }

    public static BiFunction<FileConfiguration, String, ?> getLoader(Class<?> type) {
        var loader = LOADERS.get(type);
        if (loader != null)
            return loader;

        return null;
    }

}
