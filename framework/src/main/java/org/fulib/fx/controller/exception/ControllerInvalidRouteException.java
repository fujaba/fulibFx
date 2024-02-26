package org.fulib.fx.controller.exception;

import static org.fulib.fx.util.FrameworkUtil.error;

public class ControllerInvalidRouteException extends RuntimeException {

    public ControllerInvalidRouteException(String route) {
        super(error(3005).formatted(route.isEmpty() ? "(empty)" : route));
    }

}
