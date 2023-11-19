package io.github.sekassel.jfxframework.constructs;

import io.github.sekassel.jfxframework.FxFramework;
import io.github.sekassel.jfxframework.controller.annotation.Controller;
import io.github.sekassel.jfxframework.util.Util;
import javafx.beans.DefaultProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

import java.util.Map;


@DefaultProperty("template")
public class VFor extends VBox {

    Class<?> item;

    public void setItem(Class<?> item) {
        this.item = item;
    }

    public Class<?> getItem() {
        return item;
    }


    // Property containing the template(s) to be used for each item in the list
    private final ObjectProperty<ObservableList<Node>> template = new SimpleObjectProperty<>(this, "template", FXCollections.observableArrayList());

    // Listener to the list property to update the children when the list changes
    private final ListChangeListener<Object> listChangeListener = change -> {
        while (change.next()) {
            if (change.wasAdded()) {
                for (Object item : change.getAddedSubList()) {
                    displayItem(item);
                }
            }
            // Handle removals or updates if needed
        }
    };

    // Property containing all the items to be displayed
    private ObjectProperty<ObservableList<?>> listProperty;

    public VFor() {
        listProperty().addListener((obs, oldList, newList) -> {
            if (oldList != null) {
                oldList.removeListener(listChangeListener);
            }
            if (newList != null) {
                newList.addListener(listChangeListener);
                initItems(newList);
            }
        });
    }

    public ObservableList<Node> getTemplate() {
        return template.get();
    }

    public void setTemplate(ObservableList<Node> template) {
        this.template.set(template);
    }

    public ObjectProperty<ObservableList<Node>> templateProperty() {
        return template;
    }

    public ObjectProperty<ObservableList<?>> listProperty() {
        if (listProperty == null) {
            listProperty = new SimpleObjectProperty<>(this, "list");
        }
        return listProperty;
    }

    public ObservableList<?> getList() {
        return listProperty().get();
    }

    public void setList(ObservableList<?> list) {
        listProperty().set(list);
    }

    private void initItems(ObservableList<?> newList) {
        getChildren().clear();
        for (Object item : newList) {
            displayItem(item);
        }
    }

    private void displayItem(Object item) {
        for (Node node : template.get()) {
            Node newNode = deepCloneNode(node);
            System.out.println(node);
            System.out.println(newNode);
            getChildren().add(newNode);
        }
    }

    // Implement a method to deep clone a Node
    private Node deepCloneNode(Node node) {
        if (!node.getClass().isAnnotationPresent(Controller.class)) return null;
        Node controller = (Node) FxFramework.router().getProvidedInstance(node.getClass());
        return FxFramework.router().initAndRender(controller, Map.of()); // TODO: Pass the item to the controller
    }
}