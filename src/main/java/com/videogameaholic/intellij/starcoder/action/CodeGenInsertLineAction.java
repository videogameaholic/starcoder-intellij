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
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        Caret caret = e.getData(CommonDataKeys.CARET);
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if(!performAction(editor, caret, file)) {
            // TODO log?
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // Only allow this if there are hints in the userdata.
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (file == null) return;

        String[] hints = file.getUserData(StarCoderWidget.STAR_CODER_CODE_SUGGESTION);
        e.getPresentation().setEnabledAndVisible(hints != null && hints.length > 0);
    }

    public static boolean performAction(Editor editor, Caret caret, VirtualFile file) {
        if (file == null || editor == null) return false;

        String[] hints = file.getUserData(StarCoderWidget.STAR_CODER_CODE_SUGGESTION);
        if((hints == null) || (hints.length == 0)) return false;

        Integer starCoderPos = file.getUserData(StarCoderWidget.STAR_CODER_POSITION);
        int lastPosition = (starCoderPos==null) ? 0 : starCoderPos;
        if((caret == null) || (caret.getOffset() != lastPosition)) return false;

        final String insertText = hints[0];
        WriteCommandAction.runWriteCommandAction(editor.getProject(), "StarCoder Insert", null, () -> {
            editor.getDocument().insertString(lastPosition, insertText);
            editor.getCaretModel().moveToOffset(lastPosition + insertText.length());
        });
        return true;
    }
}
