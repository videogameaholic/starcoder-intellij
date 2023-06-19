package com.videogameaholic.intellij.starcoder;

import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
import com.intellij.openapi.vfs.VirtualFile;
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
        if(!insertCodeSuggestion(editor, dataContext)) {
            myOriginalHandler.execute(editor, caret, dataContext);
        }
    }

    private boolean insertCodeSuggestion(Editor editor, DataContext dataContext) {
        VirtualFile file = dataContext.getData(CommonDataKeys.VIRTUAL_FILE);
        if (file == null) return false;

        String[] hints = file.getUserData(StarCoderWidget.STAR_CODER_CODE_SUGGESTION);
        if((hints == null) || (hints.length == 0)) return false;

        StringJoiner insertTextJoiner = new StringJoiner("\n");
        for (String hint : hints) {
            insertTextJoiner.add(hint);
        }
        String insertText = insertTextJoiner.toString();
        int lastPosition = (file.getUserData(StarCoderWidget.STAR_CODER_POSITION)==null) ? 0 : file.getUserData(StarCoderWidget.STAR_CODER_POSITION);
        WriteCommandAction.runWriteCommandAction(editor.getProject(), () -> {
            editor.getDocument().insertString(lastPosition, insertText);
            editor.getCaretModel().moveToOffset(lastPosition + insertText.length());
        });
        return true;
    }
}
