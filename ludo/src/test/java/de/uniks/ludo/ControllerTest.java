package de.uniks.ludo;

import javafx.stage.Stage;
import org.fulib.fx.controller.Subscriber;
import org.mockito.Spy;
import org.testfx.framework.junit5.ApplicationTest;

import java.util.Locale;
import java.util.ResourceBundle;

public class ControllerTest extends ApplicationTest {

    @Spy
    protected App app = new App();

    @Spy
    Subscriber subscriber;

    @Spy
    ResourceBundle bundle = ResourceBundle.getBundle(
            "de/uniks/ludo/lang/lang",
            Locale.ENGLISH
    );

    protected Stage stage;

    @Override
    public void start(Stage stage) throws Exception {
        super.start(stage);
        this.stage = stage;
        stage.setX(0);
        stage.setY(0);
        stage.requestFocus();
        app.start(stage);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        app.stop();
        app = null;
        stage = null;
    }
}
