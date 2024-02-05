package org.fulib.fx.app;

import org.fulib.fx.FulibFxApp;
import javafx.stage.Stage;
import org.testfx.framework.junit5.ApplicationTest;

public class ControllerTest extends ApplicationTest {

    public final FulibFxApp app = new FulibFxApp() {
        @Override
        public void start(Stage stage) {
            super.start(stage);
        }
    };

    @Override
    public void start(Stage stage) throws Exception {
        super.start(stage);
        stage.requestFocus();
    }
}
