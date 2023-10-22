package io.github.sekassel.jfxframework.controller;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark controllers.
 * <p>
 * See
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Controller {

    /**
     * The path of the FXML file to load.
     * <p>
     * If not specified the default naming scheme ({@link ControllerManager#transform(String)}) will be used (ExampleController --> example.fxml).
     *
     * @return The path of the FXML file to load.
     */
    String path() default "";

    /**
     * The id of the controller.
     * <p>
     * If not specified the default naming scheme will be used (io.github.example.ExampleController --> example).
     *
     * @return The route of the controller
     */
    String id() default "";

    /**
     * Whether the controller is a sub controller (rendered inside another controller).
     *
     * @return Whether the controller is a sub controller
     */
    boolean sub() default false;


}
