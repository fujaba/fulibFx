package org.fulib.fx;

import com.google.auto.service.AutoService;
import org.fulib.fx.annotation.Route;
import org.fulib.fx.annotation.controller.Component;
import org.fulib.fx.annotation.controller.Controller;
import org.fulib.fx.annotation.controller.SubComponent;
import org.fulib.fx.util.Util;

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
import java.util.concurrent.ConcurrentHashMap;

@SupportedAnnotationTypes({
        "org.fulib.fx.annotation.controller.*",
        "org.fulib.fx.annotation.Route"
})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
@SuppressWarnings("unused")
public class ControllerAnnotationProcessor extends AbstractProcessor {

    private final Set<Element> components = ConcurrentHashMap.newKeySet();
    private final Set<Element> controllers = ConcurrentHashMap.newKeySet();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        // Check if a class is annotated with both @Controller and @Component
        for (Element element : roundEnv.getRootElements()) {
            checkDoubleAnnotation(element);
        }

        // Check if the element is a valid component
        for (Element element : roundEnv.getElementsAnnotatedWith(Component.class)) {
            checkComponent(element);
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

        return true;
    }

    private void checkRoute(Element element) {
        // Check if the element is a field
        if (element.getKind() != ElementKind.FIELD) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "The @Route annotation can only be used on fields.", element);
            return;
        }

        System.out.println(element.asType().toString());
        System.out.println(processingEnv.getElementUtils().getTypeElement("javax.inject.Provider").asType().toString());


        // Check if the field is of a provider type
        if (!element.asType().toString().startsWith("javax.inject.Provider")) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "The route field must be of a provider type.", element);
            return;
        }

        // Check if the provided class is of a controller or component type
        for (TypeMirror generic : ((DeclaredType) element.asType()).getTypeArguments()) {
            if (!isController(generic) && !isComponent(generic)) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "The route field must provide a controller or component type.", element);
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
            checkViewResource(element, Util.transform(element.getSimpleName().toString()) + ".fxml");
        }
        controllers.add(element);
    }

    private void checkComponent(Element element) {
        final String view = element.getAnnotation(Component.class).view();

        // Check if the specified view file exists
        if (!view.isEmpty()) {
            checkViewResource(element, view);
        }

        // Check if the element is a subclass of javafx.scene.Parent
        if (!processingEnv.getTypeUtils().isAssignable(element.asType(), processingEnv.getElementUtils().getTypeElement("javafx.scene.Parent").asType())) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Component must extend (a subclass of) javafx.scene.Parent.", element);
        }

        components.add(element);
    }

    private void checkDoubleAnnotation(Element element) {
        if (element.getAnnotation(Controller.class) != null && element.getAnnotation(Component.class) != null) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "A class cannot be annotated with @Controller and @Component.", element);
        }
    }

    private void checkViewResource(Element element, String view) {
        try {
            // Check if the specified view file exists in the source path
            final FileObject resource = processingEnv.getFiler().getResource(StandardLocation.SOURCE_PATH,
                    processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString(), view);
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "View file not found: " + view, element);
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "(or source path not set, see https://stackoverflow.com/a/74159042)");
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
                                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Method must not have parameters: " + methodName + "()", element);
                            }

                            TypeMirror parent = processingEnv.getElementUtils().getTypeElement("javafx.scene.Parent").asType();

                            if (!processingEnv.getTypeUtils().isAssignable(method.getReturnType(), parent)) {
                                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Method must return a (subclass of) javafx.scene.Parent: " + methodName + "()", element);
                            }
                        },
                        () -> processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Method not found: " + methodName + "()", element)
                );
    }

    private void checkSubComponentElement(Element element) {
        if (element.getKind() != ElementKind.FIELD) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "The @SubComponent annotation can only be used on fields.", element);
            return;
        }

        if (!isComponent(element.asType())) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "The field must be of a component type.", element);
        }

    }

    // This method only works after the components/controllers have been processed
    private boolean isComponent(TypeMirror typeMirror) {
        return components.stream().anyMatch(element -> element.asType() == typeMirror);
    }

    // This method only works after the components/controllers have been processed
    private boolean isController(TypeMirror typeMirror) {
        return controllers.stream().anyMatch(element -> element.asType() == typeMirror);
    }

}
