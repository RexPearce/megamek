/*
 * MegaMek - Copyright (C) 2000-2002 Ben Mazur (bmazur@sev.org)
 * 
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the Free 
 * Software Foundation; either version 2 of the License, or (at your option) 
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details.
 */
package megamek.client.ui.swing.forceDisplay;

import megamek.client.ui.Messages;
import megamek.client.ui.swing.ClientGUI;
import megamek.client.ui.swing.GUIPreferences;
import megamek.client.ui.swing.lobby.ChatLounge;
import megamek.client.ui.swing.util.UIUtil;
import megamek.common.Entity;
import megamek.common.Game;
import megamek.common.force.Force;
import megamek.common.force.Forces;
import megamek.common.preference.IPreferenceChangeListener;
import megamek.common.preference.PreferenceChangeEvent;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Shows reports, with an Okay JButton
 */
public class ForceDisplayPanel extends JPanel implements IPreferenceChangeListener {

    private ForceDisplayMekTreeModel forceTreeModel;
    JTree forceTree;
    private ForceTreeMouseAdapter mekForceTreeMouseListener = new ForceTreeMouseAdapter();
    private ClientGUI clientgui;
    private Game game;
    private static final GUIPreferences GUIP = GUIPreferences.getInstance();

    public ForceDisplayPanel(ClientGUI clientgui) {
        if (clientgui == null) {
            return;
        }
        this.clientgui = clientgui;
        this.game = clientgui.getClient().getGame();

        setupForce();
        refreshTree();

        setLayout(new BorderLayout());
        JScrollPane sp = new JScrollPane(forceTree);
        add(sp, BorderLayout.CENTER);

        forceTree.addMouseListener(mekForceTreeMouseListener);

        adaptToGUIScale();
    }

    private void setupForce() {
        forceTreeModel = new ForceDisplayMekTreeModel(clientgui);
        forceTree = new JTree(forceTreeModel);
        forceTree.setRootVisible(false);
        forceTree.setDragEnabled(false);
        forceTree.setCellRenderer(new ForceDisplayMekTreeRenderer(clientgui, forceTree));
        forceTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        forceTree.setExpandsSelectedPaths(true);
    }

    /** Refreshes the Mek Tree, restoring expansion state and selection. */
    public void refreshTree() {
        // Refresh the force tree and restore selection/expand status
        HashSet<Object> selections = new HashSet<>();
        if (!forceTree.isSelectionEmpty()) {
            for (TreePath path: forceTree.getSelectionPaths()) {
                Object sel = path.getLastPathComponent();
                if (sel instanceof Force || sel instanceof Entity) {
                    selections.add(path.getLastPathComponent());
                }
            }
        }

        Forces forces = game.getForces();
        List<Integer> expandedForces = new ArrayList<>();
        for (int i = 0; i < forceTree.getRowCount(); i++) {
            TreePath currPath = forceTree.getPathForRow(i);
            if (forceTree.isExpanded(currPath)) {
                Object entry = currPath.getLastPathComponent();
                if (entry instanceof Force) {
                    expandedForces.add(((Force) entry).getId());
                }
            }
        }

        forceTree.setUI(null);
        try {
            forceTreeModel.refreshData();
        } finally {
            forceTree.updateUI();
        }
        for (int id: expandedForces) {
            if (!forces.contains(id)) {
                continue;
            }
            forceTree.expandPath(getPath(forces.getForce(id)));
        }

        forceTree.clearSelection();
        for (Object sel: selections) {
            forceTree.addSelectionPath(getPath(sel));
        }
    }

    /**
     * Returns a TreePath in the force tree for a possibly outdated entity
     * or force. Outdated means a new object of the type was sent by the server
     * and has replaced this object. Also works for the game's current objects though.
     * Uses the force's/entity's id to get the
     * game's real object with the same id. Used to reconstruct the selection
     * and expansion state of the force tree after an update.
     */
    private TreePath getPath(Object outdatedEntry) {
        Forces forces = game.getForces();
        if (outdatedEntry instanceof Force) {
            if (!forces.contains((Force) outdatedEntry)) {
                return null;
            }
            int forceId = ((Force) outdatedEntry).getId();
            List<Force> chain = forces.forceChain(forces.getForce(forceId));
            Object[] pathObjs = new Object[chain.size() + 1];
            int index = 0;
            pathObjs[index++] = forceTreeModel.getRoot();
            for (Force force: chain) {
                pathObjs[index++] = force;
            }
            return new TreePath(pathObjs);
        } else if (outdatedEntry instanceof Entity) {
            int entityId = ((Entity) outdatedEntry).getId();
            if (game.getEntity(entityId) == null) {
                return null;
            }
            List<Force> chain = forces.forceChain(game.getEntity(entityId));
            Object[] pathObjs = new Object[chain.size() + 2];
            int index = 0;
            pathObjs[index++] = forceTreeModel.getRoot();
            for (Force force: chain) {
                pathObjs[index++] = force;
            }
            pathObjs[index++] = game.getEntity(entityId);
            return new TreePath(pathObjs);
        } else {
            throw new IllegalArgumentException(Messages.getString("ChatLounge.TreePath.methodRequiresEntityForce"));
        }
    }

    public class ForceTreeMouseAdapter extends MouseInputAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                int row = forceTree.getRowForLocation(e.getX(), e.getY());
                TreePath path = forceTree.getPathForRow(row);
                if (path != null && path.getLastPathComponent() instanceof Entity) {
                    Entity entity = (Entity) path.getLastPathComponent();
                    clientgui.getUnitDisplay().displayEntity(entity);
                    GUIP.setUnitDisplayEnabled(true);

                    if (entity.isDeployed() && !entity.isOffBoard() && entity.getPosition() != null) {
                        clientgui.getBoardView().centerOnHex(entity.getPosition());
                    }
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                // If the right mouse button is pressed over an unselected entity,
                // clear the selection and select that entity instead
                int row = forceTree.getRowForLocation(e.getX(), e.getY());
                if (!forceTree.isRowSelected(row)) {
                    forceTree.setSelectionRow(row);
                }
                showPopup(e);
            }
        }

        /** Shows the right-click menu on the mek table */
        private void showPopup(MouseEvent e) {
            TreePath[] selection = forceTree.getSelectionPaths();
            List<Entity> entities = new ArrayList<>();
            List<Force> selForces = new ArrayList<>();

            if (selection != null) {
                for (TreePath path: selection) {
                    if (path != null) {
                        Object selected = path.getLastPathComponent();
                        if (selected instanceof Entity) {
                            entities.add((Entity) selected);
                        } else if (selected instanceof Force) {
                            selForces.add((Force) selected);
                        }
                    }
                }
            }
            //ScalingPopup popup = LobbyMekPopup.getPopup(entities, selForces, new LobbyMekPopupActions(ChatLounge.this), ChatLounge.this);
            //popup.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    private void adaptToGUIScale() {
        UIUtil.adjustContainer(this, UIUtil.FONT_SCALE1);
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent e) {
        // Update the text size when the GUI scaling changes
        if (e.getName().equals(GUIPreferences.GUI_SCALE)) {
            adaptToGUIScale();
        }
    }
}
