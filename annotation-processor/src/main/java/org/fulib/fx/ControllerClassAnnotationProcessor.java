package org.fulib.fx;

import com.google.auto.service.AutoService;
import org.fulib.fx.annotation.controller.Component;
import org.fulib.fx.annotation.controller.Controller;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

@SupportedAnnotationTypes({
        "org.fulib.fx.annotation.controller.*"
})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(javax.annotation.processing.Processor.class)
public class ControllerClassAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        System.out.println("test");

        for (Element element : roundEnv.getElementsAnnotatedWith(Component.class)) {
            if (element.getAnnotation(Controller.class) != null) {
                processingEnv.getMessager().printMessage(javax.tools.Diagnostic.Kind.ERROR, "A class cannot be annotated with @Controller and @Component.", element);
            }

            System.out.println(element + " " + element.asType());
        }

        return true;
    }
}
