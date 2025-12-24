package com.bobsgame.client.engine.game.gui;

import java.util.ArrayList;

import com.bobsgame.client.LWJGLUtils;
import com.bobsgame.client.engine.EnginePart;
import com.bobsgame.client.engine.game.ClientGameEngine;
import com.bobsgame.client.engine.game.FriendCharacter;
import com.bobsgame.client.engine.game.gui.gameStore.GameStore;
import com.bobsgame.client.engine.game.gui.stuffMenu.StuffMenu;
import com.bobsgame.client.state.KeyboardScreen;



import de.matthiasmann.twl.GUI;

//=========================================================================================================================
public class GUIManager extends EnginePart
{//=========================================================================================================================



	public StuffMenu stuffMenu = null;
	public GUI stuffMenuGUI = null;

	public GameStore gameStore = null;
	public GUI gameStoreGUI = null;

	public PlayerEditMenu playerEditMenu = null;
	public GUI playerEditGUI = null;

    public com.bobsgame.client.engine.game.gui.gameSequenceEditor.GameSequenceEditor gameSequenceEditor = null;
    public GUI gameSequenceEditorGUI = null;

    public com.bobsgame.client.engine.game.gui.customGameEditor.CustomGameEditor customGameEditor = null;
    public GUI customGameEditorGUI = null;

    public com.bobsgame.client.engine.game.gui.GameSelector gameSelector = null;
    public GUI gameSelectorGUI = null;

    public com.bobsgame.client.engine.game.gui.GameSetupMenu gameSetupMenu = null;
    public GUI gameSetupMenuGUI = null;

    public com.bobsgame.client.engine.game.gui.RoomOptionsMenu roomOptionsMenu = null;
    public GUI roomOptionsMenuGUI = null;

    public com.bobsgame.client.engine.game.gui.PlayerControlsMenu playerControlsMenu = null;
    public GUI playerControlsMenuGUI = null;

    public com.bobsgame.client.engine.game.gui.gameTestMenu.GameTestMenu gameTestMenu = null;
    public GUI gameTestMenuGUI = null;

    public com.bobsgame.client.engine.game.gui.LoginMenu loginMenu = null;
    public GUI loginMenuGUI = null;

    public com.bobsgame.client.engine.game.gui.CreateAccountMenu createAccountMenu = null;
    public GUI createAccountMenuGUI = null;

    public com.bobsgame.client.engine.game.gui.LocalMultiplayerMenu localMultiplayerMenu = null;
    public GUI localMultiplayerMenuGUI = null;

    public com.bobsgame.client.engine.game.gui.NetworkLobbyMenu networkLobbyMenu = null;
    public GUI networkLobbyMenuGUI = null;

	public ArrayList<GameChallengeNotificationPanel> gameChallenges = new ArrayList<GameChallengeNotificationPanel>();
	public ArrayList<GUI> gameChallengesGUIs = new ArrayList<GUI>();

	public KeyboardScreen keyboardScreen;
	public GUI keyboardScreenGUI;


	//this panel (with plate)
	static public String lightThemeString = "lightMenu";
	static public String darkThemeString = "darkMenu";


	//child panel (with plate)
	static public String subMenuPanelDialogLayoutTheme = "subMenuPanelDialogLayout";

	//scrollpane here

	//panel inside scrollpane
	static public String emptyDialogLayoutTheme = "invisibleScrollPaneContentContainerDialogLayout";

	static public String buttonTheme = "oppositeThemeButton";
	static public String checkboxTheme = "checkbox";
	static public String scrollPaneTheme = "themedScrollPane";

	public boolean lightTheme = false;

	//=========================================================================================================================
	public GUIManager(ClientGameEngine g)
	{//=========================================================================================================================


		super(g);


		//------------------------------
		// login screen
		//------------------------------

		//this has to be run before making the gui, something in the createThemeManager function delays the texture creation or something

		//game.update(1000);//this will start the map cam already scrolled
		//game.render();
		//map().draw(1);
		//glClear(GL_COLOR_BUFFER_BIT);












		stuffMenu = new StuffMenu();
		stuffMenuGUI = new GUI(stuffMenu, LWJGLUtils.TWLrenderer);
		stuffMenuGUI.applyTheme(LWJGLUtils.TWLthemeManager);


		gameStore = new GameStore();
		gameStoreGUI = new GUI(gameStore, LWJGLUtils.TWLrenderer);
		gameStoreGUI.applyTheme(LWJGLUtils.TWLthemeManager);

		playerEditMenu = new PlayerEditMenu();
		playerEditGUI = new GUI(playerEditMenu, LWJGLUtils.TWLrenderer);
		playerEditGUI.applyTheme(LWJGLUtils.TWLthemeManager);

        gameSequenceEditor = new com.bobsgame.client.engine.game.gui.gameSequenceEditor.GameSequenceEditor();
        gameSequenceEditorGUI = new GUI(gameSequenceEditor, LWJGLUtils.TWLrenderer);
        gameSequenceEditorGUI.applyTheme(LWJGLUtils.TWLthemeManager);

        customGameEditor = new com.bobsgame.client.engine.game.gui.customGameEditor.CustomGameEditor();
        customGameEditorGUI = new GUI(customGameEditor, LWJGLUtils.TWLrenderer);
        customGameEditorGUI.applyTheme(LWJGLUtils.TWLthemeManager);

        gameSelector = new com.bobsgame.client.engine.game.gui.GameSelector();
        gameSelectorGUI = new GUI(gameSelector, LWJGLUtils.TWLrenderer);
        gameSelectorGUI.applyTheme(LWJGLUtils.TWLthemeManager);

        gameSetupMenu = new com.bobsgame.client.engine.game.gui.GameSetupMenu();
        gameSetupMenuGUI = new GUI(gameSetupMenu, LWJGLUtils.TWLrenderer);
        gameSetupMenuGUI.applyTheme(LWJGLUtils.TWLthemeManager);

        roomOptionsMenu = new com.bobsgame.client.engine.game.gui.RoomOptionsMenu();
        roomOptionsMenuGUI = new GUI(roomOptionsMenu, LWJGLUtils.TWLrenderer);
        roomOptionsMenuGUI.applyTheme(LWJGLUtils.TWLthemeManager);

        playerControlsMenu = new com.bobsgame.client.engine.game.gui.PlayerControlsMenu();
        playerControlsMenuGUI = new GUI(playerControlsMenu, LWJGLUtils.TWLrenderer);
        playerControlsMenuGUI.applyTheme(LWJGLUtils.TWLthemeManager);

        gameTestMenu = new com.bobsgame.client.engine.game.gui.gameTestMenu.GameTestMenu();
        gameTestMenuGUI = new GUI(gameTestMenu, LWJGLUtils.TWLrenderer);
        gameTestMenuGUI.applyTheme(LWJGLUtils.TWLthemeManager);

        loginMenu = new com.bobsgame.client.engine.game.gui.LoginMenu();
        loginMenuGUI = new GUI(loginMenu, LWJGLUtils.TWLrenderer);
        loginMenuGUI.applyTheme(LWJGLUtils.TWLthemeManager);

        createAccountMenu = new com.bobsgame.client.engine.game.gui.CreateAccountMenu();
        createAccountMenuGUI = new GUI(createAccountMenu, LWJGLUtils.TWLrenderer);
        createAccountMenuGUI.applyTheme(LWJGLUtils.TWLthemeManager);

        localMultiplayerMenu = new com.bobsgame.client.engine.game.gui.LocalMultiplayerMenu();
        localMultiplayerMenuGUI = new GUI(localMultiplayerMenu, LWJGLUtils.TWLrenderer);
        localMultiplayerMenuGUI.applyTheme(LWJGLUtils.TWLthemeManager);

        networkLobbyMenu = new com.bobsgame.client.engine.game.gui.NetworkLobbyMenu();
        networkLobbyMenuGUI = new GUI(networkLobbyMenu, LWJGLUtils.TWLrenderer);
        networkLobbyMenuGUI.applyTheme(LWJGLUtils.TWLthemeManager);

		keyboardScreen = new KeyboardScreen();
		keyboardScreenGUI = new GUI(keyboardScreen, LWJGLUtils.TWLrenderer);
		keyboardScreenGUI.applyTheme(LWJGLUtils.TWLthemeManager);

	}


	//=========================================================================================================================
	public void init()
	{//=========================================================================================================================

		stuffMenu.init();
		//loadingScreen.init();
		//gameStore.init();
		playerEditMenu.init();


	}




	//=========================================================================================================================
	public void update()
	{//=========================================================================================================================


		//set x, y, timer

		stuffMenu.update();
		gameStore.update();
		playerEditMenu.update();
		keyboardScreen.update();
        gameSequenceEditor.update();
        customGameEditor.update();
        gameSelector.update();
        gameSetupMenu.update();
        roomOptionsMenu.update();
        playerControlsMenu.update();
        gameTestMenu.update();
        loginMenu.update();
        createAccountMenu.update();
        localMultiplayerMenu.update();
        networkLobbyMenu.update();

		for(int i=0;i<gameChallenges.size();i++)
		{
			GameChallengeNotificationPanel g = gameChallenges.get(i);
			g.update();
		}

	}



	//=========================================================================================================================
	public void render()
	{//=========================================================================================================================


		//render


		if(stuffMenu.isActivated()){stuffMenu.renderBefore();stuffMenuGUI.update();stuffMenu.render();}
		if(gameStore.isActivated()){gameStore.renderBefore();gameStoreGUI.update();gameStore.render();}
		if(playerEditMenu.isActivated()){playerEditMenu.renderBefore();playerEditGUI.update();playerEditMenu.render();}
		if(keyboardScreen.isActivated()){keyboardScreen.renderBefore();keyboardScreenGUI.update();keyboardScreen.render();}
        if(gameSequenceEditor.isActivated()){gameSequenceEditor.renderBefore();gameSequenceEditorGUI.update();gameSequenceEditor.render();}
        if(customGameEditor.isActivated()){customGameEditor.renderBefore();customGameEditorGUI.update();customGameEditor.render();}
        if(gameSelector.isActivated()){gameSelector.renderBefore();gameSelectorGUI.update();gameSelector.render();}
        if(gameSetupMenu.isActivated()){gameSetupMenu.renderBefore();gameSetupMenuGUI.update();gameSetupMenu.render();}
        if(roomOptionsMenu.isActivated()){roomOptionsMenu.renderBefore();roomOptionsMenuGUI.update();roomOptionsMenu.render();}
        if(playerControlsMenu.isActivated()){playerControlsMenu.renderBefore();playerControlsMenuGUI.update();playerControlsMenu.render();}
        if(gameTestMenu.isActivated()){gameTestMenu.renderBefore();gameTestMenuGUI.update();gameTestMenu.render();}
        if(loginMenu.isActivated()){loginMenu.renderBefore();loginMenuGUI.update();loginMenu.render();}
        if(createAccountMenu.isActivated()){createAccountMenu.renderBefore();createAccountMenuGUI.update();createAccountMenu.render();}
        if(localMultiplayerMenu.isActivated()){localMultiplayerMenu.renderBefore();localMultiplayerMenuGUI.update();localMultiplayerMenu.render();}
        if(networkLobbyMenu.isActivated()){networkLobbyMenu.renderBefore();networkLobbyMenuGUI.update();networkLobbyMenu.render();}

		for(int i=0;i<gameChallenges.size();i++)
		{
			GameChallengeNotificationPanel g = gameChallenges.get(i);
			GUI gui = gameChallengesGUIs.get(i);
			if(g.isActivated()){g.renderBefore();gui.update();g.render();}
		}
	}


	//=========================================================================================================================
	public synchronized GameChallengeNotificationPanel makeGameChallengeNotification(FriendCharacter friend, String gameName)
	{//=========================================================================================================================
		GameChallengeNotificationPanel g = new GameChallengeNotificationPanel(friend,gameName);
		gameChallenges.add(g);

		GUI gui = new GUI(g, LWJGLUtils.TWLrenderer);
		gui.applyTheme(LWJGLUtils.TWLthemeManager);
		gameChallengesGUIs.add(gui);

		g.setActivated(true);

		return g;

	}

	//=========================================================================================================================
	public void removeGameNotification(GameChallengeNotificationPanel g)
	{//=========================================================================================================================

		for(int i=0;i<gameChallenges.size();i++)
		{
			if(gameChallenges.get(i)==g)
			{
				gameChallenges.remove(i);
				GUI gui = gameChallengesGUIs.remove(i);

				gui.destroy();
			}
		}
	}





	//=========================================================================================================================
	public void cleanup()
	{//=========================================================================================================================


		//loadingScreenGUI.destroy();
		stuffMenuGUI.destroy();

		gameStoreGUI.destroy();
		playerEditGUI.destroy();
		keyboardScreenGUI.destroy();
        gameSequenceEditorGUI.destroy();
        customGameEditorGUI.destroy();
        gameSelectorGUI.destroy();
        gameSetupMenuGUI.destroy();
        roomOptionsMenuGUI.destroy();
        playerControlsMenuGUI.destroy();
        gameTestMenuGUI.destroy();
        loginMenuGUI.destroy();
        createAccountMenuGUI.destroy();
        localMultiplayerMenuGUI.destroy();
        networkLobbyMenuGUI.destroy();

		for(int i=0;i<gameChallenges.size();i++)
		{
			gameChallenges.remove(i);
			GUI gui = gameChallengesGUIs.remove(i);
			gui.destroy();
		}
	}

	//=========================================================================================================================
	public void setDarkTheme()
	{//=========================================================================================================================

		lightTheme=false;

		StatusBar().setDarkTheme();


		stuffMenu.mainPanelLayout.setTheme(GUIManager.darkThemeString);
		stuffMenu.mainPanelLayout.reapplyTheme();

		gameStore.mainPanelLayout.setTheme(GUIManager.darkThemeString);
		gameStore.mainPanelLayout.reapplyTheme();

		playerEditMenu.mainPanelLayout.setTheme(GUIManager.darkThemeString);
		playerEditMenu.mainPanelLayout.reapplyTheme();

        gameSequenceEditor.mainPanelLayout.setTheme(GUIManager.darkThemeString);
        gameSequenceEditor.mainPanelLayout.reapplyTheme();

        customGameEditor.mainPanelLayout.setTheme(GUIManager.darkThemeString);
        customGameEditor.mainPanelLayout.reapplyTheme();

        gameSelector.mainPanelLayout.setTheme(GUIManager.darkThemeString);
        gameSelector.mainPanelLayout.reapplyTheme();

        gameSetupMenu.mainPanelLayout.setTheme(GUIManager.darkThemeString);
        gameSetupMenu.mainPanelLayout.reapplyTheme();

        roomOptionsMenu.mainPanelLayout.setTheme(GUIManager.darkThemeString);
        roomOptionsMenu.mainPanelLayout.reapplyTheme();

        playerControlsMenu.mainPanelLayout.setTheme(GUIManager.darkThemeString);
        playerControlsMenu.mainPanelLayout.reapplyTheme();

        gameTestMenu.mainPanelLayout.setTheme(GUIManager.darkThemeString);
        gameTestMenu.mainPanelLayout.reapplyTheme();

        loginMenu.mainPanelLayout.setTheme(GUIManager.darkThemeString);
        loginMenu.mainPanelLayout.reapplyTheme();

        createAccountMenu.mainPanelLayout.setTheme(GUIManager.darkThemeString);
        createAccountMenu.mainPanelLayout.reapplyTheme();

        localMultiplayerMenu.mainPanelLayout.setTheme(GUIManager.darkThemeString);
        localMultiplayerMenu.mainPanelLayout.reapplyTheme();

        networkLobbyMenu.mainPanelLayout.setTheme(GUIManager.darkThemeString);
        networkLobbyMenu.mainPanelLayout.reapplyTheme();

		keyboardScreen.mainPanelLayout.setTheme(GUIManager.darkThemeString);
		keyboardScreen.mainPanelLayout.reapplyTheme();

		for(int i=0;i<gameChallenges.size();i++)
		{
			gameChallenges.get(i).mainPanelLayout.setTheme(GUIManager.darkThemeString);
			gameChallenges.get(i).mainPanelLayout.reapplyTheme();
		}

	}

	//=========================================================================================================================
	public void setLightTheme()
	{//=========================================================================================================================

		lightTheme=true;

		StatusBar().setLightTheme();

		stuffMenu.mainPanelLayout.setTheme(GUIManager.lightThemeString);
		stuffMenu.mainPanelLayout.reapplyTheme();

		gameStore.mainPanelLayout.setTheme(GUIManager.lightThemeString);
		gameStore.mainPanelLayout.reapplyTheme();

		playerEditMenu.mainPanelLayout.setTheme(GUIManager.lightThemeString);
		playerEditMenu.mainPanelLayout.reapplyTheme();

        gameSequenceEditor.mainPanelLayout.setTheme(GUIManager.lightThemeString);
        gameSequenceEditor.mainPanelLayout.reapplyTheme();

        customGameEditor.mainPanelLayout.setTheme(GUIManager.lightThemeString);
        customGameEditor.mainPanelLayout.reapplyTheme();

        gameSelector.mainPanelLayout.setTheme(GUIManager.lightThemeString);
        gameSelector.mainPanelLayout.reapplyTheme();

        gameSetupMenu.mainPanelLayout.setTheme(GUIManager.lightThemeString);
        gameSetupMenu.mainPanelLayout.reapplyTheme();

        roomOptionsMenu.mainPanelLayout.setTheme(GUIManager.lightThemeString);
        roomOptionsMenu.mainPanelLayout.reapplyTheme();

        playerControlsMenu.mainPanelLayout.setTheme(GUIManager.lightThemeString);
        playerControlsMenu.mainPanelLayout.reapplyTheme();

        gameTestMenu.mainPanelLayout.setTheme(GUIManager.lightThemeString);
        gameTestMenu.mainPanelLayout.reapplyTheme();

        loginMenu.mainPanelLayout.setTheme(GUIManager.lightThemeString);
        loginMenu.mainPanelLayout.reapplyTheme();

        createAccountMenu.mainPanelLayout.setTheme(GUIManager.lightThemeString);
        createAccountMenu.mainPanelLayout.reapplyTheme();

        localMultiplayerMenu.mainPanelLayout.setTheme(GUIManager.lightThemeString);
        localMultiplayerMenu.mainPanelLayout.reapplyTheme();

        networkLobbyMenu.mainPanelLayout.setTheme(GUIManager.lightThemeString);
        networkLobbyMenu.mainPanelLayout.reapplyTheme();

		keyboardScreen.mainPanelLayout.setTheme(GUIManager.lightThemeString);
		keyboardScreen.mainPanelLayout.reapplyTheme();

		for(int i=0;i<gameChallenges.size();i++)
		{
			gameChallenges.get(i).mainPanelLayout.setTheme(GUIManager.lightThemeString);
			gameChallenges.get(i).mainPanelLayout.reapplyTheme();
		}
	}


	//=========================================================================================================================
	public void openND()
	{//=========================================================================================================================
		closeAllMenusAndND();
		ND().setActivated(true);

	}


	//=========================================================================================================================
	public void closeND()
	{//=========================================================================================================================

		if(ND().isActivated()==true)
		{
			ND().toggleActivated();
		}

	}


	//=========================================================================================================================
	public void openGameStore()
	{//=========================================================================================================================
		closeAllMenusAndND();


	}


	//=========================================================================================================================
	public void openSettingsMenu()
	{//=========================================================================================================================
		openStuffMenu();

		StuffMenu().openSubMenu(StuffMenu().settingsPanel);
	}


	//=========================================================================================================================
	public void openFriendsMenu()
	{//=========================================================================================================================
		openStuffMenu();

		StuffMenu().openSubMenu(StuffMenu().friendsPanel);
	}


	//=========================================================================================================================
	public void openStatusMenu()
	{//=========================================================================================================================
		openStuffMenu();

		StuffMenu().openSubMenu(StuffMenu().statusPanel);
	}


	//=========================================================================================================================
	public void openLogMenu()
	{//=========================================================================================================================
		openStuffMenu();

		StuffMenu().openSubMenu(StuffMenu().logsPanel);

	}


	//=========================================================================================================================
	public void openItemsMenu()
	{//=========================================================================================================================

		openStuffMenu();

		StuffMenu().openSubMenu(StuffMenu().itemsPanel);

	}


	//=========================================================================================================================
	public void openStuffMenu()
	{//=========================================================================================================================

		ND().setActivated(false);
		GameStore().setActivated(false);
        gameSequenceEditor.setActivated(false);
        customGameEditor.setActivated(false);
        gameSelector.setActivated(false);
        gameSetupMenu.setActivated(false);
        roomOptionsMenu.setActivated(false);
        playerControlsMenu.setActivated(false);
        gameTestMenu.setActivated(false);
        loginMenu.setActivated(false);
        createAccountMenu.setActivated(false);
        localMultiplayerMenu.setActivated(false);
        networkLobbyMenu.setActivated(false);

		StuffMenu().setActivated(true);

	}

    public void openGameSequenceEditor() {
        closeAllMenusAndND();
        gameSequenceEditor.setActivated(true);
    }

    public void openCustomGameEditor() {
        closeAllMenusAndND();
        customGameEditor.setActivated(true);
    }

    public void openGameSelector() {
        closeAllMenusAndND();
        gameSelector.setActivated(true);
    }

    public void openGameSetupMenu(com.bobsgame.client.engine.game.nd.bobsgame.game.GameSequence seq) {
        closeAllMenusAndND();

        // Initialize a new Room with this sequence
        com.bobsgame.client.engine.game.nd.bobsgame.game.Room room = new com.bobsgame.client.engine.game.nd.bobsgame.game.Room();
        room.gameSequence = seq;
        room.setDefaults();

        gameSetupMenu.setRoom(room);
        gameSetupMenu.setActivated(true);
    }

    public void openRoomOptionsMenu() {
        closeAllMenusAndND();
        // Pass the room from setup menu? Or assume GameSetupMenu holds state.
        // GameSetupMenu persists while hidden?
        // For simplicity, let's just show it. It shares the room reference via GameSetupMenu.
        // Ideally we pass it.
        // Let's assume GameSetupMenu is the "owner" of the current config session.
        // But we closed all menus.

        // Actually, let's keep GameSetupMenu active?
        // No, closeAllMenusAndND hides them.

        // Let's just have roomOptionsMenu get the room from gameSetupMenu?
        // Or better, pass it.
        // Refactoring openRoomOptionsMenu to take Room

        // For now, let's cheat and grab it from gameSetupMenu if we can, or rely on caller?
        // The caller was GameSetupMenu.

        // Let's modify the signature in next step or assume GameSetupMenu passed it.
        // Actually GameSetupMenu called GUIManager().openRoomOptionsMenu().
        // I'll fix this in GameSetupMenu.java to pass the room.

        roomOptionsMenu.setActivated(true);
    }

    public void openRoomOptionsMenu(com.bobsgame.client.engine.game.nd.bobsgame.game.Room room) {
        closeAllMenusAndND();
        roomOptionsMenu.setRoom(room);
        roomOptionsMenu.setActivated(true);
    }

    public void closeRoomOptionsMenu() {
        closeAllMenusAndND();
        gameSetupMenu.setActivated(true);
    }

    public void openPlayerControlsMenu(com.bobsgame.client.engine.game.nd.bobsgame.game.GameLogic logic) {
        closeAllMenusAndND();
        playerControlsMenu.setGameLogic(logic);
        playerControlsMenu.setActivated(true);
    }

    public void closePlayerControlsMenu() {
        closeAllMenusAndND();
        gameSetupMenu.setActivated(true);
    }

    public void openGameTestMenu() {
        closeAllMenusAndND();
        gameTestMenu.setActivated(true);
    }

    public void closeGameTestMenu() {
        closeAllMenusAndND();
        stuffMenu.setActivated(true); // Return to main menu
    }

    public void openLoginMenu() {
        closeAllMenusAndND();
        loginMenu.setActivated(true);
    }

    public void closeLoginMenu() {
        closeAllMenusAndND();
        stuffMenu.setActivated(true);
    }

    public void openCreateAccountMenu() {
        closeAllMenusAndND();
        createAccountMenu.setActivated(true);
    }

    public void closeCreateAccountMenu() {
        closeAllMenusAndND();
        loginMenu.setActivated(true);
    }

    public void openLocalMultiplayerMenu() {
        closeAllMenusAndND();
        localMultiplayerMenu.setActivated(true);
    }

    public void closeLocalMultiplayerMenu() {
        closeAllMenusAndND();
        stuffMenu.setActivated(true); // Should go to Start Screen? StuffMenu is Main Menu here?
    }

    public void openNetworkLobbyMenu() {
        closeAllMenusAndND();
        networkLobbyMenu.setActivated(true);
    }

    public void closeNetworkLobbyMenu() {
        closeAllMenusAndND();
        stuffMenu.setActivated(true);
    }


	//=========================================================================================================================
	public void enableAllMenusAndND()
	{//=========================================================================================================================

		keyboardScreen.setEnabled(true);
		ND().setEnabled(true);
		GameStore().setEnabled(true);
		StuffMenu().setEnabled(true);

	}


	//=========================================================================================================================
	public void disableAllMenusAndND()
	{//=========================================================================================================================

		closeAllMenusAndND();

		keyboardScreen.setEnabled(false);
		ND().setEnabled(false);
		GameStore().setEnabled(false);
		StuffMenu().setEnabled(false);

	}


	//=========================================================================================================================
	public void closeAllMenusAndND()
	{//=========================================================================================================================

		keyboardScreen.setActivated(false);
		closeND();
		GameStore().setActivated(false);
		StuffMenu().setActivated(false);
        gameSequenceEditor.setActivated(false);
        customGameEditor.setActivated(false);
        gameSelector.setActivated(false);
        gameSetupMenu.setActivated(false);
        roomOptionsMenu.setActivated(false);
        playerControlsMenu.setActivated(false);
        gameTestMenu.setActivated(false);
        loginMenu.setActivated(false);
        createAccountMenu.setActivated(false);
        localMultiplayerMenu.setActivated(false);
        networkLobbyMenu.setActivated(false);
	}








}
