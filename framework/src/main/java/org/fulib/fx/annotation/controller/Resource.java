package org.fulib.fx.annotation.controller;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark a field as a resource.
 * If a ResourceBundle field in a controller/component is marked with this annotation,
 * it will be used as the resources of the controller/component when loading its FXML.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Resource {
}
