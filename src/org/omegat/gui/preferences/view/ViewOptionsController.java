/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2010 Didier Briel
               2015 Aaron Madlon-Kay
               2016 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 OmegaT is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.preferences.view;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.omegat.core.Core;
import org.omegat.gui.editor.ModificationInfoManager;
import org.omegat.gui.preferences.BasePreferencesController;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;

/**
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 * @author Aaron Madlon-Kay
 */
public class ViewOptionsController extends BasePreferencesController {

    private ViewOptionsPanel panel;

    @Override
    public JComponent getGui() {
        if (panel == null) {
            initGui();
            initFromPrefs();
        }
        return panel;
    }

    @Override
    public String toString() {
        return OStrings.getString("PREFS_TITLE_VIEW_OPTIONS");
    }

    private void initGui() {
        panel = new ViewOptionsPanel();
        panel.templateActivator.addActionListener(e -> updateEnabledness());
        panel.variablesList.setModel(new DefaultComboBoxModel<>(ModificationInfoManager.MOD_INFO_VARIABLES));
        panel.variablesListND.setModel(new DefaultComboBoxModel<>(ModificationInfoManager.MOD_INFO_VARIABLES_NO_DATE));
        panel.insertButton.addActionListener(
                e -> panel.modInfoTemplate.replaceSelection(panel.variablesList.getSelectedItem().toString()));
        panel.insertButtonND.addActionListener(
                e -> panel.modInfoTemplateND.replaceSelection(panel.variablesListND.getSelectedItem().toString()));
    }

    @Override
    protected void initFromPrefs() {
        panel.viewSourceAllBold
                .setSelected(Preferences.isPreferenceDefault(Preferences.VIEW_OPTION_SOURCE_ALL_BOLD,
                        Preferences.VIEW_OPTION_SOURCE_ALL_BOLD_DEFAULT));
        panel.markFirstNonUnique.setSelected(Preferences.isPreference(Preferences.VIEW_OPTION_UNIQUE_FIRST));

        panel.simplifyPPTooltips
                .setSelected(Preferences.isPreferenceDefault(Preferences.VIEW_OPTION_PPT_SIMPLIFY,
                        Preferences.VIEW_OPTION_PPT_SIMPLIFY_DEFAULT));

        panel.templateActivator
                .setSelected(Preferences.isPreference(Preferences.VIEW_OPTION_TEMPLATE_ACTIVE));

        panel.modInfoTemplate.setText(Preferences.getPreferenceDefault(Preferences.VIEW_OPTION_MOD_INFO_TEMPLATE,
                ModificationInfoManager.DEFAULT_TEMPLATE));
        panel.modInfoTemplate.setCaretPosition(0);

        panel.modInfoTemplateND
                .setText(Preferences.getPreferenceDefault(Preferences.VIEW_OPTION_MOD_INFO_TEMPLATE_WO_DATE,
                ModificationInfoManager.DEFAULT_TEMPLATE_NO_DATE));
        panel.modInfoTemplateND.setCaretPosition(0);

        updateEnabledness();
    }

    @Override
    public void restoreDefaults() {
        panel.viewSourceAllBold.setSelected(Preferences.VIEW_OPTION_SOURCE_ALL_BOLD_DEFAULT);
        panel.markFirstNonUnique.setSelected(Preferences.isPreference(Preferences.VIEW_OPTION_UNIQUE_FIRST));

        panel.simplifyPPTooltips.setSelected(Preferences.VIEW_OPTION_PPT_SIMPLIFY_DEFAULT);

        panel.templateActivator.setSelected(false);

        panel.modInfoTemplate.setText(ModificationInfoManager.DEFAULT_TEMPLATE);
        panel.modInfoTemplate.setCaretPosition(0);

        panel.modInfoTemplateND.setText(ModificationInfoManager.DEFAULT_TEMPLATE_NO_DATE);
        panel.modInfoTemplateND.setCaretPosition(0);

        updateEnabledness();
    }

    private void updateEnabledness() {
        boolean isEnabled = panel.templateActivator.isSelected();
        panel.modInfoTemplate.setEnabled(isEnabled);
        panel.templateLabel.setEnabled(isEnabled);
        panel.variablesLabel.setEnabled(isEnabled);
        panel.variablesList.setEnabled(isEnabled);
        panel.insertButton.setEnabled(isEnabled);
        panel.modInfoTemplateND.setEnabled(isEnabled);
        panel.templateLabelND.setEnabled(isEnabled);
        panel.variablesLabelND.setEnabled(isEnabled);
        panel.variablesListND.setEnabled(isEnabled);
        panel.insertButtonND.setEnabled(isEnabled);
    }

    @Override
    public void persist() {
        Preferences.setPreference(Preferences.VIEW_OPTION_SOURCE_ALL_BOLD, panel.viewSourceAllBold.isSelected());
        Preferences.setPreference(Preferences.VIEW_OPTION_UNIQUE_FIRST, panel.markFirstNonUnique.isSelected());
        Preferences.setPreference(Preferences.VIEW_OPTION_PPT_SIMPLIFY, panel.simplifyPPTooltips.isSelected());
        Preferences.setPreference(Preferences.VIEW_OPTION_TEMPLATE_ACTIVE, panel.templateActivator.isSelected());
        Preferences.setPreference(Preferences.VIEW_OPTION_MOD_INFO_TEMPLATE, panel.modInfoTemplate.getText());
        Preferences.setPreference(Preferences.VIEW_OPTION_MOD_INFO_TEMPLATE_WO_DATE, panel.modInfoTemplateND.getText());
        ModificationInfoManager.reset();
        SwingUtilities.invokeLater(() -> Core.getEditor().getSettings().updateViewPreferences());
    }
}
