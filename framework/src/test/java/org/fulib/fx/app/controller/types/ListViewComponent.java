package org.fulib.fx.app.controller.types;

import javafx.scene.control.ListView;
import org.fulib.fx.annotation.controller.Component;

// https://github.com/fujaba/fulibFx/issues/113
@Component(view = "ListView.fxml")
public class ListViewComponent<T> extends ListView<T> {
}
