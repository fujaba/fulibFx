package io.github.sekassel.todo.controller;

import io.github.sekassel.jfxframework.controller.ControllerEvent;
import io.github.sekassel.jfxframework.controller.annotation.Controller;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import javax.inject.Inject;

@Controller(id = "todo")
public class TodoController extends VBox
{
	@FXML
	private TextField titleField;
	@FXML
	private TextArea descriptionField;

	public final StringProperty title = new SimpleStringProperty();
	public final StringProperty description = new SimpleStringProperty();

	@Inject
	public TodoController()
	{
	}

	@ControllerEvent.onRender()
	public void render()
	{
		// TODO bidirectional binding not possible with FXML right now
		titleField.textProperty().bindBidirectional(title);
		descriptionField.textProperty().bindBidirectional(description);
	}
}
