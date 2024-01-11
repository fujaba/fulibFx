package io.github.sekassel.jfxframework.annotation.controller;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * SubController annotation.
 * <p>
 * Fields annotated with @SubController will be initialized and rendered with the controller they are defined in.
 * The instance still needs to be provided by the user oder using dependency injection.
 * <p>
 * The instance will be used if a sub-controller of the same type with the same (or none) id is needed.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface SubController {

    /**
     * The id of the sub-controller which is used to identify the instance.
     * Can be left empty if there is only one sub-controller of the same type.
     *
     * @return The id of the sub-controller
     */
    String value() default "";

}