package io.github.sekassel.jfxframework.annotation.controller;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark controllers.
 * <p>
 * Controllers are used to control inputs and connect the view with the model.
 * <p>
 * For controllers which should be reusable inside other controllers ("sub-controllers"), see {@link Component}.
 * It is recommended to use {@link Component} instead of {@link Controller} as it is more flexible.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Controller {

    /**
     * The view that should be rendered when displaying the controller. This can either be a path to an FXML file or a method that returns a {@link javafx.scene.Node}.
     * <p>
     * If nothing is specified the default naming scheme for an FXML file will be used (ExampleController --> example.fxml).
     * <p>
     * Example: '#myMethod' will call the method myMethod() in the controller and use the returned {@link javafx.scene.Parent} (throwing an exception if the method does not exist or is invalid).
     * <p>
     * Example: 'path/to/myView.fxml' will load the FXML file myView.fxml and use its root node as the view.
     *
     * @return The String specifying the view.
     */
    String view() default "";

}
