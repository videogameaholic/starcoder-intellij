package com.videogameaholic.intellij.starcoder;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public interface StarCoderIcons {
    Icon Action = IconLoader.getIcon("/icons/actionIcon.svg", StarCoderIcons.class);
    Icon WidgetEnabled = IconLoader.getIcon("/icons/widgetEnabled.svg", StarCoderIcons.class);
    Icon WidgetDisabled = IconLoader.getIcon("/icons/widgetDisabled.svg", StarCoderIcons.class);
    Icon WidgetError = IconLoader.getIcon("/icons/widgetError.svg", StarCoderIcons.class);
}
