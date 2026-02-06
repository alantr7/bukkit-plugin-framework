package com.github.alantr7.bukkitplugin.annotations;

import com.github.alantr7.bukkitplugin.annotations.cacher.MetaWriter;
import com.github.alantr7.bukkitplugin.annotations.cacher.builder.ClassMapQueueBuilder;
import com.github.alantr7.bukkitplugin.annotations.cacher.map.ClassMap;
import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.Set;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedAnnotationTypes({
        "com.github.alantr7.bukkitplugin.annotations.core.Singleton",
        "com.github.alantr7.bukkitplugin.annotations.core.Inject",
        "com.github.alantr7.bukkitplugin.annotations.core.Invoke",
        "com.github.alantr7.bukkitplugin.annotations.core.InvokePeriodically",
        "com.github.alantr7.bukkitplugin.annotations.core.RequiresPlugin",

        "com.github.alantr7.bukkitplugin.annotations.config.Config",
        "com.github.alantr7.bukkitplugin.annotations.config.ConfigOption",

        "com.github.alantr7.bukkitplugin.commands.annotations.CommandHandler",

        "org.bukkit.event.EventHandler",
})
public class AnnotationScanner extends AbstractProcessor {

    private final ClassMapQueueBuilder queue = new ClassMapQueueBuilder();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        for (var annotation : annotations) {
            var elements = env.getElementsAnnotatedWith(annotation);
            for (var element : elements) {
                switch (element.getKind()) {
                    case CLASS -> queue.addClass((TypeElement) element, annotation);
                    case FIELD ->
                            queue.addField((TypeElement) element.getEnclosingElement(), (VariableElement) element, annotation);
                    case METHOD ->
                            queue.addMethod((TypeElement) element.getEnclosingElement(), (ExecutableElement) element, annotation);
                    case PARAMETER -> queue.addParameter(element, annotation);
                }
            }
        }

        if (env.processingOver()) {
            // Save to a file
            var map = MetaWriter.writeToByteArray(queue.build().values().toArray(new ClassMap[0]));
            AnnotationProcessor.saveAsResource(processingEnv, "beans.dat", map);
        }

        return true;
    }

}
