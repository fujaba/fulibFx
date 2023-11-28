package io.github.sekassel.jfxframework.duplicate.duplicators;

import javafx.scene.shape.Shape;

public abstract class ShapeDuplicator<T extends Shape> extends NodeDuplicator<T> {

    @Override
    public T duplicate(T shape) {
        T newShape = super.duplicate(shape);

        newShape.setStrokeType(shape.getStrokeType());
        newShape.setFill(shape.getFill());
        newShape.setStroke(shape.getStroke());
        newShape.setSmooth(shape.isSmooth());
        newShape.setStrokeWidth(shape.getStrokeWidth());
        newShape.setStrokeLineJoin(shape.getStrokeLineJoin());
        newShape.setStrokeLineCap(shape.getStrokeLineCap());
        newShape.setStrokeType(shape.getStrokeType());
        newShape.setStrokeMiterLimit(shape.getStrokeMiterLimit());
        newShape.setStrokeDashOffset(shape.getStrokeDashOffset());

        shape.getStrokeDashArray().stream().filter(value -> !newShape.getStrokeDashArray().contains(value)).forEach(newShape.getStrokeDashArray()::add);

        return newShape;
    }


}
