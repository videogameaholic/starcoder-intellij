package com.videogameaholic.intellij.starcoder.settings;

import com.intellij.ui.EnumComboBoxModel;

import javax.swing.*;

public class SettingsPanel {

    private JPanel panel;
    private JTextField apiUrlTextField;
    private JPanel API;
    private JPanel Parameters;
    private JTextField bearerTokenTextField;
    private JTextField temperatureTextField;
    private JTextField maxNewTokensTextField;
    private JTextField topPTextField;
    private JTextField repetitionTextField;
    private JCheckBox enableSAYTCheckBox;
    private JPanel Settings;
    private JPanel ParamOuter;
    private JPanel TabActionPanel;
    private JComboBox<TabActionOption> tabActionComboBox;
    private JLabel tabActionLabel;

    public SettingsPanel() {
        tabActionComboBox.setModel(new EnumComboBoxModel<>(TabActionOption.class));
        enableSAYTCheckBox.addActionListener(e -> {
            tabActionLabel.setEnabled(enableSAYTCheckBox.isSelected());
            tabActionComboBox.setEnabled(enableSAYTCheckBox.isSelected());
        });
    }

    public JComponent getPanel() {
        return panel;
    }

    public String getApiUrl() {
        return apiUrlTextField.getText();
    }

    public void setApiUrl(String apiUrl) {
        apiUrlTextField.setText(apiUrl);
    }

    public String getApiToken() {
        return bearerTokenTextField.getText();
    }

    public void setApiToken(String bearerToken) {
        bearerTokenTextField.setText(bearerToken);
    }

    public String getTemperature() {
        return temperatureTextField.getText();
    }

    public void setTemperature(String temperature) {
        temperatureTextField.setText(temperature);
    }

    public String getMaxNewTokens() {
        return maxNewTokensTextField.getText();
    }

    public void setMaxNewTokens(String maxNewTokens) {
        maxNewTokensTextField.setText(maxNewTokens);
    }

    public String getTopP() {
        return topPTextField.getText();
    }

    public void setTopP(String topP) {
        topPTextField.setText(topP);
    }

    public String getRepetition() {
        return repetitionTextField.getText();
    }

    public void setRepetition(String repetition) {
        repetitionTextField.setText(repetition);
    }

    public boolean getEnableSAYTCheckBox() {
        return enableSAYTCheckBox.isSelected();
    }

    public void setEnableSAYTCheckBox(boolean enableSAYT) {
        enableSAYTCheckBox.setSelected(enableSAYT);
        tabActionLabel.setEnabled(enableSAYT);
        tabActionComboBox.setEnabled(enableSAYT);
    }

    public TabActionOption getTabActionOption() {
        return (TabActionOption) tabActionComboBox.getModel().getSelectedItem();
    }

    public void setTabActionOption(TabActionOption option) {
        tabActionComboBox.getModel().setSelectedItem(option);
    }
}
