package io.github.sekassel.jfxframework.app;

import io.github.sekassel.jfxframework.FxFramework;
import javafx.stage.Stage;
import org.testfx.framework.junit5.ApplicationTest;

public class ControllerTest extends ApplicationTest {

    public final FxFramework app = new FxFramework() {
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