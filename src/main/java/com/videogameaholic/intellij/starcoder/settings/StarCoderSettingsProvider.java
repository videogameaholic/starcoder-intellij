package com.videogameaholic.intellij.starcoder.settings;

import com.intellij.application.options.editor.EditorOptionsProvider;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class StarCoderSettingsProvider implements EditorOptionsProvider {
    private SettingsPanel settingsPanel;

    @Override
    public @NotNull @NonNls String getId() {
        return "StarCoder.Settings";
    }

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "StarCoder";
    }

    @Override
    public @Nullable JComponent createComponent() {
        if(settingsPanel == null) {
            settingsPanel = new SettingsPanel();
        }
        return settingsPanel.getPanel();
    }

    @Override
    public boolean isModified() {
        StarCoderSettings savedSettings = StarCoderSettings.getInstance();

        return !savedSettings.getApiURL().equals(settingsPanel.getApiUrl())
                || !savedSettings.getApiToken().equals(settingsPanel.getApiToken())
                || savedSettings.isSaytEnabled() != settingsPanel.getEnableSAYTCheckBox()
                || savedSettings.getTemperature() != Float.parseFloat(settingsPanel.getTemperature())
                || savedSettings.getMaxNewTokens() != Integer.parseInt(settingsPanel.getMaxNewTokens())
                || savedSettings.getTopP() != Float.parseFloat(settingsPanel.getTopP())
                || savedSettings.getRepetitionPenalty() != Float.parseFloat(settingsPanel.getRepetition());
    }

    @Override
    public void apply() throws ConfigurationException {
        StarCoderSettings savedSettings = StarCoderSettings.getInstance();

        savedSettings.setApiURL(settingsPanel.getApiUrl());
        savedSettings.setApiToken(settingsPanel.getApiToken());
        savedSettings.setSaytEnabled(settingsPanel.getEnableSAYTCheckBox());
        savedSettings.setTemperature(settingsPanel.getTemperature());
        savedSettings.setMaxNewTokens(settingsPanel.getMaxNewTokens());
        savedSettings.setTopP(settingsPanel.getTopP());
        savedSettings.setRepetitionPenalty(settingsPanel.getRepetition());

        // TODO Call update on the statusBarWidget
    }

    @Override
    public void reset() {
        StarCoderSettings savedSettings = StarCoderSettings.getInstance();

        settingsPanel.setApiUrl(savedSettings.getApiURL());
        settingsPanel.setApiToken(savedSettings.getApiToken());
        settingsPanel.setEnableSAYTCheckBox(savedSettings.isSaytEnabled());
        settingsPanel.setTemperature(String.valueOf(savedSettings.getTemperature()));
        settingsPanel.setMaxNewTokens(String.valueOf(savedSettings.getMaxNewTokens()));
        settingsPanel.setTopP(String.valueOf(savedSettings.getTopP()));
        settingsPanel.setRepetition(String.valueOf(savedSettings.getRepetitionPenalty()));
    }
}