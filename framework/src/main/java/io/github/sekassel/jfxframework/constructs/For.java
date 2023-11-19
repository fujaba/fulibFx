package io.github.sekassel.jfxframework.constructs;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.Parent;


public class For<T> {

    private SimpleObjectProperty<ObservableList<T>> list = new SimpleObjectProperty<>();
    private StringProperty item = new SimpleStringProperty();
    private Parent container;

    public For() {
        item.addListener((observable, oldValue, newValue) -> {
            System.out.println("Item changed from " + oldValue + " to " + newValue);
        });
        list.addListener((observable, oldValue, newValue) -> {
            System.out.println("List changed from " + oldValue + " to " + newValue);
        });
    }

    public Parent getContainer() {
        return container;
    }

    public void setContainer(Parent container) {
        this.container = container;
    }

    public SimpleObjectProperty<ObservableList<T>> listProperty() {
        return list;
    }

    public ObservableList<T> getList() {
        return list.getValue();
    }

    public void setList(ObservableList<T> list) {
        this.list.setValue(list);
    }

    public StringProperty itemProperty() {
        return item;
    }

    public String getItem() {
        return item.getValue();
    }

    public void setItem(String item) {
        this.item.setValue(item);
    }


}
