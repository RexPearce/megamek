/*
 * MegaMek - Copyright (C) 2003,2004,2005 Ben Mazur (bmazur@sev.org)
 * Copyright (c) 2021 - The MegaMek Team. All Rights Reserved.
 *
 * This file is part of MegaMek.
 *
 * MegaMek is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MegaMek is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MegaMek. If not, see <http://www.gnu.org/licenses/>.
 */
package megamek.client.ui.swing;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import megamek.MegaMek;
import megamek.client.Client;
import megamek.client.ui.Messages;
import megamek.common.*;
import megamek.common.Entity.WeaponSortOrder;
import megamek.common.preference.IPreferenceChangeListener;
import megamek.common.preference.PreferenceChangeEvent;
import static megamek.client.ui.swing.DeployMinefieldDisplay.Command.*;
import static megamek.client.ui.swing.DeploymentDisplay.DeployCommand.*;
import static megamek.client.ui.swing.MovementDisplay.MoveCommand.*;
import static megamek.client.ui.swing.FiringDisplay.FiringCommand.*;
import static megamek.client.ui.swing.PhysicalDisplay.PhysicalCommand.*;
import static megamek.client.ui.swing.ClientGUI.*;

/**
 * The menu bar that is used across MM, i.e. in the main menu, the board editor and
 * the lobby and game. 
 */
public class CommonMenuBar extends JMenuBar implements ActionListener,
        IPreferenceChangeListener {

    private static final long serialVersionUID = -3696211330827384307L;
    private Client client;
    /** The current game. Can be null! (e.g. in MM's main menu) */
    private IGame game;
    // The Game menu
    private JMenuItem fileGameNew;
    private JMenuItem fileGameOpen;
    private JMenuItem fileGameSave;
    private JMenuItem fileGameQSave;
    private JMenuItem fileGameQLoad;
    private JMenuItem fileGameSaveServer;
    private JMenuItem fileGameScenario;
    private JMenuItem fileGameConnectBot;
    private JMenuItem fileGameConnect;
    private JMenuItem replacePlayer;
    
    /** True when there is a board that can be accessed. */
    private boolean hasBoard;
    // The Board submenu
    private JMenuItem fileBoardNew;
    private JMenuItem fileBoardOpen;
    private JMenuItem fileBoardSave;
    private JMenuItem fileBoardSaveAs;
    private JMenuItem fileBoardSaveAsImage;
    private JMenuItem fileBoardSaveAsImageUnits;

    // The Units submenu
    private JMenuItem fileUnitsReinforce;
    private JMenuItem fileUnitsReinforceRAT;
    private JMenuItem fileRefreshCache;
    private JMenuItem fileUnitsPaste;
    
    /** The currently selected entity. Can be null! */
    private Entity entity;
    /** The current phase of the game, if any. */
    private IGame.Phase phase = IGame.Phase.PHASE_UNKNOWN;

    // The View menu
    private JMenuItem viewMiniMap;
    private JMenuItem viewMekDisplay;
    private JMenuItem viewAccessibilityWindow;
    private JCheckBoxMenuItem viewKeybindsOverlay;
    private JMenuItem viewZoomIn;
    private JMenuItem viewZoomOut;
    private JMenuItem viewResetWindowPositions;
    private JCheckBoxMenuItem toggleIsometric;
    private JCheckBoxMenuItem toggleFieldOfFire;
    private JCheckBoxMenuItem toggleFovHighlight;
    private JCheckBoxMenuItem toggleFovDarken;
    private JCheckBoxMenuItem toggleFiringSolutions;
    private JCheckBoxMenuItem viewMovementEnvelope;
    private JMenuItem viewMovModEnvelope;
    private JMenuItem viewChangeTheme;
    private JMenuItem viewLOSSetting;
    private JMenuItem viewUnitOverview;
    private JMenuItem viewRoundReport;
    private JMenuItem viewGameOptions;
    private JMenuItem viewClientSettings;
    private JMenuItem viewPlayerSettings;
    private JMenuItem viewPlayerList;
    private JMenuItem viewIncGUIScale;
    private JMenuItem viewDecGUIScale;
    
    // The Deploy menu
    private JMenuItem deployMinesConventional;
    private JMenuItem deployMinesCommand;
    private JMenuItem deployMinesVibrabomb;
    private JMenuItem deployMinesActive;
    private JMenuItem deployMinesInferno;
    
    // The Firing Action menu
    private JMenuItem fireSaveWeaponOrder;
    private JMenuItem fireCancel;
    
    /** Contains all ActionListeners that have registered themselves with this menu bar. */
    private final List<ActionListener> actionListeners = new ArrayList<>();
    
    /** Maps the Action Command to the respective MenuItem. */
    private final Map<String, JMenuItem> itemMap = new HashMap<String, JMenuItem>();

    /**
     * Create a MegaMek menu bar.
     */
    public CommonMenuBar(Client parent) {
        this();
        client = parent;
    }

    public CommonMenuBar() {
        // Create the File menu
        JMenu menu = new JMenu(getString("FileMenu")); 
        menu.setMnemonic(KeyEvent.VK_F);
        add(menu);

        // Create the Game sub-menu
        JMenu submenu = new JMenu(getString("GameMenu")); 
        menu.add(submenu);
        fileGameNew = createMenuItem(submenu, getString("fileGameNew"), FILE_GAME_NEW, KeyEvent.VK_N);
        fileGameOpen = createMenuItem(submenu, getString("fileGameOpen"), FILE_GAME_OPEN);
        fileGameSave = createMenuItem(submenu, getString("fileGameSave"), FILE_GAME_SAVE);
        fileGameQSave = createMenuItem(submenu, getString("fileGameQuickSave"), FILE_GAME_QSAVE);
        fileGameQSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        fileGameQLoad = createMenuItem(submenu, getString("fileGameQuickLoad"), FILE_GAME_QLOAD);
        fileGameQLoad.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK));
        fileGameSaveServer = createMenuItem(submenu, getString("fileGameSaveServer"), FILE_GAME_SAVE_SERVER);
        submenu.addSeparator();
        fileGameScenario = createMenuItem(submenu, getString("fileGameScenario"), FILE_GAME_SCENARIO);
        submenu.addSeparator();
        fileGameConnectBot = createMenuItem(submenu, getString("fileGameConnectBot"), FILE_GAME_CONNECT_BOT);
        fileGameConnect = createMenuItem(submenu, getString("fileGameConnect"), FILE_GAME_CONNECT);
        replacePlayer = createMenuItem(submenu, getString("replacePlayer"), FILE_GAME_REPLACE_PLAYER);

        // Create the Board sub-menu.
        submenu = new JMenu(getString("BoardMenu")); 
        menu.add(submenu);
        fileBoardNew = createMenuItem(submenu, getString("fileBoardNew"), FILE_BOARD_NEW);
        fileBoardOpen = createMenuItem(submenu, getString("fileBoardOpen"), FILE_BOARD_OPEN);
        fileBoardSave = createMenuItem(submenu, getString("fileBoardSave"), FILE_BOARD_SAVE);
        fileBoardSaveAs = createMenuItem(submenu, getString("fileBoardSaveAs"), FILE_BOARD_SAVE_AS);
        fileBoardSaveAsImage = createMenuItem(submenu, getString("fileBoardSaveAsImage"), FILE_BOARD_SAVE_AS_IMAGE);
        fileBoardSaveAsImage.setToolTipText(getString("fileBoardSaveAsImage.tooltip"));
        fileBoardSaveAsImageUnits = createMenuItem(submenu, getString("fileBoardSaveAsImageUnits"), FILE_BOARD_SAVE_AS_IMAGE_UNITS);
        fileBoardSaveAsImage.setToolTipText(getString("fileBoardSaveAsImageUnits.tooltip")); 

        // Create the Unit List sub-menu.
        submenu = new JMenu(getString("UnitListMenu")); 
        menu.add(submenu);
        fileUnitsReinforce = createMenuItem(submenu, getString("fileUnitsReinforce"), FILE_UNITS_REINFORCE);
        fileUnitsReinforceRAT = createMenuItem(submenu, getString("fileUnitsReinforceRAT"), FILE_UNITS_REINFORCE_RAT);
        fileRefreshCache = createMenuItem(submenu, getString("fileUnitsRefreshUnitCache"), FILE_REFRESH_CACHE);
        fileUnitsPaste = createMenuItem(submenu, getString("fileUnitsPaste"), FILE_UNITS_PASTE);
        fileUnitsPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));

        // Create the view menu.
        menu = new JMenu(getString("ViewMenu"));
        menu.setMnemonic(KeyEvent.VK_V);
        add(menu);
        viewMekDisplay = createMenuItem(menu, getString("viewMekDisplay"), VIEW_MEK_DISPLAY, KeyEvent.VK_D);
        viewAccessibilityWindow = createMenuItem(menu, getString("viewAccessibilityWindow"), VIEW_ACCESSIBILITY_WINDOW);
        viewAccessibilityWindow.setMnemonic(KeyEvent.VK_A);
        viewIncGUIScale = createMenuItem(menu, getString("viewIncGUIScale"), VIEW_INCGUISCALE);
        viewIncGUIScale.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, InputEvent.CTRL_DOWN_MASK));
        viewDecGUIScale = createMenuItem(menu, getString("viewDecGUIScale"), VIEW_DECGUISCALE);
        viewDecGUIScale.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, InputEvent.CTRL_DOWN_MASK));
        //TODO: remove keybinds overlay from the KeyCommandBinds and add an accelerator here instead
        viewKeybindsOverlay = createCbxMenuItem(menu, getString("viewKeyboardShortcuts"), VIEW_KEYBINDS_OVERLAY);
        viewKeybindsOverlay.setState(GUIPreferences.getInstance().getBoolean(GUIPreferences.SHOW_KEYBINDS_OVERLAY));
        viewResetWindowPositions = createMenuItem(menu, getString("viewResetWindowPos"), VIEW_RESET_WINDOW_POSITIONS);
        //TODO: show minimap should be a checkbox
        viewMiniMap = createMenuItem(menu, getString("viewMiniMap"), VIEW_MINI_MAP, KeyEvent.VK_M);
        //TODO: show unit overview should be a checkbox
        viewUnitOverview = createMenuItem(menu, getString("viewUnitOverview"), VIEW_UNIT_OVERVIEW, KeyEvent.VK_U);
        viewZoomIn = createMenuItem(menu, getString("viewZoomIn"), VIEW_ZOOM_IN);
        viewZoomOut = createMenuItem(menu, getString("viewZoomOut"), VIEW_ZOOM_OUT);
        menu.addSeparator();
        toggleIsometric = createCbxMenuItem(menu, getString("viewToggleIsometric"), VIEW_TOGGLE_ISOMETRIC);
        toggleIsometric.setState(GUIPreferences.getInstance().getBoolean(GUIPreferences.USE_ISOMETRIC)); 
        toggleFovDarken = createCbxMenuItem(menu, getString("viewToggleFovDarken"), VIEW_TOGGLE_FOV_DARKEN);
        toggleFovDarken.setState(GUIPreferences.getInstance().getBoolean(GUIPreferences.FOV_DARKEN)); 
        toggleFovDarken.setToolTipText(getString("viewToggleFovDarkenTooltip"));
        toggleFovHighlight = createCbxMenuItem(menu, getString("viewToggleFovHighlight"), VIEW_TOGGLE_FOV_HIGHLIGHT);
        toggleFovHighlight.setState(GUIPreferences.getInstance().getBoolean("FovHighlight"));
        toggleFieldOfFire = createCbxMenuItem(menu, getString("viewToggleFieldOfFire"), VIEW_TOGGLE_FIELD_OF_FIRE);
        toggleFieldOfFire.setState(GUIPreferences.getInstance().getShowFieldOfFire());
        toggleFieldOfFire.setToolTipText(getString("viewToggleFieldOfFireToolTip"));
        toggleFiringSolutions = createCbxMenuItem(menu, getString("viewToggleFiringSolutions"), VIEW_TOGGLE_FIRING_SOLUTIONS);
        toggleFiringSolutions.setToolTipText(getString("viewToggleFiringSolutionsToolTip")); 
        toggleFiringSolutions.setState(GUIPreferences.getInstance().getBoolean("FiringSolutions"));
        viewMovementEnvelope = createCbxMenuItem(menu, getString("movementEnvelope"), VIEW_MOVE_ENV, KeyEvent.VK_Q);
        viewMovementEnvelope.setState(GUIPreferences.getInstance().getBoolean("MoveEnvelope"));
        viewMovModEnvelope = createMenuItem(menu, getString("movementModEnvelope"), VIEW_MOVE_MOD_ENV, KeyEvent.VK_W);
        viewChangeTheme = createMenuItem(menu, getString("viewChangeTheme"), VIEW_CHANGE_THEME);
        menu.addSeparator();
        viewRoundReport = createMenuItem(menu, getString("viewRoundReport"), VIEW_ROUND_REPORT, KeyEvent.VK_R);
        menu.addSeparator();
        viewGameOptions = createMenuItem(menu, getString("viewGameOptions"), VIEW_GAME_OPTIONS);
        viewClientSettings = createMenuItem(menu, getString("viewClientSettings"), VIEW_CLIENT_SETTINGS);
        viewClientSettings.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.ALT_DOWN_MASK));
        viewLOSSetting = createMenuItem(menu, getString("viewLOSSetting"), VIEW_LOS_SETTING, KeyEvent.VK_L);
        viewPlayerSettings = createMenuItem(menu, getString("viewPlayerSettings"), VIEW_PLAYER_SETTINGS);
        menu.addSeparator();
        viewPlayerList = createMenuItem(menu, getString("viewPlayerList"), VIEW_PLAYER_LIST);

        //  Create the deploy menu
        menu = new JMenu(getString("DeployMenu")); 
        menu.setMnemonic(KeyEvent.VK_D);
        add(menu);

        // Create the Deploy Mines sub-menu
        submenu = new JMenu(getString("DeployMinesMenu")); 
        deployMinesConventional = createMenuItem(submenu, getString("deployMinesConventional"), DEPLOY_MINE_CONV.getCmd()); 
        deployMinesCommand = createMenuItem(submenu, getString("deployMinesCommand"), DEPLOY_MINE_COM.getCmd()); 
        deployMinesVibrabomb = createMenuItem(submenu, getString("deployMinesVibrabomb"), DEPLOY_MINE_VIBRA.getCmd()); 
        deployMinesActive = createMenuItem(submenu, getString("deployMinesActive"), DEPLOY_MINE_ACTIVE.getCmd()); 
        deployMinesInferno = createMenuItem(submenu, getString("deployMinesInferno"), DEPLOY_MINE_INFERNO.getCmd()); 

        // Finish off the deploy menu.
        createMenuItem(menu, getString("deployNext"), DEPLOY_NEXT.getCmd(), KeyEvent.VK_N); 
        createMenuItem(menu, getString("deployTurn"), DEPLOY_TURN.getCmd()); 
        createMenuItem(menu, getString("deployLoad"), DEPLOY_LOAD.getCmd()); 
        createMenuItem(menu, getString("deployUnload"), DEPLOY_UNLOAD.getCmd()); 
        createMenuItem(menu, getString("deployRemove"), DEPLOY_REMOVE.getCmd()); 
        createMenuItem(menu, getString("deployAssaultDrop"), DEPLOY_ASSAULTDROP.getCmd()); 
        menu.addSeparator();
        menu.add(submenu);

        // Create the Move Commands menu
        menu = new JMenu(getString("MoveMenu")); 
        menu.setMnemonic(KeyEvent.VK_M);
        add(menu);
        createMenuItem(menu, getString("moveWalk"), MOVE_WALK.getCmd(), KeyEvent.VK_W); 
        createMenuItem(menu, getString("moveJump"), MOVE_JUMP.getCmd(), KeyEvent.VK_J); 
        createMenuItem(menu, getString("moveSwim"), MOVE_SWIM.getCmd(), KeyEvent.VK_S); 
        createMenuItem(menu, getString("moveModeConvert"), MOVE_MODE_CONVERT.getCmd(), KeyEvent.VK_C); 
        createMenuItem(menu, getString("moveBackUp"), MOVE_BACK_UP.getCmd()); 
        createMenuItem(menu, getString("moveGetUp"), MOVE_GET_UP.getCmd()); 
        createMenuItem(menu, getString("moveGoProne"), MOVE_GO_PRONE.getCmd()); 
        createMenuItem(menu, getString("moveTurn"), MOVE_TURN.getCmd()); 
        createMenuItem(menu, getString("moveNext"), MOVE_NEXT.getCmd(), KeyEvent.VK_N); 
        createMenuItem(menu, getString("moveForwardIni"), MOVE_FORWARD_INI.getCmd()); 
        createMenuItem(menu, getString("moveRaise"), MOVE_RAISE_ELEVATION.getCmd()); 
        createMenuItem(menu, getString("moveLower"), MOVE_LOWER_ELEVATION.getCmd()); 
        createMenuItem(menu, getString("moveReckless"), MOVE_RECKLESS.getCmd()); 
        createMenuItem(menu, getString("moveEvade"), MOVE_EVADE.getCmd()); 
        createMenuItem(menu, getString("moveBootlegger"), MOVE_BOOTLEGGER.getCmd()); 
        createMenuItem(menu, getString("moveShutdown"), MOVE_SHUTDOWN.getCmd()); 
        createMenuItem(menu, getString("moveStartup"), MOVE_STARTUP.getCmd()); 
        createMenuItem(menu, getString("moveSelfDestruct"), MOVE_SELF_DESTRUCT.getCmd()); 
        /* TODO: moveTraitor = createMenuItem(menu, getString("moveTraitor"), MovementDisplay.MOVE_TRAITOR);  */

        // Create the Aero Movement sub-menu
        JMenu aeromenu = new JMenu(getString("AeroMenu")); 
        createMenuItem(aeromenu, getString("moveAcc"), MOVE_ACC.getCmd()); 
        createMenuItem(aeromenu, getString("moveDec"), MOVE_DEC.getCmd()); 
        createMenuItem(aeromenu, getString("moveAccN"), MOVE_ACCN.getCmd()); 
        createMenuItem(aeromenu, getString("moveDecN"), MOVE_DECN.getCmd()); 
        createMenuItem(aeromenu, getString("moveEvadeAero"), MOVE_EVADE_AERO.getCmd()); 
        createMenuItem(aeromenu, getString("moveRoll"), MOVE_ROLL.getCmd()); 
        aeromenu.addSeparator();
        createMenuItem(aeromenu, getString("moveHover"), MOVE_HOVER.getCmd()); 
        createMenuItem(aeromenu, getString("moveManeuver"), MOVE_MANEUVER.getCmd()); 
        createMenuItem(aeromenu, getString("moveTurnLeft"), MOVE_TURN_LEFT.getCmd()); 
        createMenuItem(aeromenu, getString("moveTurnRight"), MOVE_TURN_RIGHT.getCmd()); 
        createMenuItem(aeromenu, getString("moveThrust"), MOVE_THRUST.getCmd()); 
        createMenuItem(aeromenu, getString("moveYaw"), MOVE_YAW.getCmd()); 
        createMenuItem(aeromenu, getString("moveEndOver"), MOVE_END_OVER.getCmd()); 
        createMenuItem(aeromenu, getString("moveStrafe"), MOVE_STRAFE.getCmd()); 
        createMenuItem(aeromenu, getString("moveBomb"), MOVE_BOMB.getCmd()); 

        menu.addSeparator();
        menu.add(aeromenu);

        // Create the Special sub-menu.
        submenu = new JMenu(getString("SpecialMenu")); 
        createMenuItem(submenu, getString("MoveLoad"), MOVE_LOAD.getCmd()); 
        createMenuItem(submenu, getString("MoveUnload"), MOVE_UNLOAD.getCmd()); 
        createMenuItem(submenu, getString("moveTow"), MOVE_TOW.getCmd()); 
        createMenuItem(submenu, getString("moveDisconnect"), MOVE_DISCONNECT.getCmd()); 
        createMenuItem(submenu, getString("moveLaunch"), MOVE_LAUNCH.getCmd()); 
        createMenuItem(submenu, getString("moveRecover"), MOVE_RECOVER.getCmd()); 
        createMenuItem(submenu, getString("moveJoin"), MOVE_JOIN.getCmd()); 
        submenu.addSeparator();
        createMenuItem(submenu, getString("MoveCharge"), MOVE_CHARGE.getCmd()); 
        createMenuItem(submenu, getString("MoveDeth"), MOVE_DFA.getCmd()); 
        createMenuItem(submenu, getString("moveRam"), MOVE_RAM.getCmd()); 
        submenu.addSeparator();
        createMenuItem(submenu, getString("MoveFlee"), MOVE_FLEE.getCmd()); 
        createMenuItem(submenu, getString("MoveFlyOff"), MOVE_FLY_OFF.getCmd()); 
        createMenuItem(submenu, getString("MoveEject"), MOVE_EJECT.getCmd()); 
        submenu.addSeparator();
        createMenuItem(submenu, getString("moveUnjam"), MOVE_UNJAM.getCmd()); 
        createMenuItem(submenu, getString("moveSearchlight"), MOVE_SEARCHLIGHT.getCmd()); 
        createMenuItem(submenu, getString("moveClear"), MOVE_CLEAR.getCmd()); 
        createMenuItem(submenu, getString("moveHullDown"), MOVE_HULL_DOWN.getCmd()); 
        createMenuItem(submenu, getString("moveLayMine"), MOVE_LAY_MINE.getCmd()); 
        createMenuItem(submenu, getString("moveDump"), MOVE_DUMP.getCmd()); 

        menu.addSeparator();
        menu.add(submenu);

        menu.addSeparator();
        createMenuItem(menu, getString("moveCancel"), MOVE_CANCEL.getCmd(), KeyEvent.VK_ESCAPE); 

        // Create the Attack actions menu
        menu = new JMenu(getString("FireMenu")); 
        menu.setMnemonic(KeyEvent.VK_I);
        add(menu);
        createMenuItem(menu, getString("fireFire"), FIRE_FIRE.getCmd(), KeyEvent.VK_F); 
        createMenuItem(menu, getString("fireSkip"), FIRE_SKIP.getCmd(), KeyEvent.VK_S); 
        createMenuItem(menu, getString("fireNextTarg"), FIRE_NEXT_TARG.getCmd(), KeyEvent.VK_T); 
        createMenuItem(menu, getString("fireNext"), FIRE_NEXT.getCmd(), KeyEvent.VK_N); 
        menu.addSeparator();
        createMenuItem(menu, getString("fireTwist"), FIRE_TWIST.getCmd()); 
        createMenuItem(menu, getString("fireFlipArms"), FIRE_FLIP_ARMS.getCmd()); 
        menu.addSeparator();
        createMenuItem(menu, getString("fireMode"), FIRE_MODE.getCmd(), KeyEvent.VK_O); 
        createMenuItem(menu, getString("fireCalled"), FIRE_CALLED.getCmd()); 
        menu.addSeparator();
        createMenuItem(menu, getString("fireFindClub"), FIRE_FIND_CLUB.getCmd()); 
        createMenuItem(menu, getString("fireSpot"), FIRE_SPOT.getCmd()); 
        createMenuItem(menu, getString("fireSearchlight"), FIRE_SEARCHLIGHT.getCmd()); 
        createMenuItem(menu, getString("fireClearTurret"), FIRE_CLEAR_TURRET.getCmd()); 
        createMenuItem(menu, getString("fireClearWeaponJam"), FIRE_CLEAR_WEAPON.getCmd()); 
        menu.addSeparator();
        createMenuItem(menu, getString("fireStrafe"), FIRE_CLEAR_WEAPON.getCmd()); 
        menu.addSeparator();
        fireCancel = createMenuItem(menu, getString("fireCancel"), FIRE_CANCEL.getCmd(), KeyEvent.VK_ESCAPE); 
        menu.addSeparator();
        fireSaveWeaponOrder = createMenuItem(menu, getString("fireSaveWeaponOrder"), ClientGUI.FIRE_SAVE_WEAPON_ORDER); 

        // Create the Physical Attacks menu
        menu = new JMenu(getString("PhysicalMenu")); 
        menu.setMnemonic(KeyEvent.VK_P);
        add(menu);
        createMenuItem(menu, getString("physicalPunch"), PHYSICAL_PUNCH.getCmd()); 
        createMenuItem(menu, getString("physicalKick"), PHYSICAL_KICK.getCmd()); 
        createMenuItem(menu, getString("physicalPush"), PHYSICAL_PUSH.getCmd()); 
        createMenuItem(menu, getString("physicalClub"), PHYSICAL_CLUB.getCmd()); 
        createMenuItem(menu, getString("physicalBrushOff"), PHYSICAL_BRUSH_OFF.getCmd()); 
        createMenuItem(menu, getString("physicalThrash"), PHYSICAL_THRASH.getCmd()); 
        createMenuItem(menu, getString("physicalProto"), PHYSICAL_PROTO.getCmd()); 
        createMenuItem(menu, getString("physicalDodge"), PHYSICAL_DODGE.getCmd()); 
        createMenuItem(menu, getString("physicalVibro"), PHYSICAL_VIBRO.getCmd()); 
        createMenuItem(menu, getString("physicalNext"), PHYSICAL_NEXT.getCmd(), KeyEvent.VK_N); 

        // Create the Help menu
        menu = new JMenu(getString("HelpMenu")); 
        menu.setMnemonic(KeyEvent.VK_H);
        add(menu);
        createMenuItem(menu, getString("helpContents"), ClientGUI.HELP_CONTENTS, true);
        createMenuItem(menu, getString("helpSkinning"), ClientGUI.HELP_SKINNING, true);
        menu.addSeparator();
        createMenuItem(menu, getString("helpAbout"), ClientGUI.HELP_ABOUT, true);

        manageMenu();
        
        GUIPreferences.getInstance().addPreferenceChangeListener(this);
    }

    private JMenuItem createMenuItem(JMenu menu, String label, String command, int shortcut) {
        JMenuItem item = createMenuItem(menu, label, command);
        item.setMnemonic(shortcut);
        item.setAccelerator(KeyStroke.getKeyStroke(shortcut, getToolkit().getMenuShortcutKeyMaskEx()));
        return item;
    }
    
    private JMenuItem createMenuItem(JMenu menu, String label, String command) {
        JMenuItem item = new JMenuItem(label);
        item.addActionListener(this);
        item.setActionCommand(command);
        item.setEnabled(false);
        itemMap.put(command, item);
        menu.add(item);
        return item;
    }
    
    private JMenuItem createMenuItem(JMenu menu, String label, String command, boolean active) {
        JMenuItem item = createMenuItem(menu, label, command);
        item.setEnabled(active);
        return item;
    }
    
    private JCheckBoxMenuItem createCbxMenuItem(JMenu menu, String label, String command) {
        var item = new JCheckBoxMenuItem(label); 
        item.addActionListener(this);
        item.setActionCommand(command);
        item.setEnabled(false);
        itemMap.put(command, item);
        menu.add(item);
        return item;
    }
    
    private JCheckBoxMenuItem createCbxMenuItem(JMenu m, String label, String command, int shortcut) {
        JCheckBoxMenuItem item = createCbxMenuItem(m, label, command);
        item.setMnemonic(shortcut);
        item.setAccelerator(KeyStroke.getKeyStroke(shortcut, getToolkit().getMenuShortcutKeyMaskEx()));
        return item;
    }

    /**
     * Implement the <code>ActionListener</code> interface.
     *
     * @param event - the <code>ActionEvent</code> that spawned this call.
     */
    public void actionPerformed(ActionEvent event) {
        // Pass the action on to each of our listeners.
        actionListeners.stream().forEach(l -> l.actionPerformed(event));
    }

    /**
     * Register an object that wishes to be alerted when an item on this menu
     * bar has been selected. <p/> Please note, the ActionCommand property of
     * the action event will inform the listener as to which menu item that has
     * been selected. Not all listeners will be interested in all menu items.
     *
     * @param listener - the <code>ActionListener</code> that wants to
     *            register itself.
     */
    public synchronized void addActionListener(ActionListener listener) {
        actionListeners.add(listener);
    }

    /**
     * Remove an object that was being alerted when an item on this menu bar was
     * selected.
     *
     * @param listener - the <code>ActionListener</code> that wants to be
     *            removed.
     */
    public synchronized void removeActionListener(ActionListener listener) {
        actionListeners.remove(listener);
    }

    /**
     * A helper function that will manage the enabled states of the items in
     * this menu, based upon the object's current state.
     */
    private synchronized void manageMenu() {
        // If we have a game, we can't join a new one, but we can save it.
        // Also, no Game menu in the editor (where (this.hasBoard &&
        // null==this.client)).
        if ((game != null) || (hasBoard && (null == client))) {
            fileGameNew.setEnabled(false);
            fileGameOpen.setEnabled(false);
            fileGameQLoad.setEnabled(false);
            fileGameScenario.setEnabled(false);
            fileGameConnectBot.setEnabled(false);
            fileGameConnect.setEnabled(false);
            replacePlayer.setEnabled(false);
            // We can only save in certain phases of the game.
            if ((phase != IGame.Phase.PHASE_UNKNOWN)
                    && (phase != IGame.Phase.PHASE_SELECTION)
                    && (phase != IGame.Phase.PHASE_EXCHANGE)
                    && (phase != IGame.Phase.PHASE_VICTORY)
                    && (phase != IGame.Phase.PHASE_STARTING_SCENARIO)) {
                fileGameSave.setEnabled(true);
                fileGameSaveServer.setEnabled(true);
                replacePlayer.setEnabled(true);
                fileGameQSave.setEnabled(true);
            } else {
                fileGameSave.setEnabled(false);
                fileGameSaveServer.setEnabled(false);
                replacePlayer.setEnabled(false);
                fileGameQSave.setEnabled(false);
            }
        }
        // If we have no game, we can't save, but we can create or join one.
        else {
            fileGameNew.setEnabled(true);
            fileGameOpen.setEnabled(true);
            fileGameQLoad.setEnabled(true);
            fileGameSave.setEnabled(false);
            fileGameSaveServer.setEnabled(false);
            fileGameScenario.setEnabled(true);
            fileGameConnectBot.setEnabled(true);
            fileGameConnect.setEnabled(true);
            replacePlayer.setEnabled(true);
            fileGameQSave.setEnabled(false);
        }

        // can view Game Opts if we have a game
        if (game != null) {
            viewGameOptions.setEnabled(true);
            viewPlayerSettings.setEnabled(true);
        } else {
            viewGameOptions.setEnabled(false);
            viewPlayerSettings.setEnabled(false);
        }

        // the Client doesn't have any board actions
        if (client != null) {
            fileBoardNew.setEnabled(false);
            fileBoardOpen.setEnabled(false);
            fileBoardSave.setEnabled(false);
            fileBoardSaveAs.setEnabled(false);
            fileBoardSaveAsImage.setEnabled(false);
            fileBoardSaveAsImageUnits.setEnabled(false);
            // but the main window and map editor do
        } else {
            fileBoardNew.setEnabled(true);
            fileBoardOpen.setEnabled(true);
            fileBoardSave.setEnabled(false);
            fileBoardSaveAs.setEnabled(false);
            fileBoardSaveAsImage.setEnabled(false);
        }

        // If we have a board, we can perform board actions and view the mini
        // map.
        if (hasBoard) {
            fileBoardSave.setEnabled(true);
            fileBoardSaveAs.setEnabled(true);
            fileBoardSaveAsImage.setEnabled(true);
            fileBoardSaveAsImageUnits.setEnabled(true);
            viewMiniMap.setEnabled(true);
            viewZoomIn.setEnabled(true);
            viewZoomOut.setEnabled(true);
            viewKeybindsOverlay.setEnabled(true);
        }
        // If we don't have a board we can't view the mini map.
        else {
            fileBoardSave.setEnabled(false);
            fileBoardSaveAs.setEnabled(false);
            fileBoardSaveAsImage.setEnabled(false);
            fileBoardSaveAsImageUnits.setEnabled(false);
            viewMiniMap.setEnabled(false);
            viewZoomIn.setEnabled(false);
            viewZoomOut.setEnabled(false);
            viewKeybindsOverlay.setEnabled(false);
        }
        
        fileUnitsPaste.setEnabled(phase == IGame.Phase.PHASE_LOUNGE);

        // Reinforcements cannot be added in the lounge!
        fileUnitsReinforce.setEnabled(phase != IGame.Phase.PHASE_LOUNGE);
        fileUnitsReinforceRAT.setEnabled(phase != IGame.Phase.PHASE_LOUNGE);

        // If an entity has been selected, we can view it.
        if (entity != null) {
            viewMekDisplay.setEnabled(true);
        }
        // If we haven't selected an entity, we can't view it.
        else {
            viewMekDisplay.setEnabled(false);
        }
        
        // We can only view the LOS/Range tool setting and
        // the mini map in certain phases of the game. Also
        // the board editor has no LOS/Units/Player stuff
        if ((client == null) && hasBoard) {
            viewLOSSetting.setEnabled(false);
            viewUnitOverview.setEnabled(false);
            viewPlayerList.setEnabled(false);
            viewChangeTheme.setEnabled(true);
        }
        
        
        // We're in-game.
        else if ((phase == IGame.Phase.PHASE_SET_ARTYAUTOHITHEXES)
                || (phase == IGame.Phase.PHASE_DEPLOY_MINEFIELDS)
                || (phase == IGame.Phase.PHASE_MOVEMENT)
                || (phase == IGame.Phase.PHASE_FIRING)
                || (phase == IGame.Phase.PHASE_PHYSICAL)
                || (phase == IGame.Phase.PHASE_OFFBOARD)
                || (phase == IGame.Phase.PHASE_TARGETING)
                || (phase == IGame.Phase.PHASE_DEPLOYMENT)) {
            viewLOSSetting.setEnabled(true);
            viewMiniMap.setEnabled(true);
            viewZoomIn.setEnabled(true);
            viewZoomOut.setEnabled(true);
            viewUnitOverview.setEnabled(true);
            viewPlayerList.setEnabled(true);
            viewChangeTheme.setEnabled(false);
            toggleIsometric.setEnabled(true);
            toggleFieldOfFire.setEnabled(true);
            toggleFovHighlight.setEnabled(true);
            toggleFovDarken.setEnabled(true);
            toggleFiringSolutions.setEnabled(true);
            viewMovementEnvelope.setEnabled(true);
            viewMovModEnvelope.setEnabled(true);
        }
        // We're in-game, but not in a phase with map functions.
        else {
            viewLOSSetting.setEnabled(false);
            viewMiniMap.setEnabled(false);
            viewZoomIn.setEnabled(false);
            viewZoomOut.setEnabled(false);
            viewUnitOverview.setEnabled(false);
            viewPlayerList.setEnabled(false);
            viewChangeTheme.setEnabled(false);
            toggleIsometric.setEnabled(false);
            toggleFieldOfFire.setEnabled(false);
            toggleFovHighlight.setEnabled(false);
            toggleFovDarken.setEnabled(false);
            toggleFiringSolutions.setEnabled(false);
            viewMovementEnvelope.setEnabled(false);
            viewMovModEnvelope.setEnabled(false);
        }

        // We can only view the round report in certain phases.
        if ((phase == IGame.Phase.PHASE_INITIATIVE)
                || (phase == IGame.Phase.PHASE_MOVEMENT)
                || (phase == IGame.Phase.PHASE_FIRING)
                || (phase == IGame.Phase.PHASE_PHYSICAL)
                || (phase == IGame.Phase.PHASE_OFFBOARD)
                || (phase == IGame.Phase.PHASE_TARGETING)
                || (phase == IGame.Phase.PHASE_END)
                || (phase == IGame.Phase.PHASE_DEPLOYMENT)) {
            viewRoundReport.setEnabled(true);
        } else {
            viewRoundReport.setEnabled(false);
        }

        if ((phase != IGame.Phase.PHASE_FIRING) || (entity == null)) {
            fireCancel.setEnabled(false);
        } else {
            fireCancel.setEnabled(true);
        }

        // Some menu items can always be used
        viewClientSettings.setEnabled(true);
        viewIncGUIScale.setEnabled(true);
        viewDecGUIScale.setEnabled(true);
        viewResetWindowPositions.setEnabled(true);
        fileRefreshCache.setEnabled(true);

        updateSaveWeaponOrderMenuItem();
    }

    public synchronized void updateSaveWeaponOrderMenuItem() {
        if (entity != null) {
            WeaponOrderHandler.WeaponOrder storedWeapOrder = 
                    WeaponOrderHandler.getWeaponOrder(entity.getChassis(), entity.getModel());
            WeaponOrderHandler.WeaponOrder newWeapOrder = new WeaponOrderHandler.WeaponOrder();
            newWeapOrder.orderType = entity.getWeaponSortOrder();
            newWeapOrder.customWeaponOrderMap = entity.getCustomWeaponOrder();
            boolean isDefault = (storedWeapOrder == null)
                    && (newWeapOrder.orderType == WeaponSortOrder.DEFAULT);
            fireSaveWeaponOrder.setEnabled(!newWeapOrder.equals(storedWeapOrder) && !isDefault);
        } else {
            fireSaveWeaponOrder.setEnabled(false);
        }
    }

    /**
     * Identify to the menu bar that a <code>IGame</code> is available to the
     * parent.
     *
     * @param selected - The <code>IGame</code> that is currently selected.
     *            When there is no selection, set this to <code>null</code>.
     */
    public synchronized void setGame(IGame selected) {
        game = selected;
        manageMenu();
    }

    /**
     * Identify to the menu bar that a <code>Board</code> is available to the
     * parent.
     *
     * @param available - <code>true</code> when a <code>Board</code> is
     *            available. Set this value to <code>false</code> after the
     *            <code>Board</code> is cleared.
     */
    public synchronized void setBoard(boolean available) {
        hasBoard = available;
        manageMenu();
    }

    /**
     * Identify to the menu bar that a unit list is available to the parent.
     *
     * @param available - <code>true</code> when a unit list is available. Set
     *            this value to <code>false</code> after the unit list is
     *            cleared.
     */
    public synchronized void setUnitList(boolean available) {
        // manageMenu sets unit menus based on phase not from this setUnitListFlag.
        manageMenu();
    }

    /**
     * Identify to the menu bar that a <code>Entity</code> is available to the
     * parent.
     *
     * @param selected - The <code>Entity</code> that is currently selected.
     *            When there is no selection, set this to <code>null</code>.
     */
    public synchronized void setEntity(Entity selected) {
        entity = selected;
        manageMenu();
    }

    /**
     * Identify to the menu bar which phase is currently in progress
     *
     * @param current - the <code>int</code> value of the current phase (the
     *            valid values for this argument are defined as constants in the
     *            <code>Game</code> class).
     */
    public synchronized void setPhase(IGame.Phase current) {
        entity = null;
        phase = current;
        // There are certain phases where we shouldn't allow the board to be
        //  saved, however the vast majority of phases should allow it
        switch (current) {
            case PHASE_STARTING_SCENARIO:
            case PHASE_UNKNOWN:
            case PHASE_LOUNGE:
            case PHASE_SELECTION:
            case PHASE_EXCHANGE:
                setBoard(false);
                break;
            default:
                setBoard(true);
                break;
        }
        manageMenu();
    }
    
    public synchronized void setEnabled(String command, boolean enabled) {
        if (!itemMap.containsKey(command)) {
            MegaMek.getLogger().error("ActionCommand " + command + " not recognized.");
            return;
        }
        itemMap.get(command).setEnabled(enabled);
    }

    public synchronized void setDeployConventionalEnabled(int nbr) {
        deployMinesConventional.setText(Messages.getString("Minefield", nbr)); 
        deployMinesConventional.setEnabled(nbr > 0);
    }

    public synchronized void setDeployCommandEnabled(int nbr) {
        deployMinesCommand.setText(Messages.getString("Command", nbr)); 
        // Cannot ever deploy command mines...
        deployMinesCommand.setEnabled(false);
    }

    public synchronized void setDeployVibrabombEnabled(int nbr) {
        deployMinesVibrabomb.setText(Messages.getString("Vibrabomb", nbr)); 
        deployMinesVibrabomb.setEnabled(nbr > 0);
    }

    public synchronized void setDeployActiveEnabled(int nbr) {
        deployMinesActive.setText(Messages.getString("Active", nbr)); 
        deployMinesActive.setEnabled(nbr > 0);
    }

    public synchronized void setDeployInfernoEnabled(int nbr) {
        deployMinesInferno.setText(Messages.getString("Inferno", nbr)); 
        deployMinesInferno.setEnabled(nbr > 0);
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent e) {
        if (e.getName().equals(GUIPreferences.USE_ISOMETRIC)) {
            toggleIsometric.setSelected((Boolean)e.getNewValue());
        } else if (e.getName().equals(GUIPreferences.SHOW_FIELD_OF_FIRE)) {
            toggleFieldOfFire.setSelected((Boolean)e.getNewValue());
        } else if (e.getName().equals(GUIPreferences.SHOW_KEYBINDS_OVERLAY)) {
            viewKeybindsOverlay.setSelected((Boolean)e.getNewValue());
        }
    }

    public void die() {
        GUIPreferences.getInstance().removePreferenceChangeListener(this);        
    }
    
    /* Convenience method to shorten calls */
    private String getString(String ident) {
        return Messages.getString("CommonMenuBar." + ident);
    }
}
