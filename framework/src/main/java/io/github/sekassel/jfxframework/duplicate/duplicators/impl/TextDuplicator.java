package io.github.sekassel.jfxframework.duplicate.duplicators.impl;

import io.github.sekassel.jfxframework.duplicate.duplicators.ShapeDuplicator;
import javafx.scene.text.Text;

public class TextDuplicator<T extends Text> extends ShapeDuplicator<T> {

    @Override
    public T duplicate(T text) {
        T newText = super.duplicate(text);

        newText.setText(text.getText());
        newText.setX(text.getX());
        newText.setY(text.getY());
        newText.setFont(text.getFont());
        newText.setTextOrigin(text.getTextOrigin());
        newText.setBoundsType(text.getBoundsType());
        newText.setWrappingWidth(text.getWrappingWidth());
        newText.setUnderline(text.isUnderline());
        newText.setStrikethrough(text.isStrikethrough());
        newText.setTextAlignment(text.getTextAlignment());
        newText.setLineSpacing(text.getLineSpacing());
        newText.setFontSmoothingType(text.getFontSmoothingType());
        newText.setSelectionStart(text.getSelectionStart());
        newText.setSelectionEnd(text.getSelectionEnd());
        newText.setSelectionFill(text.getSelectionFill());
        newText.setCaretPosition(text.getCaretPosition());
        newText.setCaretBias(text.isCaretBias());
        newText.setTabSize(text.getTabSize());

        return newText;
    }


}
