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
//        TextAttributes newAttributes = editor.getColorsScheme().getAttributes(DefaultLanguageHighlighterColors.INLINE_PARAMETER_HINT);
//        TextAttributes newAttributes = editor.getColorsScheme().getAttributes(DefaultLanguageHighlighterColors.LINE_COMMENT);
        TextAttributes newAttributes = editor.getColorsScheme().getAttributes(DefaultLanguageHighlighterColors.INLAY_DEFAULT);
//        newAttributes.setFontType(Font.ITALIC);
        return newAttributes;
    }

}
