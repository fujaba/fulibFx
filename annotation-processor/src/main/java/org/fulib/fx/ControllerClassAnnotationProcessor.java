package org.fulib.fx;

import com.google.auto.service.AutoService;
import org.fulib.fx.annotation.controller.Component;
import org.fulib.fx.annotation.controller.Controller;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.util.Set;

@SupportedAnnotationTypes({
	"org.fulib.fx.annotation.controller.*"
})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class ControllerClassAnnotationProcessor extends AbstractProcessor
{

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
	{

		for (Element element : roundEnv.getElementsAnnotatedWith(Component.class))
		{
			checkComponent(element);
		}

		return true;
	}

	private void checkComponent(Element element)
	{
		final String view = element.getAnnotation(Component.class).view();
		if (!view.isEmpty())
		{
			checkView(element, view);
		}

		if (element.getAnnotation(Controller.class) != null)
		{
			processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
				"A class cannot be annotated with @Controller and @Component.",
				element);
		}
	}

	private void checkView(Element element, String view)
	{
		try
		{
			final FileObject resource = processingEnv.getFiler().getResource(StandardLocation.SOURCE_PATH,
				processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString(), view);
			System.out.println(resource);
		}
		catch (IOException e)
		{
			processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
				"View file not found: " + view,
				element);
			processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
				"(or source path not set, see https://stackoverflow.com/a/74159042)");
		}
	}
}
