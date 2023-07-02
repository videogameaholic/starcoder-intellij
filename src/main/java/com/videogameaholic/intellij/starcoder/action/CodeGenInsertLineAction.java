package com.videogameaholic.intellij.starcoder.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.VirtualFile;
import com.videogameaholic.intellij.starcoder.StarCoderWidget;
import org.jetbrains.annotations.NotNull;

public class CodeGenInsertLineAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (file == null || editor == null) return;

        String[] hints = file.getUserData(StarCoderWidget.STAR_CODER_CODE_SUGGESTION);
        if((hints == null) || (hints.length == 0)) return;

        Caret caret = e.getData(CommonDataKeys.CARET);
        Integer starCoderPos = file.getUserData(StarCoderWidget.STAR_CODER_POSITION);
        int lastPosition = (starCoderPos==null) ? 0 : starCoderPos;
        if((caret == null) || (caret.getOffset() != lastPosition)) return;

        // TODO this technically works but it feels like a bug
        // It should be hints[0]+ rather than +=, but it currently matches in the widget.
        final String insertText = (hints.length > 1) ? hints[0] += "\n" : hints[0];

        WriteCommandAction.runWriteCommandAction(editor.getProject(), "StarCoder Insert", null, () -> {
            editor.getDocument().insertString(lastPosition, insertText);
            editor.getCaretModel().moveToOffset(lastPosition + insertText.length());
        });

    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // Only allow this if there are hints in the userdata.
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (file == null) return;

        String[] hints = file.getUserData(StarCoderWidget.STAR_CODER_CODE_SUGGESTION);
        e.getPresentation().setEnabledAndVisible(hints != null && hints.length > 0);
    }
}
