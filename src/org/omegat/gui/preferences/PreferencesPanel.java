/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.omegat.gui.preferences;

import org.omegat.util.OStrings;

/**
 *
 * @author Aaron Madlon-Kay <aaron@madlon-kay.com>
 */
@SuppressWarnings("serial")
public class PreferencesPanel extends javax.swing.JPanel {

    /**
     * Creates new form PreferencesPanel
     */
    public PreferencesPanel() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainSplitPane = new javax.swing.JSplitPane();
        availablePrefsScrollPane = new javax.swing.JScrollPane();
        availablePrefsTree = new javax.swing.JTree();
        selectedPrefsScrollPane = new javax.swing.JScrollPane();
        selectedPrefsPlaceholderPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        bottomPanel = new javax.swing.JPanel();
        bottomButtonsPanel = new javax.swing.JPanel();
        resetButton = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 32767));
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        mainSplitPane.setBorder(null);

        availablePrefsTree.setRootVisible(false);
        availablePrefsTree.setShowsRootHandles(true);
        availablePrefsScrollPane.setViewportView(availablePrefsTree);

        mainSplitPane.setLeftComponent(availablePrefsScrollPane);

        selectedPrefsPlaceholderPanel.setLayout(new java.awt.BorderLayout());

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText(OStrings.getString("PREFERENCES_EMPTY_SELECTION_PLACEHOLDER")); // NOI18N
        selectedPrefsPlaceholderPanel.add(jLabel1, java.awt.BorderLayout.CENTER);

        selectedPrefsScrollPane.setViewportView(selectedPrefsPlaceholderPanel);

        mainSplitPane.setRightComponent(selectedPrefsScrollPane);

        add(mainSplitPane, java.awt.BorderLayout.CENTER);

        bottomPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bottomPanel.setLayout(new java.awt.BorderLayout());

        bottomButtonsPanel.setLayout(new javax.swing.BoxLayout(bottomButtonsPanel, javax.swing.BoxLayout.LINE_AXIS));

        org.openide.awt.Mnemonics.setLocalizedText(resetButton, OStrings.getString("PREFERENCES_BUTTON_RESET")); // NOI18N
        bottomButtonsPanel.add(resetButton);
        bottomButtonsPanel.add(filler1);

        org.openide.awt.Mnemonics.setLocalizedText(okButton, OStrings.getString("BUTTON_OK")); // NOI18N
        bottomButtonsPanel.add(okButton);

        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, OStrings.getString("BUTTON_CANCEL")); // NOI18N
        bottomButtonsPanel.add(cancelButton);

        bottomPanel.add(bottomButtonsPanel, java.awt.BorderLayout.EAST);

        add(bottomPanel, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.JScrollPane availablePrefsScrollPane;
    javax.swing.JTree availablePrefsTree;
    javax.swing.JPanel bottomButtonsPanel;
    javax.swing.JPanel bottomPanel;
    javax.swing.JButton cancelButton;
    javax.swing.Box.Filler filler1;
    javax.swing.JLabel jLabel1;
    javax.swing.JSplitPane mainSplitPane;
    javax.swing.JButton okButton;
    javax.swing.JButton resetButton;
    javax.swing.JPanel selectedPrefsPlaceholderPanel;
    javax.swing.JScrollPane selectedPrefsScrollPane;
    // End of variables declaration//GEN-END:variables
}
