package com.videogameaholic.intellij.starcoder.action;

import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CodeGenTabAction  extends EditorWriteActionHandler {
    protected final EditorActionHandler myOriginalHandler;

    public CodeGenTabAction(EditorActionHandler originalHandler) {
        myOriginalHandler = originalHandler;
    }

    @Override
    public void executeWriteAction(@NotNull Editor editor, @Nullable Caret caret, DataContext dataContext) {
        if(!insertCodeSuggestion(editor, caret, dataContext)) {
            myOriginalHandler.execute(editor, caret, dataContext);
        }
    }

    private boolean insertCodeSuggestion(Editor editor, Caret caret, DataContext dataContext) {
        // TODO Check settings to determine tab action (single or all)
        VirtualFile file = dataContext.getData(CommonDataKeys.VIRTUAL_FILE);

        return CodeGenInsertAllAction.performAction(editor, caret, file);
    }
}
