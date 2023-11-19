package io.github.sekassel.jfxframework.constructs;

import javafx.collections.ObservableList;
import javafx.scene.Parent;


public class For<T> {


    private ObservableList<T> list;
    private Parent container;

    public For() {
    }

    public Parent getContainer() {
        return container;
    }

    public void setContainer(Parent container) {
        this.container = container;
    }

    public ObservableList<T> getList() {
        return list;
    }

    public void setList(ObservableList<T> list) {
        this.list = list;
    }

    public void initialize() {
        System.out.println("init");
    }


}
