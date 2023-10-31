package io.github.sekassel.jfxframework.jfx;

import javafx.fxml.FXML;
import javafx.scene.Parent;

public class Children extends Parent {

    @FXML
    String route;

    @FXML
    public String getRoute() {
        return this.route;
    }

    @FXML
    public void setRoute(String route) {
        if (route == null || route.isEmpty()) {
            throw new IllegalArgumentException("Route must not be null or empty");
        }
        this.route = route;
    }

}
