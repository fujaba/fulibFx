package io.github.sekassel.todo.controller;

import javafx.beans.DefaultProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ListView;

@DefaultProperty("template")
public class For<T> extends ListView<T>
{
	private final ObservableList<Node> template = FXCollections.observableArrayList();
	private final SimpleObjectProperty<T> current = new SimpleObjectProperty<>();

	public ObservableList<Node> getTemplate()
	{
		return template;
	}

	public ObjectProperty<T> current()
	{
		return current;
	}

	public For()
	{
		setCellFactory(param -> new ForCell<>());
	}
}
