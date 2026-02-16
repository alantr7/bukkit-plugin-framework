package com.github.alantr7.bukkitplugin.annotations;

import com.github.alantr7.bukkitplugin.annotations.generative.*;
import com.github.alantr7.bukkitplugin.annotations.plugin.CommandDefinition;
import com.github.alantr7.bukkitplugin.annotations.plugin.PermissionDefinition;
import com.github.alantr7.bukkitplugin.annotations.plugin.PluginInfo;
import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedAnnotationTypes({
        "com.github.alantr7.bukkitplugin.annotations.generative.JavaPlugin",
        "com.github.alantr7.bukkitplugin.annotations.generative.Command",
        "com.github.alantr7.bukkitplugin.annotations.generative.Permission",
        "com.github.alantr7.bukkitplugin.annotations.generative.Depends",
        "com.github.alantr7.bukkitplugin.annotations.generative.SoftDepends",
})
public class AnnotationProcessor extends AbstractProcessor {

    public static AnnotationProcessor inst;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        inst = this;
    }

    public Messager getMessager() {
        return processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        for (var annotation : annotations) {
            var elements = env.getElementsAnnotatedWith(annotation);
            for (var element : elements) {
                System.out.println(annotation.getSimpleName());
                if (annotation.getSimpleName().toString().equals("JavaPlugin")) {
                    var plugin = element.getAnnotation(JavaPlugin.class);
                    PluginInfo.inst.setName(plugin.name());
                    if (!plugin.main().isEmpty())
                        PluginInfo.inst.setMainClass(plugin.main());
                    PluginInfo.inst.setMainClass(((TypeElement) element).getQualifiedName().toString());
                    PluginInfo.inst.setVersion(plugin.version());
                    PluginInfo.inst.setApiVersion(plugin.apiVersion());
                    PluginInfo.inst.setDescription(plugin.description());
                }

                else if (annotation.getSimpleName().toString().equals("Depends")) {
                    PluginInfo.inst.setDepend(Arrays.asList(element.getAnnotation(Depends.class).value()));
                }

                else if (annotation.getSimpleName().toString().equals("SoftDepends")) {
                    PluginInfo.inst.setSoftDepend(Arrays.asList(element.getAnnotation(SoftDepends.class).value()));
                }

                else if (annotation.getSimpleName().toString().equals("Command")) {
                    var command = element.getAnnotation(Command.class);
                    PluginInfo.inst.getCommands().add(new CommandDefinition(
                            command.name(), command.description(), new String[0], !command.permission().isEmpty() ? command.permission() : null
                    ));
                }

                else if (annotation.getSimpleName().toString().equals("Permission")) {
                    var permission = element.getAnnotation(Permission.class);
                    var field = (VariableElement) element;

                    if (!field.asType().toString().equals("java.lang.String")) {
                        AnnotationProcessor.inst.getMessager().printMessage(Diagnostic.Kind.ERROR, "Only fields of type String can be annotated with @Permission!");
                        continue;
                    }

                    var node = (String) field.getConstantValue();
                    if (node == null) {
                        AnnotationProcessor.inst.getMessager().printMessage(Diagnostic.Kind.ERROR, "Field is not initialized or is not final!");
                        continue;
                    }

                    PluginInfo.inst.getPermissions().add(new PermissionDefinition(node, permission.allowed()));
                }
            }
        }

        if (env.processingOver()) {
            saveAsResource(processingEnv, "plugin.yml", PluginInfo.inst.toYAML().getBytes(StandardCharsets.UTF_8));
        }

        return true;
    }

    public static void saveAsResource(ProcessingEnvironment processingEnv, String name, byte[] data) {
        var filer = processingEnv.getFiler();
        try {
            var resource = filer.createResource(StandardLocation.CLASS_OUTPUT, "", name);
            System.out.println("Created a file: " + resource.toUri());

            var fos = resource.openOutputStream();
            fos.write(data);

            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
