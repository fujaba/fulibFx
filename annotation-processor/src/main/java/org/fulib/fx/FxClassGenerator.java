package org.fulib.fx;

import org.fulib.fx.annotation.controller.Component;
import org.fulib.fx.annotation.controller.Controller;
import org.fulib.fx.annotation.controller.SubComponent;
import org.fulib.fx.annotation.event.onDestroy;
import org.fulib.fx.annotation.event.onInit;
import org.fulib.fx.annotation.event.onRender;
import org.fulib.fx.annotation.param.Param;
import org.fulib.fx.annotation.param.Params;
import org.fulib.fx.annotation.param.ParamsMap;
import org.fulib.fx.util.ControllerUtil;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
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
        out.println("import javafx.scene.Node;");
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
        out.printf("  public Node render(%s instance, Map<String, Object> params) {%n", simpleClassName);
        generateSidecarRender(out, componentClass);
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

        initSubComponents(out, componentClass, "init");
    }

    private void fillParametersInfoFields(PrintWriter out, TypeElement componentClass) {
        for (Element element : componentClass.getEnclosedElements()) {
            if (!(element instanceof VariableElement varElement)) {
                continue;
            }

            final Param param = element.getAnnotation(Param.class);
            if (param != null) {
                String fieldName = varElement.getSimpleName().toString();
                String fieldType = varElement.asType().toString();
                // TODO handle primitives, not found, WritableValue
                // TODO field must be public, package-private or protected -- add a diagnostic if it's private
                out.printf("    instance.%s = (%s) params.get(\"%s\");%n", fieldName, fieldType, param.value());
            }

            final ParamsMap paramsMap = element.getAnnotation(ParamsMap.class);
            if (paramsMap != null) {
                String fieldName = varElement.getSimpleName().toString();
                if (varElement.getModifiers().contains(Modifier.FINAL)) {
                    out.printf("    instance.%s.clear();%n", fieldName);
                    out.printf("    instance.%s.putAll(params);%n", fieldName);
                } else {
                    out.printf("    instance.%s = params;%n", fieldName);
                }
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

    private void initSubComponents(PrintWriter out, TypeElement componentClass, String method) {
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
            out.printf("    this.controllerManager.%s(instance.%s, params);%n", method, fieldName);
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

    private void generateSidecarRender(PrintWriter out, TypeElement componentClass) {
        initSubComponents(out, componentClass, "render");
        generateRenderResult(out, componentClass);
        callRenderMethods(out, componentClass);
        // TODO Register key events
        out.println("    return result;");
    }

    private void generateRenderResult(PrintWriter out, TypeElement componentClass) {
        final Component component = componentClass.getAnnotation(Component.class);
        final Controller controller = componentClass.getAnnotation(Controller.class);

        if (component != null) {
            final String view = component.view();
            if (view.isEmpty()) {
                out.println("    final Node result = instance;");
            } else {
                final TypeMirror parent = processingEnv.getElementUtils().getTypeElement("javafx.scene.Parent").asType();
                if (processingEnv.getTypeUtils().isAssignable(componentClass.asType(), parent)) {
                    out.println("    instance.getChildren().clear();");
                }
                out.printf("    final Node result = this.controllerManager.loadFXML(\"%s\", instance, true);%n", view);
            }
        } else if (controller != null) {
            final String view = controller.view();
            if (view.startsWith("#")) {
                out.printf("    final Node result = instance.%s();%n", view.substring(1));
            } else {
                final String inferredView = view.isEmpty() ? ControllerUtil.transform(componentClass.getSimpleName().toString()) + ".fxml" : view;
                out.printf("    final Node result = this.controllerManager.loadFXML(\"%s\", instance, false);%n", inferredView);
            }
        }
    }

    private void callRenderMethods(PrintWriter out, TypeElement componentClass) {
        // TODO inherited methods
        componentClass
            .getEnclosedElements()
            .stream()
            .filter(element -> element.getAnnotation(onRender.class) != null && element instanceof ExecutableElement)
            .sorted(Comparator.comparingInt(a -> a.getAnnotation(onRender.class).value()))
            .forEach(element -> {
                generateInitCall(out, (ExecutableElement) element);
            });
    }
}
