package org.fulib.fx.app.controller.subcomponent.order;

import org.fulib.fx.annotation.controller.Component;
import org.fulib.fx.annotation.controller.SubComponent;
import org.fulib.fx.annotation.event.OnDestroy;
import org.fulib.fx.annotation.event.OnInit;
import org.fulib.fx.annotation.event.OnRender;
import org.fulib.fx.annotation.param.Param;
import javafx.scene.layout.VBox;

import javax.inject.Inject;
import java.util.List;

@Component
public class SubController extends VBox {

    @Param("destroyList")
    List<String> destroyList;

    @Inject
    @SubComponent
    SubSubController subSubController;

    @Inject
    @SubComponent
    OtherSubSubController otherSubSubController;

    @Inject
    public SubController() {
    }

    @OnInit()
    public void init(@Param("initList") List<String> initList) {
        initList.add("sub");
    }

    @OnRender()
    public void render(@Param("renderList") List<String> renderList) {
        renderList.add("sub");
    }

    @OnDestroy()
    public void destroy() {
        destroyList.add("sub");
    }


}
