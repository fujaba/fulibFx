package org.fulib.fx;

import javafx.scene.input.KeyCode;
import org.fulib.fx.annotation.controller.*;
import org.fulib.fx.annotation.event.OnDestroy;
import org.fulib.fx.annotation.event.OnInit;
import org.fulib.fx.annotation.event.OnKey;
import org.fulib.fx.annotation.event.OnRender;
import org.fulib.fx.annotation.param.Param;
import org.fulib.fx.annotation.param.Params;
import org.fulib.fx.annotation.param.ParamsMap;
import org.fulib.fx.util.ControllerUtil;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.*;

public class FxClassGenerator {
    private static final String CLASS_SUFFIX = "_Fx";
    private final ProcessingEnvironment processingEnv;
    private final ProcessingHelper helper;

    /**
     * The `javafx.scene.Parent` type.
     */
    private final TypeMirror parent;

    public FxClassGenerator(ProcessingHelper helper, ProcessingEnvironment processingEnv) {
        this.helper = helper;
        this.processingEnv = processingEnv;

        parent = processingEnv.getElementUtils().getTypeElement("javafx.scene.Parent").asType();
    }

    public void generateSidecar(TypeElement componentClass) {
        try {
            final JavaFileObject builderFile = processingEnv.getFiler()
                .createSourceFile(componentClass.getQualifiedName() + CLASS_SUFFIX);
            try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
                this.generateSidecar(out, componentClass);
            }
        } catch (IOException e) {
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
        out.println("import java.util.ResourceBundle;");
        out.println("import javafx.scene.Node;");
        out.println("import org.fulib.fx.annotation.event.OnKey;");
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
        out.println("  @Override");
        out.printf("  public ResourceBundle getResources(%s instance) {%n", simpleClassName);
        generateSidecarResources(out, componentClass);
        out.println("  }");
        out.println("  @Override");
        out.printf("  public String getTitle(%s instance) {%n", simpleClassName);
        generateSidecarTitle(out, componentClass);
        out.println("  }");
        out.println("}");
    }

    private void generateSidecarInit(PrintWriter out, TypeElement componentClass) {
        generateParametersIntoFields(out, componentClass);

        generateCallParamMethods(out, componentClass, Param.class);
        generateCallParamMethods(out, componentClass, Params.class);
        generateCallParamMethods(out, componentClass, ParamsMap.class);

        generateCallInitMethods(out, componentClass);

        generateCallSubComponents(out, componentClass, "init");
    }

    private void generateParametersIntoFields(PrintWriter out, TypeElement componentClass) {
        helper.streamAllFields(componentClass, Param.class).forEach(field -> {
            final Param param = field.getAnnotation(Param.class);
            final String fieldName = field.getSimpleName().toString();
            final String fieldType = field.asType().toString();
            final String paramNameLiteral = helper.stringLiteral(param.value());
            out.printf("    if (params.containsKey(%s)) {%n", paramNameLiteral);

            // TODO field must be public, package-private or protected -- add a diagnostic if it's private
            final String methodName = param.method();
            if (methodName != null && !methodName.isEmpty()) {
                // this is some hacky way to get the @Param().type().
                // param.type() does not work because we cannot access the Class<?> instance at compile time.
                // So we have to read the AnnotationMirror.
                final AnnotationMirror paramMirror = field.getAnnotationMirrors().stream()
                    .filter(a -> "org.fulib.fx.annotation.param.Param".equals(a.getAnnotationType().toString()))
                    .findFirst().orElseThrow();
                final String methodParamType = paramMirror.getElementValues().values().stream()
                    .map(Object::toString)
                    .filter(s -> s.endsWith(".class"))
                    .map(s -> s.substring(0, s.length() - 6)) // remove ".class"
                    .findFirst().orElse("Object"); // else case also happens when type is not specified in the annotation (default Object)
                out.printf("      instance.%s.%s((%s) params.get(%s));%n", fieldName, methodName, methodParamType, paramNameLiteral);
            } else {
                out.printf("      instance.%s = (%s) params.get(%s);%n", fieldName, fieldType, paramNameLiteral);
            }
            out.println("    }");
        });

        helper.streamAllFields(componentClass, ParamsMap.class).forEach(field -> {
            final String fieldName = field.getSimpleName().toString();
            if (field.getModifiers().contains(Modifier.FINAL)) {
                out.printf("    instance.%s.clear();%n", fieldName);
                out.printf("    instance.%s.putAll(params);%n", fieldName);
            } else {
                out.printf("    instance.%s = params;%n", fieldName);
            }
        });
    }

    private void generateCallInitMethods(PrintWriter out, TypeElement componentClass) {
        helper.streamAllMethods(componentClass, OnInit.class)
            .sorted(Comparator.comparingInt(a -> a.getAnnotation(OnInit.class).value()))
            .forEach(methodElement -> generateCall(out, methodElement));
    }

    private void generateCallParamMethods(PrintWriter out, TypeElement componentClass, Class<? extends Annotation> annotation) {
        helper.streamAllMethods(componentClass, annotation)
            .forEach(methodElement -> generateCall(out, methodElement));
    }

    private void generateCall(PrintWriter out, ExecutableElement methodElement) {
        final List<? extends VariableElement> parameters = methodElement.getParameters();
        final List<String> arguments = Arrays.asList(new String[parameters.size()]);

        fillMethodArguments(methodElement, arguments, parameters);

        for (int i = 0; i < parameters.size(); i++) {
            final VariableElement parameter = parameters.get(i);
            final Param paramAnnotation = parameter.getAnnotation(Param.class);
            if (paramAnnotation != null) {
                arguments.set(i, "(%s) params.get(%s)".formatted(parameter.asType(), helper.stringLiteral(paramAnnotation.value())));
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
            arguments.set(0, "(%s) params.get(%s)".formatted(parameters.get(0).asType(), helper.stringLiteral(paramAnnotation.value())));
        }

        final Params paramsAnnotation = methodElement.getAnnotation(Params.class);
        if (paramsAnnotation != null) {
            final String[] paramNames = paramsAnnotation.value();
            for (int i = 0; i < paramNames.length; i++) {
                final VariableElement parameter = parameters.get(i);
                arguments.set(i, "(%s) params.get(%s)".formatted(parameter.asType(), helper.stringLiteral(paramNames[i])));
            }
        }

        final ParamsMap paramsMapAnnotation = methodElement.getAnnotation(ParamsMap.class);
        if (paramsMapAnnotation != null) {
            arguments.set(0, "params");
        }
    }

    private void generateCallSubComponents(PrintWriter out, TypeElement componentClass, String method) {
        helper.streamAllFields(componentClass, SubComponent.class).forEach(field -> {
            if (field.asType().toString().startsWith("javax.inject.Provider")) {
                // Provider fields are initialized on demand
                return;
            }

            final String fieldName = field.getSimpleName().toString();
            out.printf("    this.controllerManager.%s(instance.%s, params);%n", method, fieldName);
        });
    }

    private void generateSidecarDestroy(PrintWriter out, TypeElement componentClass) {
        generateDestroySubComponents(out, componentClass);
        generateCallDestroyMethods(out, componentClass);
    }

    private void generateCallDestroyMethods(PrintWriter out, TypeElement componentClass) {
        helper.streamAllMethods(componentClass, OnDestroy.class)
            .sorted(Comparator.comparingInt(a -> a.getAnnotation(OnDestroy.class).value()))
            .forEach(element -> generateCall(out, element));
    }

    private void generateDestroySubComponents(PrintWriter out, TypeElement componentClass) {
        final List<String> fieldNames = new ArrayList<>();
        helper.streamAllFields(componentClass, SubComponent.class).forEach(field -> {
            if (field.asType().toString().startsWith("javax.inject.Provider")) {
                // Provider fields are destroyed on demand
                return;
            }

            final String fieldName = field.getSimpleName().toString();
            fieldNames.add(fieldName);
        });

        Collections.reverse(fieldNames);
        for (String fieldName : fieldNames) {
            out.printf("    this.controllerManager.destroy(instance.%s);%n", fieldName);
        }
    }

    private void generateSidecarRender(PrintWriter out, TypeElement componentClass) {
        generateCallSubComponents(out, componentClass, "render");
        generateRenderResult(out, componentClass);
        generateCallRenderMethods(out, componentClass);
        generateRegisterKeyEventHandlers(out, componentClass);
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
                if (processingEnv.getTypeUtils().isAssignable(componentClass.asType(), parent)) {
                    out.println("    instance.getChildren().clear();");
                }
                out.printf("    final Node result = this.controllerManager.loadFXML(%s, instance, true);%n", helper.stringLiteral(view));
            }
        } else if (controller != null) {
            final String view = controller.view();
            if (view.startsWith("#")) {
                out.printf("    final Node result = instance.%s();%n", view.substring(1));
            } else {
                final String inferredView = view.isEmpty() ? ControllerUtil.transform(componentClass.getSimpleName().toString()) + ".fxml" : view;
                out.printf("    final Node result = this.controllerManager.loadFXML(%s, instance, false);%n", helper.stringLiteral(inferredView));
            }
        }
    }

    private void generateCallRenderMethods(PrintWriter out, TypeElement componentClass) {
        helper.streamAllMethods(componentClass, OnRender.class)
            .sorted(Comparator.comparingInt(a -> a.getAnnotation(OnRender.class).value()))
            .forEach(element -> generateCall(out, element));
    }

    private void generateRegisterKeyEventHandlers(PrintWriter out, TypeElement componentClass) {
        helper.streamAllMethods(componentClass, OnKey.class)
            .forEach(method -> generateRegisterKeyEventHandler(out, componentClass, method));
    }

    private void generateRegisterKeyEventHandler(PrintWriter out, TypeElement componentClass, ExecutableElement method) {
        // @OnKey(
        //     code = KeyCode.R,
        //     control = true,
        //     shift = false,
        //     alt = false,
        //     meta = false,
        //     character = "r",
        //     text = "",
        //     target = onKey.Target.STAGE,
        //     type = onKey.Type.RELEASED
        // )
        // void foo()
        // --- becomes: ---
        // controllerManager.addKeyEventHandler(instance, target, type, event -> {
        //     if (!event.isControlDown()) return;
        //     if (event.getCode() != KeyCode.R) return;
        //     instance.foo();
        // });
        for (OnKey onKey : method.getAnnotationsByType(OnKey.class)) {
            System.out.println("annotation onkey");
            out.printf("    controllerManager.addKeyEventHandler(instance, OnKey.Target.%s, OnKey.Type.%s.asEventType(), event -> {%n", onKey.target(), onKey.type());
            if (onKey.control()) {
                out.printf("      if (!event.isControlDown()) return;%n");
            } else if (onKey.strict()) {
                out.printf("      if (event.isControlDown()) return;%n");
            }
            if (onKey.shift()) {
                out.printf("      if (!event.isShiftDown()) return;%n");
            } else if (onKey.strict()) {
                out.printf("      if (event.isShiftDown()) return;%n");
            }
            if (onKey.alt()) {
                out.printf("      if (!event.isAltDown()) return;%n");
            } else if (onKey.strict()) {
                out.printf("      if (event.isAltDown()) return;%n");
            }
            if (onKey.meta()) {
                out.printf("      if (!event.isMetaDown()) return;%n");
            } else if (onKey.strict()) {
                out.printf("      if (event.isMetaDown()) return;%n");
            }
            if (onKey.code() != KeyCode.UNDEFINED) {
                out.printf("      if (event.getCode() != javafx.scene.input.KeyCode.%s) return;%n", onKey.code());
            }
            if (!onKey.text().isEmpty()) {
                out.printf("      if (!%s.equals(event.getText())) return;%n", helper.stringLiteral(onKey.text()));
            }
            if (!"\0".equals(onKey.character())) {
                out.printf("      if (!%s.equals(event.getCharacter())) return;%n", helper.stringLiteral(onKey.character()));
            }
            out.printf("      instance.%s(%s);%n", method.getSimpleName(), method.getParameters().size() == 1 ? "event" : "");
            out.printf("    });%n");
        }
    }

    private void generateSidecarResources(PrintWriter out, TypeElement componentClass) {
        helper.streamAllFields(componentClass, Resource.class)
            .findFirst()
            .ifPresentOrElse(
                field -> out.printf("    return instance.%s;%n", field.getSimpleName()),
                () -> out.println("    return controllerManager.getDefaultResourceBundle();")
            );
    }

    private void generateSidecarTitle(PrintWriter out, TypeElement componentClass) {
        final Title title = componentClass.getAnnotation(Title.class);
        if (title == null) {
            out.println("    return null;");
            return;
        }

        if (title.value().startsWith("%")) {
            out.printf("    return getResources(instance).getString(%s);%n", helper.stringLiteral(title.value().substring(1)));
        } else if ("$name".equals(title.value())) {
            out.printf("    return %s;%n", helper.stringLiteral(ControllerUtil.transform(componentClass.getSimpleName().toString())));
        } else {
            out.printf("    return %s;%n", helper.stringLiteral(title.value()));
        }
    }

}
