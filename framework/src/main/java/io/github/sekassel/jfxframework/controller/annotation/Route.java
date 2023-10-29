package io.github.sekassel.jfxframework.controller.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to define routes inside an application.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Route {

    /**
     * The route at which the controller should be registered.
     * <p>
     * If not specified the name of the field will be used as the route.
     *
     * @return The route at which the controller should be registered.
     */
    String route() default "$name";

}
