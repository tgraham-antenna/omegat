package org.omegat.gui.preferences;

import java.awt.Dimension;
import java.awt.Window;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.WindowConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.omegat.core.Core;
import org.omegat.core.data.NotLoadedProject;
import org.omegat.gui.preferences.view.ViewOptionsView;
import org.omegat.gui.preferences.view.WorkflowOptionsView;
import org.omegat.util.TestPreferencesInitializer;
import org.omegat.util.gui.StaticUIUtils;

public class PreferencesWindowController {

    private PreferencesView currentView;
    private final Map<String, Runnable> persistenceRunnables = new HashMap<>();

    public void show(Window parent) {
        JDialog dialog = new JDialog();
        dialog.setTitle("Preferences");
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        StaticUIUtils.setEscapeClosable(dialog);
        
        PreferencesPanel panel = new PreferencesPanel();
        dialog.add(panel);

        panel.availablePrefsTree.setModel(new DefaultTreeModel(getRootNode()));
        panel.availablePrefsTree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) panel.availablePrefsTree
                    .getLastSelectedPathComponent();
            if (node != null) {
                if (currentView != null) {
                    persistenceRunnables.put(currentView.getClass().getName(), currentView.getPersistenceLogic());
                }
                currentView = (PreferencesView) node.getUserObject();
                panel.selectedPrefsScrollPane.setViewportView(currentView.getGui());
            }
        });
        DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) panel.availablePrefsTree.getCellRenderer();
        renderer.setIcon(null);
        renderer.setLeafIcon(null);
        renderer.setOpenIcon(null);
        renderer.setClosedIcon(null);
        renderer.setDisabledIcon(null);

        panel.okButton.addActionListener(e -> {
            doSave();
            StaticUIUtils.closeWindowByEvent(dialog);
        });
        panel.cancelButton.addActionListener(e -> StaticUIUtils.closeWindowByEvent(dialog));
        panel.resetButton.addActionListener(e -> currentView.initDefaults());

        dialog.getRootPane().setDefaultButton(panel.okButton);

        dialog.setPreferredSize(new Dimension(800, 500));
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    public TreeNode getRootNode() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        root.add(new DefaultMutableTreeNode(new WorkflowOptionsView()));
        root.add(new DefaultMutableTreeNode(new ViewOptionsView()));
        return root;
    }

    private void doSave() {
        persistenceRunnables.values().forEach(Runnable::run);
    }

    public static void main(String[] args) throws IOException {
        TestPreferencesInitializer.init();
        Core.setProject(new NotLoadedProject());
        new PreferencesWindowController().show(null);
    }
}
