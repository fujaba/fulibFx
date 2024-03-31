package org.fulib.fx;

import org.fulib.fx.annotation.controller.SubComponent;
import org.fulib.fx.annotation.event.onDestroy;
import org.fulib.fx.annotation.event.onInit;
import org.fulib.fx.annotation.param.Param;
import org.fulib.fx.annotation.param.Params;
import org.fulib.fx.annotation.param.ParamsMap;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.*;

public class FxClassGenerator {
    private static final String CLASS_SUFFIX = "_Fx";
    private final ProcessingEnvironment processingEnv;

    public FxClassGenerator(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    public void generateSidecar(TypeElement componentClass) {
        try {
            final JavaFileObject builderFile = processingEnv.getFiler()
                .createSourceFile(componentClass.getQualifiedName() + CLASS_SUFFIX);
            try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
                this.generateSidecar(out, componentClass);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void generateSidecar(PrintWriter out, TypeElement componentClass) throws IOException {
        final String className = componentClass.getQualifiedName().toString();
        String packageName = null;
        int lastDot = className.lastIndexOf('.');
        if (lastDot > 0) {
            packageName = className.substring(0, lastDot);
        }

        String simpleClassName = className.substring(lastDot + 1);
        String builderClassName = className + CLASS_SUFFIX;
        String builderSimpleClassName = builderClassName.substring(lastDot + 1);

        if (packageName != null) {
            out.println("package " + packageName + ";");
            out.println();
        }

        out.println("import java.util.Map;");
        out.println("import org.fulib.fx.controller.ControllerManager;");
        out.println("import org.fulib.fx.controller.internal.FxSidecar;");
        out.println();

        out.printf("public class %s implements FxSidecar<%s> {%n", builderSimpleClassName, simpleClassName);
        out.println("  private final ControllerManager controllerManager;");
        out.printf("  public %s(ControllerManager cm) {%n", builderSimpleClassName);
        out.println("    this.controllerManager = cm;");
        out.println("  }");
        out.println("  @Override");
        out.printf("  public void init(%s instance, Map<String, Object> params) {%n", simpleClassName);
        generateSidecarInit(out, componentClass);
        out.println("  }");
        out.println("  @Override");
        out.printf("  public void destroy(%s instance) {%n", simpleClassName);
        generateSidecarDestroy(out, componentClass);
        out.println("  }");
        out.println("}");
    }

    private void generateSidecarInit(PrintWriter out, TypeElement componentClass) {
        fillParametersInfoFields(out, componentClass);

        callParamMethods(out, componentClass, Param.class);
        callParamMethods(out, componentClass, Params.class);
        callParamMethods(out, componentClass, ParamsMap.class);

        callInitMethods(out, componentClass);

        initSubComponents(out, componentClass);
    }

    private void fillParametersInfoFields(PrintWriter out, TypeElement componentClass) {
        for (Element element : componentClass.getEnclosedElements()) {
            final Param param = element.getAnnotation(Param.class);
            if (param != null && element instanceof VariableElement varElement) {
                String fieldName = varElement.getSimpleName().toString();
                String fieldType = varElement.asType().toString();
                // TODO handle ParamsMap, primitives, not found, WritableValue
                // TODO field must be public, package-private or protected -- add a diagnostic if it's private
                out.printf("    instance.%s = (%s) params.get(\"%s\");%n", fieldName, fieldType, param.value());
            }
        }
    }

    private void callInitMethods(PrintWriter out, TypeElement componentClass) {
        // TODO inherited methods
        componentClass
            .getEnclosedElements()
            .stream()
            .filter(element -> element.getAnnotation(onInit.class) != null && element instanceof ExecutableElement)
            .map(element -> (ExecutableElement) element)
            .sorted(Comparator.comparingInt(a -> a.getAnnotation(onInit.class).value()))
            .forEach(methodElement -> {
                generateInitCall(out, methodElement);
            });
    }

    private void callParamMethods(PrintWriter out, TypeElement componentClass, Class<? extends Annotation> annotation) {
        // TODO inherited methods
        componentClass
            .getEnclosedElements()
            .stream()
            .filter(element -> element.getAnnotation(annotation) != null && element instanceof ExecutableElement)
            .map(element -> (ExecutableElement) element)
            .forEach(methodElement -> {
                generateInitCall(out, methodElement);
            });
    }

    private void generateInitCall(PrintWriter out, ExecutableElement methodElement) {
        final List<? extends VariableElement> parameters = methodElement.getParameters();
        final List<String> arguments = Arrays.asList(new String[parameters.size()]);

        fillMethodArguments(methodElement, arguments, parameters);

        for (int i = 0; i < parameters.size(); i++) {
            final VariableElement parameter = parameters.get(i);
            final Param paramAnnotation = parameter.getAnnotation(Param.class);
            if (paramAnnotation != null) {
                arguments.set(i, "(%s) params.get(\"%s\")".formatted(parameter.asType(), paramAnnotation.value()));
                continue;
            }

            final ParamsMap paramsMapAnnotation = parameter.getAnnotation(ParamsMap.class);
            if (paramsMapAnnotation != null) {
                arguments.set(i, "params");
                continue;
            }
        }

        out.printf("    instance.%s(%s);%n", methodElement.getSimpleName(), String.join(", ", arguments));
    }

    private void fillMethodArguments(ExecutableElement methodElement, List<String> arguments, List<? extends VariableElement> parameters) {
        final Param paramAnnotation = methodElement.getAnnotation(Param.class);
        if (paramAnnotation != null) {
            arguments.set(0, "(%s) params.get(\"%s\")".formatted(parameters.get(0).asType(), paramAnnotation.value()));
        }

        final Params paramsAnnotation = methodElement.getAnnotation(Params.class);
        if (paramsAnnotation != null) {
            final String[] paramNames = paramsAnnotation.value();
            for (int i = 0; i < paramNames.length; i++) {
                final VariableElement parameter = parameters.get(i);
                arguments.set(i, "(%s) params.get(\"%s\")".formatted(parameter.asType(), paramNames[i]));
            }
        }

        final ParamsMap paramsMapAnnotation = methodElement.getAnnotation(ParamsMap.class);
        if (paramsMapAnnotation != null) {
            arguments.set(0, "params");
        }
    }

    private void initSubComponents(PrintWriter out, TypeElement componentClass) {
        for (final Element element : componentClass.getEnclosedElements()) {
            if (!(element instanceof VariableElement varElement)) {
                continue;
            }

            final SubComponent subComponent = element.getAnnotation(SubComponent.class);
            if (subComponent == null) {
                continue;
            }

            if (varElement.asType().toString().startsWith("javax.inject.Provider")) {
                // Provider fields are initialized on demand
                continue;
            }

            final String fieldName = varElement.getSimpleName().toString();
            out.printf("    this.controllerManager.init(instance.%s, params);%n", fieldName);
        }
    }

    private void generateSidecarDestroy(PrintWriter out, TypeElement componentClass) {
        destroySubComponents(out, componentClass);
        callDestroyMethods(out, componentClass);
    }

    private void callDestroyMethods(PrintWriter out, TypeElement componentClass) {
        // TODO inherited methods
        componentClass
            .getEnclosedElements()
            .stream()
            .filter(element -> element.getAnnotation(onDestroy.class) != null && element instanceof ExecutableElement)
            .sorted(Comparator.comparingInt(a -> a.getAnnotation(onDestroy.class).value()))
            .forEach(element -> {
                generateInitCall(out, (ExecutableElement) element);
            });
    }

    private void destroySubComponents(PrintWriter out, TypeElement componentClass) {
        final List<String> fieldNames = new ArrayList<>();
        for (final Element element : componentClass.getEnclosedElements()) {
            if (!(element instanceof VariableElement varElement)) {
                continue;
            }

            final SubComponent subComponent = element.getAnnotation(SubComponent.class);
            if (subComponent == null) {
                continue;
            }

            if (varElement.asType().toString().startsWith("javax.inject.Provider")) {
                // Provider fields are destroyed on demand
                continue;
            }

            final String fieldName = varElement.getSimpleName().toString();
            fieldNames.add(fieldName);
        }

        Collections.reverse(fieldNames);
        for (String fieldName : fieldNames) {
            out.printf("    this.controllerManager.destroy(instance.%s);%n", fieldName);
        }
    }
}
