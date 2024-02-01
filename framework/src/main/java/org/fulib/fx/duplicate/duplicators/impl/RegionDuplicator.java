package org.fulib.fx.duplicate.duplicators.impl;

import org.fulib.fx.duplicate.duplicators.ParentDuplicator;
import javafx.scene.layout.Region;

public class RegionDuplicator<T extends Region> extends ParentDuplicator<T>
{

        @Override
        public T duplicate(T region) {
            T newRegion = super.duplicate(region);

            newRegion.setSnapToPixel(region.isSnapToPixel());
            newRegion.setPadding(region.getPadding());
            newRegion.setBackground(region.getBackground());
            newRegion.setBorder(region.getBorder());
            newRegion.setOpaqueInsets(region.getOpaqueInsets());
            region.setMinWidth(region.getMinWidth());
            region.setMinHeight(region.getMinHeight());
            region.setPrefWidth(region.getPrefWidth());
            region.setPrefHeight(region.getPrefHeight());
            region.setMaxWidth(region.getMaxWidth());
            region.setMaxHeight(region.getMaxHeight());
            region.setShape(region.getShape());
            region.setScaleShape(region.isScaleShape());
            region.setCenterShape(region.isCenterShape());
            region.setCacheShape(region.isCacheShape());

            return newRegion;
        }
}
