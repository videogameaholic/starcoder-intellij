package com.videogameaholic.intellij.starcoder.action;

import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
import com.intellij.openapi.vfs.VirtualFile;
import com.videogameaholic.intellij.starcoder.StarCoderWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.StringJoiner;

public class CodeGenInsertAction extends EditorWriteActionHandler {

    protected final EditorActionHandler myOriginalHandler;

    public CodeGenInsertAction(EditorActionHandler originalHandler) {
        myOriginalHandler = originalHandler;
    }

    @Override
    public void executeWriteAction(@NotNull Editor editor, @Nullable Caret caret, DataContext dataContext) {
        if(!insertCodeSuggestion(editor, caret, dataContext)) {
            myOriginalHandler.execute(editor, caret, dataContext);
        }
    }

    private boolean insertCodeSuggestion(Editor editor, @Nullable Caret caret, DataContext dataContext) {
        VirtualFile file = dataContext.getData(CommonDataKeys.VIRTUAL_FILE);
        if (file == null) return false;

        String[] hints = file.getUserData(StarCoderWidget.STAR_CODER_CODE_SUGGESTION);
        if((hints == null) || (hints.length == 0)) return false;

        StringJoiner insertTextJoiner = new StringJoiner("\n");
        for (String hint : hints) {
            insertTextJoiner.add(hint);
        }

        Integer starCoderPos = file.getUserData(StarCoderWidget.STAR_CODER_POSITION);
        int lastPosition = (starCoderPos==null) ? 0 : starCoderPos;
        if((caret == null) || (caret.getOffset() != lastPosition)) return false;

        file.putUserData(StarCoderWidget.STAR_CODER_CODE_SUGGESTION, null);

        String insertText = insertTextJoiner.toString();
        WriteCommandAction.runWriteCommandAction(editor.getProject(), "StarCoder Insert", null, () -> {
            editor.getDocument().insertString(lastPosition, insertText);
            editor.getCaretModel().moveToOffset(lastPosition + insertText.length());
        });
        return true;
    }
}
