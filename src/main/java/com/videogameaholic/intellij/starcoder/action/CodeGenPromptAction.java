package com.videogameaholic.intellij.starcoder.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.videogameaholic.intellij.starcoder.StarCoderService;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public class CodeGenPromptAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor focusedEditor =e.getDataContext().getData(CommonDataKeys.EDITOR);
        if(focusedEditor == null) return;

        int selectionStart = focusedEditor.getSelectionModel().getSelectionStart();
        int selectionEnd = focusedEditor.getSelectionModel().getSelectionEnd();
        String selectedText = focusedEditor.getCaretModel().getCurrentCaret().getSelectedText();
        if(StringUtils.isEmpty(selectedText)) return;

        StarCoderService starCoder = ApplicationManager.getApplication().getService(StarCoderService.class);
        String replacementSuggestion = starCoder.replacementSuggestion(selectedText);
        WriteCommandAction.runWriteCommandAction(focusedEditor.getProject(),"StarCoder Insert", null, () ->
                focusedEditor.getDocument().replaceString(selectionStart, selectionEnd, replacementSuggestion));
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // Only show the action if there is selected text in the editor
        String selection = "";
        Editor focusedEditor =e.getDataContext().getData(CommonDataKeys.EDITOR);
        if(focusedEditor != null) {
            selection = focusedEditor.getCaretModel().getCurrentCaret().getSelectedText();
        }
        e.getPresentation().setEnabledAndVisible(!StringUtils.isEmpty(selection));
    }
}
