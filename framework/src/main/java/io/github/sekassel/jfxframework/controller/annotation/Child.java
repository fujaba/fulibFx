package io.github.sekassel.jfxframework.controller.annotation;

import java.lang.annotation.*;

@Repeatable(Children.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Child {

    /**
     * The child route at which the controller should be registered.
     * <p>
     * If the main route is "/list" and the child route is "entry/:entry" the full route will be "/list/entry/:entry".
     *
     * @return The child route at which the controller should be registered.
     */
    String route();

    /**
     * The field containing the controller instance to be registered.
     *
     * @return The field containing the controller instance to be registered.
     */
    String controller();

}
