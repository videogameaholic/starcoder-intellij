package com.videogameaholic.intellij.starcoder;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.event.*;
import com.intellij.openapi.editor.impl.EditorComponentImpl;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
import com.intellij.util.Consumer;
import com.intellij.util.ui.update.MergingUpdateQueue;
import com.intellij.util.ui.update.Update;
import com.videogameaholic.intellij.starcoder.settings.StarCoderSettings;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;

public class StarCoderWidget extends EditorBasedWidget
implements StatusBarWidget.Multiframe, StatusBarWidget.IconPresentation,
        CaretListener, SelectionListener, BulkAwareDocumentListener.Simple, PropertyChangeListener {
    public static final String ID = "StarCoderWidget";

    public static final Key<String[]> STAR_CODER_CODE_SUGGESTION = new Key<>("StarCoder Code Suggestion");
    public static final Key<Integer> STAR_CODER_POSITION = new Key<>("StarCoder Position");

    private MergingUpdateQueue serviceQueue;

    protected StarCoderWidget(@NotNull Project project) {
        super(project);
    }

    @Override
    public @NonNls @NotNull String ID() {
        return ID;
    }

    @Override
    public StatusBarWidget copy() {
        return new StarCoderWidget(getProject());
    }

    @Override
    public @Nullable Icon getIcon() {
        StarCoderService starCoder = ApplicationManager.getApplication().getService(StarCoderService.class);
        StarCoderStatus status = StarCoderStatus.getStatusByCode(starCoder.getStatus());
        if(status == StarCoderStatus.OK) {
            return StarCoderSettings.getInstance().isSaytEnabled() ? StarCoderIcons.WidgetEnabled : StarCoderIcons.WidgetDisabled;
        } else {
            return StarCoderIcons.WidgetError;
        }
    }

    @Override
    public WidgetPresentation getPresentation() {
        return this;
    }

    @Override
    public @Nullable @NlsContexts.Tooltip String getTooltipText() {
        StringBuilder toolTipText = new StringBuilder("StarCoder");
        if(StarCoderSettings.getInstance().isSaytEnabled()) {
            toolTipText.append(" enabled");
        } else {
            toolTipText.append(" disabled");
        }

        StarCoderService starCoder = ApplicationManager.getApplication().getService(StarCoderService.class);
        int statusCode = starCoder.getStatus();
        StarCoderStatus status = StarCoderStatus.getStatusByCode(statusCode);
        switch (status) {
            case OK:
                if(StarCoderSettings.getInstance().isSaytEnabled()) {
                    toolTipText.append(" (Click to disable)");
                } else {
                    toolTipText.append(" (Click to enable)");
                }
                break;
            case UNKNOWN:
                toolTipText.append(" (http error ");
                toolTipText.append(statusCode);
                toolTipText.append(")");
                break;
            default:
                toolTipText.append(" (");
                toolTipText.append(status.getDisplayValue());
                toolTipText.append(")");
        }

        return toolTipText.toString();
    }

    @Override
    public @Nullable Consumer<MouseEvent> getClickConsumer() {
        // Toggle if the plugin is enabled.
        return mouseEvent -> {
            StarCoderSettings.getInstance().toggleSaytEnabled();
            if(myStatusBar != null) {
                myStatusBar.updateWidget(ID);
            }
        };
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
        super.install(statusBar);
        //TODO MergingUpdateQueue?
        serviceQueue = new MergingUpdateQueue("StarCoderServiceQueue",1000,true,null,this);
        EditorEventMulticaster multicaster = EditorFactory.getInstance().getEventMulticaster();
        multicaster.addCaretListener(this, this);
        multicaster.addSelectionListener(this, this);
        multicaster.addDocumentListener(this,this);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(SWING_FOCUS_OWNER_PROPERTY, this);
        Disposer.register(this,
                () -> KeyboardFocusManager.getCurrentKeyboardFocusManager().removePropertyChangeListener(SWING_FOCUS_OWNER_PROPERTY,
                        this)
        );
    }

    private Editor getFocusOwnerEditor() {
        Component component = getFocusOwnerComponent();
        Editor editor = component instanceof EditorComponentImpl ? ((EditorComponentImpl)component).getEditor() : getEditor();
        return editor != null && !editor.isDisposed() ? editor : null;
    }

    private Component getFocusOwnerComponent() {
        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (focusOwner == null) {
            IdeFocusManager focusManager = IdeFocusManager.getInstance(getProject());
            Window frame = focusManager.getLastFocusedIdeWindow();
            if (frame != null) {
                focusOwner = focusManager.getLastFocusedFor(frame);
            }
        }
        return focusOwner;
    }

    private boolean isFocusedEditor(Editor editor) {
        Component focusOwner = getFocusOwnerComponent();
        return focusOwner == editor.getContentComponent();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        updateInlayHints(getFocusOwnerEditor());
    }

    @Override
    public void selectionChanged(SelectionEvent event) {
        updateInlayHints(event.getEditor());
    }

    @Override
    public void caretPositionChanged(@NotNull CaretEvent event) {
        updateInlayHints(event.getEditor());
    }

    @Override
    public void caretAdded(@NotNull CaretEvent event) {
        updateInlayHints(event.getEditor());
    }

    @Override
    public void caretRemoved(@NotNull CaretEvent event) {
        updateInlayHints(event.getEditor());
    }

    @Override
    public void afterDocumentChange (@NotNull Document document) {
        if(ApplicationManager.getApplication().isDispatchThread()) {
            EditorFactory.getInstance().editors(document)
                    .filter(this::isFocusedEditor)
                    .findFirst()
                    .ifPresent(this::updateInlayHints);
        }
    }

    private void updateInlayHints(Editor focusedEditor) {
        if(focusedEditor == null) return;
        // TODO File extension exclusion settings?
        VirtualFile file = FileDocumentManager.getInstance().getFile(focusedEditor.getDocument());
        if (file == null) return;

        // If a selection is highlighted, clear all hints.
        String selection = focusedEditor.getCaretModel().getCurrentCaret().getSelectedText();
        if(selection != null && selection.length() > 0) {
            String[] existingHints = file.getUserData(STAR_CODER_CODE_SUGGESTION);
            if (existingHints != null && existingHints.length > 0) {
                file.putUserData(STAR_CODER_CODE_SUGGESTION, null);
                file.putUserData(STAR_CODER_POSITION, focusedEditor.getCaretModel().getOffset());

                InlayModel inlayModel = focusedEditor.getInlayModel();
                inlayModel.getInlineElementsInRange(0, focusedEditor.getDocument().getTextLength()).forEach(this::disposeInlayHints);
                inlayModel.getBlockElementsInRange(0, focusedEditor.getDocument().getTextLength()).forEach(this::disposeInlayHints);
            }
            return;
        }

        Integer starCoderPos = file.getUserData(STAR_CODER_POSITION);
        int lastPosition = (starCoderPos==null) ? 0 : starCoderPos;
        int currentPosition = focusedEditor.getCaretModel().getOffset();

        // If cursor hasn't moved, don't do anything.
        if (lastPosition == currentPosition) return;

        // Check the existing inline hint (not blocks) if it exists.
        InlayModel inlayModel = focusedEditor.getInlayModel();
        if (currentPosition > lastPosition) {
            String[] existingHints = file.getUserData(STAR_CODER_CODE_SUGGESTION);
            if (existingHints != null && existingHints.length > 0) {
                String inlineHint = existingHints[0];
                String modifiedText = focusedEditor.getDocument().getCharsSequence().subSequence(lastPosition, currentPosition).toString();
                if(modifiedText.startsWith("\n")) {
                    // If the user typed Enter, the editor may have auto-spaced for alignment.
                    modifiedText = modifiedText.replace(" ","");
                    // TODO Count the spaces and remove from the next block hint, or just remove
                    // leading spaces from the block hint before moving up?
                    // example: set a boolean here and do existingHints[1] = existingHints[1].stripLeading()
                }
                // See if they typed the same thing that we suggested.
                if (inlineHint.startsWith(modifiedText)) {
                    // Update the hint rather than calling the API to suggest a new one.
                    inlineHint = inlineHint.substring(modifiedText.length());
                    if(inlineHint.length()>0) {
                        // We only need to modify the inline hint and any block hints will remain unchanged.
                        inlayModel.getInlineElementsInRange(0, focusedEditor.getDocument().getTextLength()).forEach(this::disposeInlayHints);
                        inlayModel.addInlineElement(currentPosition, true, new CodeGenHintRenderer(inlineHint));
                        existingHints[0] = inlineHint;

                        // Update the UserData
                        file.putUserData(STAR_CODER_CODE_SUGGESTION, existingHints);
                        file.putUserData(STAR_CODER_POSITION, currentPosition);
                        return;
                    } else if (existingHints.length > 1) {
                        // If the first line has been completely inserted, and there are more lines, move them up.
                        existingHints = Arrays.copyOfRange(existingHints, 1, existingHints.length);
                        addCodeSuggestion(focusedEditor, file, currentPosition, existingHints);
                        return;
                    } else {
                        // We ran out of inline hint and there are no block hints,
                        // So clear the hints now, and we'll call the API below.
                        file.putUserData(STAR_CODER_CODE_SUGGESTION, null);
                    }
                }
            }
        }

        // If we made it through all that, clear all hints and call the API.
        inlayModel.getInlineElementsInRange(0, focusedEditor.getDocument().getTextLength()).forEach(this::disposeInlayHints);
        inlayModel.getBlockElementsInRange(0, focusedEditor.getDocument().getTextLength()).forEach(this::disposeInlayHints);

        // Update position immediately to prevent repeated calls.
        file.putUserData(STAR_CODER_POSITION, currentPosition);

        StarCoderService starCoder = ApplicationManager.getApplication().getService(StarCoderService.class);
        CharSequence editorContents = focusedEditor.getDocument().getCharsSequence();
        System.out.println("Queued update: "+currentPosition+" for "+file.getName());
        // TODO this does reduce the number of API calls but introduces a noticeable UI lag.
        serviceQueue.queue(Update.create(focusedEditor,() -> {
            String[] hintList = starCoder.getCodeCompletionHints(editorContents, currentPosition);
            this.addCodeSuggestion(focusedEditor, file, currentPosition, hintList);
        }));
//        CompletableFuture<String[]> future = CompletableFuture.supplyAsync(() -> starCoder.getCodeCompletionHints(editorContents, currentPosition));
//        future.thenAccept(hintList -> this.addCodeSuggestion(focusedEditor, file, currentPosition, hintList));
    }

    private void disposeInlayHints(Inlay<?> inlay) {
        if(inlay.getRenderer() instanceof CodeGenHintRenderer) {
            inlay.dispose();
        }
    }

    private void addCodeSuggestion(Editor focusedEditor, VirtualFile file, int suggestionPosition, String[] hintList) {
        WriteCommandAction.runWriteCommandAction(focusedEditor.getProject(), () -> {
            // Discard this update if the position has changed or text is now selected.
            if (suggestionPosition != focusedEditor.getCaretModel().getOffset()) return;
            if (focusedEditor.getSelectionModel().getSelectedText() != null) return;

            file.putUserData(STAR_CODER_CODE_SUGGESTION, hintList);
            file.putUserData(STAR_CODER_POSITION, suggestionPosition);

            InlayModel inlayModel = focusedEditor.getInlayModel();
            inlayModel.getInlineElementsInRange(0, focusedEditor.getDocument().getTextLength()).forEach(this::disposeInlayHints);
            inlayModel.getBlockElementsInRange(0, focusedEditor.getDocument().getTextLength()).forEach(this::disposeInlayHints);
            if (hintList != null && hintList.length > 0) {
                // The first line is an inline element
                if (hintList[0].trim().length() > 0) {
                    inlayModel.addInlineElement(suggestionPosition, true, new CodeGenHintRenderer(hintList[0]));
                }
                // Each additional line is a block element
                for (int i = 1; i < hintList.length; i++) {
                    inlayModel.addBlockElement(suggestionPosition, false, false, 0, new CodeGenHintRenderer(hintList[i]));
                }
            }

            System.out.println("Completed update: "+suggestionPosition+" for "+file.getName());
        });
    }
}
