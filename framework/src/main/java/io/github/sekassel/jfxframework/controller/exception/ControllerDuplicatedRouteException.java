package io.github.sekassel.jfxframework.controller.exception;

public class ControllerDuplicatedRouteException extends RuntimeException {

    public ControllerDuplicatedRouteException(String route, Class<?> oldController, Class<?> newController) {
        super("Route '%s' already leads to '%s' but was tried to be registered for '%s'.".formatted(route, oldController.getName(), newController.getName()));
    }
}
