package com.videogameaholic.intellij.starcoder.action;

import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
import com.intellij.openapi.vfs.VirtualFile;
import com.videogameaholic.intellij.starcoder.settings.StarCoderSettings;
import com.videogameaholic.intellij.starcoder.settings.TabActionOption;
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
        VirtualFile file = dataContext.getData(CommonDataKeys.VIRTUAL_FILE);
        TabActionOption tabActionOption = StarCoderSettings.getInstance().getTabActionOption();

        switch (tabActionOption) {
            case ALL:
                return CodeGenInsertAllAction.performAction(editor, caret, file);
            case SINGLE:
                return CodeGenInsertLineAction.performAction(editor, caret, file);
            default:
                return false;
        }
    }
}
