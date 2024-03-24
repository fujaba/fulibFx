package org.fulib.fx.app;

import javafx.application.Platform;
import org.fulib.fx.FulibFxApp;
import org.fulib.fx.app.controller.InvalidParamController;
import org.fulib.fx.app.controller.types.BasicComponent;
import org.fulib.fx.app.controller.ParamController;
import org.fulib.fx.app.controller.TitleController;
import org.fulib.fx.app.controller.history.AController;
import org.fulib.fx.app.controller.history.BController;
import org.fulib.fx.app.controller.history.CController;
import org.fulib.fx.app.controller.ModalComponent;
import org.fulib.fx.app.controller.subcomponent.basic.ButtonSubComponent;
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

    public static void runAndWait(Runnable runnable) {
        Platform.runLater(runnable);
        waitForFxEvents();
    }

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

    @Test
    public void title() {
        runAndWait(() -> app.show(new TitleController()));
        assertEquals("Title", app.stage().getTitle());
    }

    /**
     * Tests if the framework is able to load the routes and different kinds of controllers.
     */
    @Test
    public void controllerTypes() {
        runAndWait(() -> app.show("/controller/basic"));
        verifyThat("Basic Controller", Node::isVisible);
        sleep(200);

        runAndWait(() -> app.show("/controller/method"));
        verifyThat("Method Controller", Node::isVisible);
        sleep(200);

        runAndWait(() -> app.show("/controller/view"));
        verifyThat("View Controller", Node::isVisible);
        sleep(200);

        runAndWait(() -> app.show("/component/basic"));
        verifyThat("Basic Component", Node::isVisible);
        sleep(200);

        runAndWait(() -> app.show("/component/root"));
        verifyThat("Root Component", Node::isVisible);
        sleep(200);

        runAndWait(() -> app.show("../root"));
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
        runAndWait(() -> app.show("/controller/withsubcomponent"));
        sleep(200);
        ButtonSubComponent button = lookup("#buttonSubComponent").query();
        verifyThat(button, Node::isVisible);
        assertEquals(100, button.getMinWidth());
    }

    @Test
    public void routeSubComponent() {
        assertThrows(IllegalArgumentException.class, () -> app.initAndRender("/controller/view"));

        runAndWait(() -> assertEquals(BasicComponent.class, app.initAndRender("/component/basic").getClass()));
    }

    /**
     * Tests if the framework can load a controller with a for loop and if the loop is correctly executed.
     */
    @Test
    public void simpleForTest() {
        ObservableList<String> list = FXCollections.observableList(new ArrayList<>());
        FulibFxApp.FX_SCHEDULER.scheduleDirect(() -> {
            list.add("Hello");
            list.add("World");
            list.add("!");
        });

        runAndWait(() -> app.show("/controller/for", Map.of("list", list)));
        verifyThat("#container", Node::isVisible);

        VBox container = lookup("#container").queryAs(VBox.class);
        assertEquals(3, container.getChildren().size());

        FulibFxApp.FX_SCHEDULER.scheduleDirect(() -> list.remove("World"));
        waitForFxEvents();

        assertEquals(2, container.getChildren().size());

        FulibFxApp.FX_SCHEDULER.scheduleDirect(() -> list.add(1, "World"));
        waitForFxEvents();

        assertEquals(3, container.getChildren().size());
    }

    @Test
    public void testSubOrder() {
        List<String> initList = new ArrayList<>();
        List<String> renderList = new ArrayList<>();
        List<String> destroyList = new ArrayList<>();

        runAndWait(() -> app.show("/ordertest/main", Map.of("initList", initList, "renderList", renderList, "destroyList", destroyList)));

        assertEquals(List.of("main", "sub", "subsub", "othersubsub"), initList);
        assertEquals(List.of("subsub", "othersubsub", "sub", "main"), renderList);
        assertEquals(List.of(), destroyList);

        runAndWait(() -> app.show("/controller/basic"));

        assertEquals(List.of("othersubsub", "subsub", "sub", "main"), destroyList);
    }

    @Test
    public void modalTest() {
        runAndWait(() -> app.show("/controller/basic"));
        verifyThat("Basic Controller", Node::isVisible);
        sleep(200);

        Modals.showModal(app, new ModalComponent(), (stage, controller) -> {
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

    @Test
    public void params() {
        ParamController controller = new ParamController();
        Map<String, Object> params = Map.of(
                "integer", 1,
                "string", "string",
                "character", 'a',
                "bool", true
        );
        runAndWait(() -> app.show(controller, params));

        assertEquals(1, controller.getOnInitParam());
        assertEquals("string", controller.getSetterParam());
        assertEquals(1, controller.getFieldParam());

        assertEquals("string", controller.fieldPropertyParamProperty().get());

        assertEquals(Map.of("integer", 1, "string", "string", "character", 'a', "bool", true), controller.getOnInitParamsMap());
        assertEquals(Map.of("integer", 1, "string", "string", "character", 'a', "bool", true), controller.getSetterParamsMap());
        assertEquals(Map.of("integer", 1, "string", "string", "character", 'a', "bool", true), controller.getFieldParamsMap());

        assertEquals('a', controller.getSetterMultiParams1());
        assertEquals(true, controller.getSetterMultiParams2());

        runAndWait(() ->
                assertThrows(
                        RuntimeException.class, // Fails because the field is of type Integer, but a String is provided
                        () -> app.show(new InvalidParamController(), Map.of("one", "string"))
                )
        );

        runAndWait(() ->
                assertThrows(
                        RuntimeException.class, // Fails because the property expects an Integer, but a String is provided
                        () -> app.show(new InvalidParamController(), Map.of("two", "123"))
                )
        );

        runAndWait(() ->
                assertThrows(
                        RuntimeException.class, // Fails because the property is null
                        () -> app.show(new InvalidParamController(), Map.of("three", 123))
                )
        );
    }

    @Test
    public void history() {
        runAndWait(() -> app.show(new AController(), Map.of("string", "a")));
        verifyThat("A:a", Node::isVisible);

        runAndWait(() -> app.show(new BController(), Map.of("string", "b")));
        verifyThat("B:b", Node::isVisible);

        runAndWait(() -> app.show(new CController(), Map.of("string", "c")));
        verifyThat("C:c", Node::isVisible);

        runAndWait(app::back);
        verifyThat("B:b", Node::isVisible);

        runAndWait(app::back);
        verifyThat("A:a", Node::isVisible);

        runAndWait(app::forward);
        verifyThat("B:b", Node::isVisible);

        runAndWait(app::forward);
        verifyThat("C:c", Node::isVisible);

        runAndWait(app::forward);
        verifyThat("C:c", Node::isVisible); // should not change

        runAndWait(app::back);
        verifyThat("B:b", Node::isVisible);

        FulibFxApp.FX_SCHEDULER.scheduleDirect(app::refresh);
        waitForFxEvents();
        verifyThat("B:b", Node::isVisible);
    }

}