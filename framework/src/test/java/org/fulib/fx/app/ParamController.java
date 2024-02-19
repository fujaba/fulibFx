package org.fulib.fx.app;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.VBox;
import org.fulib.fx.annotation.controller.Controller;
import org.fulib.fx.annotation.event.onInit;
import org.fulib.fx.annotation.param.Param;
import org.fulib.fx.annotation.param.Params;
import org.fulib.fx.annotation.param.ParamsMap;

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

    private Map<String, Object> onInitParamsMap;
    private Map<String, Object> setterParamsMap;
    @ParamsMap
    private Map<String, Object> fieldParamsMap;

    private Character setterMultiParams1;
    private Boolean setterMultiParams2;

    @Inject
    public ParamController() {
    }

    @Param("string")
    public void setParam(String string) {
        this.setterParam = string;
    }

    @Params({"character", "bool"})
    public void setParams(Character character, Boolean bool) {
        this.setterMultiParams1 = character;
        this.setterMultiParams2 = bool;
    }

    @ParamsMap
    public void setParams(Map<String, Object> map) {
        this.setterParamsMap = map;
    }

    @onInit
    public void init(@Param("integer") Integer integer, @ParamsMap Map<String, Object> map) {
        this.onInitParam = integer;
        this.onInitParamsMap = map;
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

    public Map<String, Object> getOnInitParamsMap() {
        return onInitParamsMap;
    }

    public Map<String, Object> getSetterParamsMap() {
        return setterParamsMap;
    }

    public Map<String, Object> getFieldParamsMap() {
        return fieldParamsMap;
    }

    public Character getSetterMultiParams1() {
        return setterMultiParams1;
    }

    public Boolean getSetterMultiParams2() {
        return setterMultiParams2;
    }

}
