package com.github.alantr7.bukkitplugin.annotations.cacher.builder;

import com.github.alantr7.bukkitplugin.annotations.cacher.map.ClassMap;
import com.github.alantr7.bukkitplugin.annotations.cacher.map.MappedField;
import com.github.alantr7.bukkitplugin.annotations.cacher.map.MappedMethod;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.HashMap;
import java.util.Map;

public class ClassMapQueueBuilder {

    private final Map<String, ClassMap> queue = new HashMap<>();

    protected ClassMap withClass(TypeElement klass) {
        var mapped = queue.get(klass.getQualifiedName().toString());
        if (mapped != null)
            return mapped;

        mapped = new ClassMap(klass.getQualifiedName().toString());
        mapped.superclass = klass.getSuperclass().toString();
        mapped.interfaces = klass.getInterfaces().stream().map(TypeMirror::toString).toArray(String[]::new);

        queue.put(klass.getQualifiedName().toString(), mapped);
        return mapped;
    }

    public void addClass(TypeElement element, TypeElement annotation) {
        withClass(element).annotations.add(annotation.getQualifiedName().toString());
    }

    public void addField(TypeElement enclosing, VariableElement field, TypeElement annotation) {
        withClass(enclosing)
                .fields.computeIfAbsent(field.getSimpleName().toString(), v -> new MappedField(v, field.asType().toString()))
                .annotations.add(annotation.getQualifiedName().toString());
    }

    public void addMethod(TypeElement enclosing, ExecutableElement method, TypeElement annotation) {
        withClass(enclosing)
                .methods.computeIfAbsent(method.getSimpleName() + " " + method, v -> {
                    var mapped = new MappedMethod(
                            method.getSimpleName().toString(),
                            method.getReturnType().toString(),
                            method.getParameters().stream().map(element -> {
                                var parameterType = element.asType().toString();
                                var param = new MappedMethod.Parameter(parameterType, element.getSimpleName().toString());

                                return param;
                            }).toArray(MappedMethod.Parameter[]::new)
                    );
                    return mapped;
                })
                .annotations.add(annotation.getQualifiedName().toString());
    }

    public void addParameter(Element parameter, TypeElement annotation) {
        if (parameter.getEnclosingElement().getKind() != ElementKind.METHOD)
            return;

        var method = (ExecutableElement) parameter.getEnclosingElement();

        // Register class if it's not already
        var klass = withClass((TypeElement) method.getEnclosingElement());

        // Register method if it's not already
        var mapped = klass.methods.computeIfAbsent(method.getSimpleName() + " " + method, v -> new MappedMethod(method.getSimpleName().toString(), method.getReturnType().toString(), method.getParameters().stream().map(element -> {
            var parameterType = element.asType().toString();
            var param = new MappedMethod.Parameter(parameterType, element.getSimpleName().toString());

            return param;
        }).toArray(MappedMethod.Parameter[]::new)));

        mapped.parametersMap.get(parameter.getSimpleName().toString()).annotations.add(annotation.getQualifiedName().toString());
    }

    public Map<String, ClassMap> build() {
        queue.forEach((path, map) -> {
            map.annotations.forEach(annotation -> System.out.println("@" + annotation));
            System.out.println("class " + path + " {");

            map.methods.forEach((signature, method) -> {
                method.annotations.forEach(annotation -> System.out.println("  @" + annotation));
                if (method.parameters.length == 0)
                    System.out.println("  " + method.returnType + " " + method.name + " ()");
                else {
                    System.out.println("  " + method.returnType + " " + method.name + " (");
                    method.parametersMap.forEach((name, param) -> {
                        param.annotations.forEach(annotation -> System.out.println("    @" + annotation));
                        System.out.println("    " + param.type + " " + param.name + ",");
                    });
                    System.out.println("  );");
                }
            });

            System.out.println("}\n");
        });

        return queue;
    }

}
