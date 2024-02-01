package org.fulib.fx.duplicate.duplicators;

import org.fulib.fx.duplicate.Duplicator;
import javafx.scene.Node;

import java.lang.reflect.InvocationTargetException;

public abstract class NodeDuplicator<T extends Node> implements Duplicator<T>
{

    @Override
    public T duplicate(T node) {
        T newNode = newNode(node);

        newNode.setAccessibleHelp(node.getAccessibleHelp());
        newNode.setAccessibleRole(node.getAccessibleRole());
        newNode.setAccessibleText(node.getAccessibleText());
        newNode.setAccessibleRoleDescription(node.getAccessibleRoleDescription());

        newNode.setBlendMode(node.getBlendMode());

        newNode.setCache(node.isCache());
        newNode.setClip(node.getClip());
        newNode.setCursor(node.getCursor());
        newNode.setCacheHint(node.getCacheHint());

        newNode.setDepthTest(node.getDepthTest());
        newNode.setDisable(node.isDisable());

        newNode.setEffect(node.getEffect());
        newNode.setEventDispatcher(node.getEventDispatcher());

        newNode.setFocusTraversable(node.isFocusTraversable());

        newNode.setId(node.getId());
        newNode.setInputMethodRequests(node.getInputMethodRequests());

        newNode.setLayoutX(node.getLayoutX());
        newNode.setLayoutY(node.getLayoutY());

        newNode.setManaged(node.isManaged());
        newNode.setMouseTransparent(node.isMouseTransparent());

        newNode.setNodeOrientation(node.getNodeOrientation());

        newNode.setOnDragDetected(node.getOnDragDetected());
        newNode.setOnDragDone(node.getOnDragDone());
        newNode.setOnDragDropped(node.getOnDragDropped());
        newNode.setOnDragEntered(node.getOnDragEntered());
        newNode.setOnDragExited(node.getOnDragExited());
        newNode.setOnDragOver(node.getOnDragOver());
        newNode.setOnInputMethodTextChanged(node.getOnInputMethodTextChanged());
        newNode.setOnKeyPressed(node.getOnKeyPressed());
        newNode.setOnKeyReleased(node.getOnKeyReleased());
        newNode.setOnKeyTyped(node.getOnKeyTyped());
        newNode.setOnMouseClicked(node.getOnMouseClicked());
        newNode.setOnMouseDragEntered(node.getOnMouseDragEntered());
        newNode.setOnMouseDragExited(node.getOnMouseDragExited());
        newNode.setOnMouseDragged(node.getOnMouseDragged());
        newNode.setOnMouseDragOver(node.getOnMouseDragOver());
        newNode.setOnMouseDragReleased(node.getOnMouseDragReleased());
        newNode.setOnMouseEntered(node.getOnMouseEntered());
        newNode.setOnMouseExited(node.getOnMouseExited());
        newNode.setOnMouseMoved(node.getOnMouseMoved());
        newNode.setOnMousePressed(node.getOnMousePressed());
        newNode.setOnMouseReleased(node.getOnMouseReleased());
        newNode.setOnRotate(node.getOnRotate());
        newNode.setOnRotationFinished(node.getOnRotationFinished());
        newNode.setOnRotationStarted(node.getOnRotationStarted());
        newNode.setOnScroll(node.getOnScroll());
        newNode.setOnScrollFinished(node.getOnScrollFinished());
        newNode.setOnScrollStarted(node.getOnScrollStarted());
        newNode.setOnSwipeDown(node.getOnSwipeDown());
        newNode.setOnSwipeLeft(node.getOnSwipeLeft());
        newNode.setOnSwipeRight(node.getOnSwipeRight());
        newNode.setOnSwipeUp(node.getOnSwipeUp());
        newNode.setOnTouchMoved(node.getOnTouchMoved());
        newNode.setOnTouchPressed(node.getOnTouchPressed());
        newNode.setOnTouchReleased(node.getOnTouchReleased());
        newNode.setOnTouchStationary(node.getOnTouchStationary());
        newNode.setOnZoom(node.getOnZoom());
        newNode.setOnZoomFinished(node.getOnZoomFinished());
        newNode.setOnZoomStarted(node.getOnZoomStarted());

        newNode.setPickOnBounds(node.isPickOnBounds());

        newNode.setRotate(node.getRotate());
        newNode.setRotationAxis(node.getRotationAxis());
        newNode.setScaleX(node.getScaleX());
        newNode.setScaleY(node.getScaleY());
        newNode.setScaleZ(node.getScaleZ());
        newNode.setStyle(node.getStyle());

        newNode.setTranslateX(node.getTranslateX());
        newNode.setTranslateY(node.getTranslateY());
        newNode.setTranslateZ(node.getTranslateZ());

        newNode.setUserData(node.getUserData());

        newNode.setViewOrder(node.getViewOrder());
        newNode.setVisible(node.isVisible());

        return newNode;
    }


    @SuppressWarnings("unchecked")
    private T newNode(Node node) {
        try {
            return (T) node.getClass().getConstructor().newInstance();
        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
