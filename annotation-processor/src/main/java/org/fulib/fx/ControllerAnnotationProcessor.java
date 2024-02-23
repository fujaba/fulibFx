package org.fulib.fx;

import com.google.auto.service.AutoService;
import org.fulib.fx.annotation.Route;
import org.fulib.fx.annotation.controller.Component;
import org.fulib.fx.annotation.controller.Controller;
import org.fulib.fx.annotation.controller.SubComponent;
import org.fulib.fx.annotation.param.Params;
import org.fulib.fx.annotation.param.ParamsMap;
import org.fulib.fx.util.ControllerUtil;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.util.Set;

import static org.fulib.fx.util.FrameworkUtil.error;

@SupportedAnnotationTypes({
        "org.fulib.fx.annotation.controller.*",
        "org.fulib.fx.annotation.Route",
        "org.fulib.fx.annotation.param.*",
})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
@SuppressWarnings("unused")
public class ControllerAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        // Check if the element is a valid component
        for (Element element : roundEnv.getElementsAnnotatedWith(Component.class)) {
            checkComponent(element);
            checkDoubleAnnotation(element); // Check if a class is annotated with both @Controller and @Component
        }

        // Check if the element is a valid controller
        for (Element element : roundEnv.getElementsAnnotatedWith(Controller.class)) {
            checkController(element);
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(SubComponent.class)) {
            checkSubComponentElement(element);
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(Route.class)) {
            checkRoute(element);
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(ParamsMap.class)) {
            checkParamsMap(element);
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(Params.class)) {
            checkParams(element);
        }

        return true;
    }

    private void checkParams(Element element) {
        if (element instanceof ExecutableElement method) {
            Params annotation = method.getAnnotation(Params.class);
            if (method.getParameters().size() != annotation.value().length) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, error(4006).formatted(method.getSimpleName(), method.getEnclosingElement().asType().toString()), method);
            }
        }
    }

    private void checkParamsMap(Element element) {
        if (element instanceof ExecutableElement method) {
            if (method.getParameters().size() != 1) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, error(4003).formatted(method.getSimpleName(), method.getEnclosingElement().asType().toString()), method);
            }
        }
    }

    private void checkRoute(Element element) {

        // Check if the field is of a provider type
        if (!element.asType().toString().startsWith("javax.inject.Provider")) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, error(3004).formatted(element.getSimpleName(), element.asType().toString()), element);
            return;
        }

        // Check if the provided class is of a controller or component type
        for (TypeMirror generic : ((DeclaredType) element.asType()).getTypeArguments()) {
            if (!isController(generic) && !isComponent(generic)) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, error(3003).formatted(element.getSimpleName(), element.asType().toString()), element);
            }
        }

    }

    private void checkController(Element element) {
        final String view = element.getAnnotation(Controller.class).view();
        if (!view.isEmpty()) {
            if (view.startsWith("#")) {
                // Check if the view is referring to a valid method
                checkViewMethod(element, view);
            } else {
                // Check if the specified view file exists
                checkViewResource(element, view);
            }
        } else {
            // No view specified, so we transform the class name to a view file name
            checkViewResource(element, ControllerUtil.transform(element.getSimpleName().toString()) + ".fxml");
        }
    }

    private void checkComponent(Element element) {
        final String view = element.getAnnotation(Component.class).view();

        // Check if the specified view file exists
        if (!view.isEmpty()) {
            checkViewResource(element, view);
        }

        // Check if the element is a subclass of javafx.scene.Parent
        if (!processingEnv.getTypeUtils().isAssignable(element.asType(), processingEnv.getElementUtils().getTypeElement("javafx.scene.Parent").asType())) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, error(1006), element);
        }
    }

    private void checkDoubleAnnotation(Element element) {
        if (element.getAnnotation(Controller.class) != null && element.getAnnotation(Component.class) != null) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, error(1007), element);
        }
    }

    private void checkViewResource(Element element, String view) {
        try {
            // Check if the specified view file exists in the source path
            final FileObject resource = processingEnv.getFiler().getResource(StandardLocation.SOURCE_PATH,
                    processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString(), view);
        } catch (IOException e) {
            String viewPath = processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString().replace('.', '/') + "/" + view;
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, error(2000).formatted(viewPath), element);
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, error(2006));
        }
    }

    private void checkViewMethod(Element element, String view) {
        String methodName = view.substring(1); // remove the leading '#'
        processingEnv.getElementUtils().getAllMembers((TypeElement) element).stream()
                .filter(elem -> elem.getKind() == ElementKind.METHOD)
                .filter(elem -> elem.getSimpleName().toString().equals(methodName))
                .map(elem -> (ExecutableElement) elem)
                .findFirst()
                .ifPresentOrElse(
                        method -> {
                            if (!method.getParameters().isEmpty()) {
                                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, error(1008), method);
                            }

                            TypeMirror parent = processingEnv.getElementUtils().getTypeElement("javafx.scene.Parent").asType();

                            if (!processingEnv.getTypeUtils().isAssignable(method.getReturnType(), parent)) {
                                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, error(1002), method);
                            }
                        },
                        () -> processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, error(1003).formatted(methodName), element)
                );
    }

    private void checkSubComponentElement(Element element) {
        // Check if the field is of a component type
        if (isComponent(element.asType())) {
            return;
        }

        // Check if the field is of a provider type
        if (isProvider(element.asType())) {
            // Check if the provided class is of a component type
            if (element.asType() instanceof DeclaredType type) {
                final TypeMirror componentType = type.getTypeArguments().get(0);
                if (isComponent(componentType)) {
                    return;
                }
            }
        }
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, error(6005).formatted(element.getSimpleName(), element.getEnclosingElement().asType().toString()), element);
    }

    // The method expects the typeMirror to be a declared type
    private boolean isComponent(TypeMirror typeMirror) {
        DeclaredType declaredType = (DeclaredType) typeMirror;
        return declaredType.asElement().getAnnotation(Component.class) != null;
    }

    // The method expects the typeMirror to be a declared type
    private boolean isController(TypeMirror typeMirror) {
        DeclaredType declaredType = (DeclaredType) typeMirror;
        return declaredType.asElement().getAnnotation(Controller.class) != null;
    }

    private boolean isProvider(TypeMirror typeMirror) {
        return typeMirror.toString().startsWith("javax.inject.Provider");
    }

}
