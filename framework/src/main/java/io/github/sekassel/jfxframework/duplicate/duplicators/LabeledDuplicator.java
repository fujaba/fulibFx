package io.github.sekassel.jfxframework.duplicate.duplicators;

import javafx.scene.control.Labeled;

public abstract class LabeledDuplicator<T extends Labeled> extends ControlDuplicator<T> {

    @Override
    public T duplicate(T labeled) {
        T newLabeled = super.duplicate(labeled);

        newLabeled.setText(labeled.getText());
        newLabeled.setAlignment(labeled.getAlignment());
        newLabeled.setTextAlignment(labeled.getTextAlignment());
        newLabeled.setTextOverrun(labeled.getTextOverrun());
        newLabeled.setEllipsisString(labeled.getEllipsisString());
        newLabeled.setWrapText(labeled.isWrapText());
        newLabeled.setFont(labeled.getFont());
        newLabeled.setGraphic(labeled.getGraphic());
        newLabeled.setUnderline(labeled.isUnderline());
        newLabeled.setLineSpacing(labeled.getLineSpacing());
        newLabeled.setContentDisplay(labeled.getContentDisplay());
        newLabeled.setGraphicTextGap(labeled.getGraphicTextGap());
        newLabeled.setTextFill(labeled.getTextFill());
        newLabeled.setMnemonicParsing(labeled.isMnemonicParsing());

        return newLabeled;
    }

}
