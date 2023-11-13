package io.github.sekassel.todo.controller;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ListCell;

public class ForCell<T> extends ListCell<T>
{

	@Override
	protected void updateItem(T item, boolean empty)
	{
		super.updateItem(item, empty);
		final For<T> tFor = (For<T>) this.getListView();
		if (item != null)
		{
			tFor.current().set(item);
			for (Node node : tFor.getTemplate())
			{
				this.getChildren().add(node.);
			}
		}
		else
		{
			this.getChildren().clear();
		}
	}
}
