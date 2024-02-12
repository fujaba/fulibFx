package org.fulib.fx.app;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.VBox;
import org.fulib.fx.annotation.controller.Controller;
import org.fulib.fx.annotation.event.onInit;
import org.fulib.fx.annotation.param.Param;
import org.fulib.fx.annotation.param.Params;

import javax.inject.Inject;
import java.util.Map;

@Controller(view = "#paramView")
public class ParamController {


    private int onInitParam;
    private String setterParam;
    @Param("integer")
    private int fieldParam;

    @Param("string")
    private final StringProperty fieldPropertyParam = new SimpleStringProperty();

    private Map<String, Object> onInitParams;
    private Map<String, Object> setterParams;
    @Params
    private Map<String, Object> fieldParams;

    private Map<String, Object> onInitPartialParams;
    private Character setterPartialParams1;
    private Boolean setterPartialParams2;
    @Params({"string", "integer"})
    private Map<String, Object> fieldPartialParams;

    @Inject
    public ParamController() {
    }

    @Param("string")
    public void setParam(String string) {
        this.setterParam = string;
    }

    @Params({"character", "bool"})
    public void setParams(Character character, Boolean bool) {
        this.setterPartialParams1 = character;
        this.setterPartialParams2 = bool;
    }

    @Params
    public void setParams(Map<String, Object> map) {
        this.setterParams = map;
    }

    @onInit
    public void init(@Param("integer") Integer integer, @Params Map<String, Object> map, @Params({"string", "integer"}) Map<String, Object> partialMap) {
        this.onInitParam = integer;
        this.onInitParams = map;
        this.onInitPartialParams = partialMap;
    }

    public VBox paramView() {
        return new VBox();
    }

    public int getOnInitParam() {
        return onInitParam;
    }

    public String getSetterParam() {
        return setterParam;
    }

    public int getFieldParam() {
        return fieldParam;
    }

    public String getFieldPropertyParam() {
        return fieldPropertyParam.get();
    }

    public StringProperty fieldPropertyParamProperty() {
        return fieldPropertyParam;
    }

    public Map<String, Object> getOnInitParams() {
        return onInitParams;
    }

    public Map<String, Object> getSetterParams() {
        return setterParams;
    }

    public Map<String, Object> getFieldParams() {
        return fieldParams;
    }

    public Map<String, Object> getOnInitPartialParams() {
        return onInitPartialParams;
    }

    public Character getSetterPartialParams1() {
        return setterPartialParams1;
    }

    public Boolean getSetterPartialParams2() {
        return setterPartialParams2;
    }

    public Map<String, Object> getFieldPartialParams() {
        return fieldPartialParams;
    }

}
