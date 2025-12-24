package com.bobsgame.client.engine.game.gui;

import java.util.ArrayList;
import com.bobsgame.client.engine.game.nd.bobsgame.game.GameLogic;
import com.bobsgame.client.engine.game.nd.bobsgame.game.Room;
import com.bobsgame.client.engine.game.Player;
import com.bobsgame.client.engine.game.nd.bobsgame.BobsGame;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ListBox;
import de.matthiasmann.twl.model.SimpleChangableListModel;

public class LocalMultiplayerMenu extends MenuPanel {

    private Label infoLabel;
    private Button startButton;
    private Button backButton;

    // List of joined local players (keyboard, controller 1, controller 2...)
    private ListBox<String> playersList;
    private SimpleChangableListModel<String> playersListModel;

    // We need access to GameLogic's player list or similar

    public LocalMultiplayerMenu() {
        super();

        Label title = new Label("Local Multiplayer");

        infoLabel = new Label("Press Space (Keyboard) or A (Controller) to join.");

        playersListModel = new SimpleChangableListModel<String>();
        playersList = new ListBox<String>(playersListModel);

        startButton = new Button("Start Game");
        startButton.addCallback(new Runnable() {
            public void run() {
                startGame();
            }
        });

        backButton = new Button("Back");
        backButton.addCallback(new Runnable() {
            public void run() {
                GUIManager().closeLocalMultiplayerMenu();
            }
        });

        insideScrollPaneLayout.setHorizontalGroup(
            insideScrollPaneLayout.createParallelGroup()
                .addWidget(title)
                .addWidget(infoLabel)
                .addWidget(playersList)
                .addWidget(startButton)
                .addWidget(backButton)
        );

        insideScrollPaneLayout.setVerticalGroup(
            insideScrollPaneLayout.createSequentialGroup()
                .addWidget(title)
                .addWidget(infoLabel)
                .addWidget(playersList)
                .addWidget(startButton)
                .addWidget(backButton)
        );
    }

    // In update(), verify inputs and add players
    @Override
    public void update() {
        super.update();

        // This logic mimics C++ localMultiplayerPlayerJoinMenuUpdate
        // Check for Space/A presses on unassigned controllers

        // For mockup purposes, let's assume we can add a keyboard player
        if(GUIManager() != null && GUIManager().ND() != null && GUIManager().ND().getGame() instanceof BobsGame) {
             // Access GameLogic to add players?
             // Or manage a local list and push to GameLogic on start?
        }
    }

    private void startGame() {
        // Transition to GameSetupMenu to pick game rules?
        // Or if rules already picked, start game.
        // Usually: Start Screen -> Local MP -> (Join Players) -> Game Setup -> Play

        if(GUIManager() != null) {
            GUIManager().openGameSelector(); // Pick game after players join
        }
    }
}
