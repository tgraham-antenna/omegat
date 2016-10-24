package org.omegat.gui.preferences;

import javax.swing.JComponent;

public interface PreferencesView {
    JComponent getGui();

    Runnable getPersistenceLogic();

    void initDefaults();

    default boolean validate() {
        return true;
    };
}
