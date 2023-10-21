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
     * If not specified the default naming scheme will be used.
     *
     * @return The path of the FXML file to load.
     */
    String path() default "";

    /**
     * The route of the controller.
     * <p>
     * Example: "/ingame/npc/shop"
     *
     * @return The route of the controller
     */
    String route();

    /**
     * Whether the controller is a sub controller (rendered inside another controller).
     *
     * @return Whether the controller is a sub controller
     */
    boolean sub() default false;


}
