package io.github.sekassel.jfxframework.duplicate.duplicators.impl;

import io.github.sekassel.jfxframework.duplicate.duplicators.NodeDuplicator;
import javafx.scene.image.ImageView;

public class ImageViewDuplicator<T extends ImageView> extends NodeDuplicator<T> {

    @Override
    public T duplicate(T imageView) {
        T newImageView = super.duplicate(imageView);

        newImageView.setImage(imageView.getImage());
        newImageView.setX(imageView.getX());
        newImageView.setY(imageView.getY());
        newImageView.setFitWidth(imageView.getFitWidth());
        newImageView.setFitHeight(imageView.getFitHeight());
        newImageView.setPreserveRatio(imageView.isPreserveRatio());
        newImageView.setSmooth(imageView.isSmooth());
        newImageView.setViewport(imageView.getViewport());

        return newImageView;
    }

}
