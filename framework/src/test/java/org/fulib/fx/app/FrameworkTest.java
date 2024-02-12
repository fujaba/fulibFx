package org.fulib.fx.app;

import org.fulib.fx.FulibFxApp;
import org.fulib.fx.app.controller.modal.ModalComponent;
import org.fulib.fx.app.controller.sub.ButtonSubComponent;
import org.fulib.fx.controller.Modals;
import org.fulib.fx.controller.exception.ControllerInvalidRouteException;
import org.fulib.fx.controller.exception.IllegalControllerException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

public class FrameworkTest extends ApplicationTest {

    public final FulibFxApp app = new FulibFxApp() {

        final TestComponent component = DaggerTestComponent.builder().mainApp(this).build();

        @Override
        public void start(Stage stage) {
            super.start(stage);
            registerRoutes(component.routes());
        }
    };

    @Override
    public void start(Stage stage) throws Exception {
        super.start(stage);
        app.start(stage);
        stage.requestFocus();
    }

    /**
     * Tests if the framework is able to load the routes and different kinds of controllers.
     */
    @Test
    public void controllers() {
        app.show("/controller/basic");
        verifyThat("Basic Controller", Node::isVisible);
        sleep(200);

        app.show("/controller/method");
        verifyThat("Method Controller", Node::isVisible);
        sleep(200);

        app.show("/controller/view");
        verifyThat("View Controller", Node::isVisible);
        sleep(200);

        app.show("/component/basic");
        verifyThat("Basic Component", Node::isVisible);
        sleep(200);

        app.show("/component/root");
        verifyThat("Root Component", Node::isVisible);
        sleep(200);

        app.show("../root");
        verifyThat("Root Component", Node::isVisible);
        sleep(200);

        assertThrows(ControllerInvalidRouteException.class, () -> app.show("/controller/invalid"));

        assertThrows(IllegalControllerException.class, () -> app.show("/controller/nonextending"));

    }

    /**
     * Tests if the framework can load a controller with a subcomponent and if the subcomponent is correctly injected.
     */
    @Test
    public void subComponent() {
        app.show("/controller/withsubcomponent");
        sleep(200);
        ButtonSubComponent button = lookup("#buttonSubComponent").query();
        verifyThat(button, Node::isVisible);
        assertEquals(100, button.getMinWidth());
    }

    /**
     * Tests if the framework can load a controller with a for loop and if the loop is correctly executed.
     */
    @Test
    public void simpleForTest() {
        ObservableList<String> list = FXCollections.observableList(new ArrayList<>());
        FulibFxApp.scheduler().scheduleDirect(() -> {
            list.add("Hello");
            list.add("World");
            list.add("!");
        });

        app.show("/controller/for", Map.of("list", list));
        verifyThat("#container", Node::isVisible);

        VBox container = lookup("#container").queryAs(VBox.class);
        assertEquals(3, container.getChildren().size());

        FulibFxApp.scheduler().scheduleDirect(() -> list.remove("World"));
        waitForFxEvents();

        assertEquals(2, container.getChildren().size());

        FulibFxApp.scheduler().scheduleDirect(() -> list.add(1, "World"));
        waitForFxEvents();

        assertEquals(3, container.getChildren().size());
    }

    @Test
    public void testSubOrder() {
        List<String> initList = new ArrayList<>();
        List<String> renderList = new ArrayList<>();
        List<String> destroyList = new ArrayList<>();

        app.show("/ordertest/main", Map.of("initList", initList, "renderList", renderList, "destroyList", destroyList));

        assertEquals(List.of("main", "sub", "subsub", "othersubsub"), initList);
        assertEquals(List.of("subsub", "othersubsub", "sub", "main"), renderList);
        assertEquals(List.of(), destroyList);

        app.show("/controller/basic");

        assertEquals(List.of("othersubsub", "subsub", "sub", "main"), destroyList);
    }

    @Test
    public void modalTest() {
        app.show("/controller/basic");
        verifyThat("Basic Controller", Node::isVisible);
        sleep(200);

        Modals.showModal(app.stage(), new ModalComponent(), (stage, controller) -> {
            stage.setTitle("Modal");
            stage.setWidth(200);
            stage.setHeight(200);
        });

        waitForFxEvents();

        Modals.ModalStage modal = (Modals.ModalStage) Stage.getWindows().stream().filter(window -> window instanceof Modals.ModalStage).map(window -> (Stage) window).findAny().orElse(null);

        assertNotNull(modal);
        verifyThat("Modal Component", Node::isVisible);
        assertEquals(200, modal.getWidth());
        assertEquals(200, modal.getHeight());
        assertEquals("Modal", modal.getTitle());
    }

}
