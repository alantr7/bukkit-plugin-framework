package com.github.alantr7.bukkitplugin.annotations;

import com.github.alantr7.bukkitplugin.annotations.relocate.Relocations;
import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedAnnotationTypes({
        "com.github.alantr7.bukkitplugin.annotations.relocate.Relocations",
})
public class AnnotationRelocator extends AbstractProcessor {

    public static final Map<String, String> relocations = new HashMap<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        for (var annotation : annotations) {
            var elements = env.getElementsAnnotatedWith(annotation);
            for (var element : elements) {
                var relocations = element.getAnnotation(Relocations.class);
                var relocates = relocations.value();
                for (var relocation : relocates) {
                    AnnotationRelocator.relocations.put(relocation.from(), relocation.to());
                }
            }
        }

        return true;
    }

    public static String relocate(String input) {
        if (input == null)
            return null;

        for (var entry : relocations.entrySet()) {

            var pattern = entry.getKey();

            if (input.startsWith(pattern)) {
                return entry.getValue() + input.substring(pattern.length());
            }

        }

        return input;
    }

}
