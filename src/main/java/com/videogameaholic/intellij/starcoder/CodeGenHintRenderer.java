package com.videogameaholic.intellij.starcoder;

import com.intellij.codeInsight.daemon.impl.HintRenderer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.TextAttributes;
import org.jetbrains.annotations.Nullable;

public class CodeGenHintRenderer extends HintRenderer {
    public CodeGenHintRenderer(@Nullable String text) {
        super(text);
    }

    @Override
    protected TextAttributes getTextAttributes(Editor editor) {
        // TODO custom color schemes?
        TextAttributes newAttributes = new TextAttributes();
        newAttributes.copyFrom(editor.getColorsScheme().getAttributes(DefaultLanguageHighlighterColors.LINE_COMMENT));
        return newAttributes;
    }

}
