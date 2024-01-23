package io.github.sekassel.jfxframework.annotation.controller;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * SubComponent annotation.
 * <p>
 * Component fields annotated with {@link SubComponent} will be initialized and rendered with the controller they are defined in.
 * The instance still needs to be provided by the user oder using dependency injection.
 * The instance will be used if the fxml specifies a subcomponent with the same type.
 * <p>
 * Provider fields annotated with {@link SubComponent} can be used if the fxml specifies a subcomponent with the same type.
 * Instances manually provided by the provider using {@code get()} will NOT be initialized or rendered!
 * <p>
 * When a sub-controller is specified in the fxml, the framework will look for any component field annotated with @SubController.
 * If a matching field is found, the instance will be used as the sub-controller.
 * If no matching field is found, the framework will look for any provider field annotated with @SubController.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface SubComponent {

}