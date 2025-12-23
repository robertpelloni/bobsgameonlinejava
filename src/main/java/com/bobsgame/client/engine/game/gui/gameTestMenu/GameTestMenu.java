package com.bobsgame.client.engine.game.gui.gameTestMenu;

import com.bobsgame.client.engine.game.gui.MenuPanel;
import com.bobsgame.client.engine.game.gui.GUIManager;
import com.bobsgame.client.engine.game.nd.bobsgame.game.GameSequence;
import com.bobsgame.client.engine.game.nd.bobsgame.game.GameType;
import com.bobsgame.client.engine.game.nd.bobsgame.game.GameFileUtils;
import com.bobsgame.client.engine.game.nd.bobsgame.game.GameLogic;
import com.bobsgame.client.engine.game.nd.bobsgame.game.DifficultyType;
import com.bobsgame.net.BobNet;
import com.bobsgame.client.engine.game.nd.bobsgame.BobsGame;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ListBox;
import de.matthiasmann.twl.TabbedPane;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.ComboBox;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.model.SimpleChangableListModel;
import de.matthiasmann.twl.model.SimpleBooleanModel;

public class GameTestMenu extends MenuPanel {

    private TabbedPane tabbedPane;

    // Single Game Types Tab
    private ListBox<GameType> gameTypesList;
    private SimpleChangableListModel<GameType> gameTypesModel;

    // Game Sequences Tab
    private ListBox<GameSequence> gameSequencesList;
    private SimpleChangableListModel<GameSequence> gameSequencesModel;

    // Right panel (Preview/Control)
    private Label selectedGameLabel;
    private Button resetButton;
    private ComboBox<DifficultyType> difficultyBox;
    private SimpleChangableListModel<DifficultyType> difficultyModel;
    private Button upVoteButton;
    private Button downVoteButton;
    private Button exitButton;
    private ToggleButton hideVotedButton;
    private SimpleBooleanModel hideVotedModel;

    private GameSequence currentPreviewSequence;

    public GameTestMenu() {
        super();

        tabbedPane = new TabbedPane();

        // --- Game Types Tab ---
        gameTypesModel = new SimpleChangableListModel<GameType>();
        gameTypesList = new ListBox<GameType>(gameTypesModel);
        gameTypesList.getSelectionModel().addCallback(new Runnable() {
            public void run() {
                onGameTypeSelect();
            }
        });

        DialogLayout typesLayout = new DialogLayout();
        typesLayout.setHorizontalGroup(typesLayout.createParallelGroup().addWidget(gameTypesList));
        typesLayout.setVerticalGroup(typesLayout.createParallelGroup().addWidget(gameTypesList));
        tabbedPane.addTab("Single Game Types", typesLayout);

        // --- Game Sequences Tab ---
        gameSequencesModel = new SimpleChangableListModel<GameSequence>();
        gameSequencesList = new ListBox<GameSequence>(gameSequencesModel);
        gameSequencesList.getSelectionModel().addCallback(new Runnable() {
            public void run() {
                onGameSequenceSelect();
            }
        });

        DialogLayout sequencesLayout = new DialogLayout();
        sequencesLayout.setHorizontalGroup(sequencesLayout.createParallelGroup().addWidget(gameSequencesList));
        sequencesLayout.setVerticalGroup(sequencesLayout.createParallelGroup().addWidget(gameSequencesList));
        tabbedPane.addTab("Game Sequences", sequencesLayout);


        // --- Right Controls ---
        selectedGameLabel = new Label("Current Game: None");

        hideVotedModel = new SimpleBooleanModel();
        hideVotedModel.setValue(true);
        hideVotedModel.addCallback(new Runnable() { public void run() { refreshLists(); } });
        hideVotedButton = new ToggleButton(hideVotedModel);
        hideVotedButton.setText("Hide Voted");

        resetButton = new Button("Reset / Preview");
        resetButton.addCallback(new Runnable() {
            public void run() {
                initPreviewGame();
            }
        });

        difficultyModel = new SimpleChangableListModel<DifficultyType>();
        difficultyBox = new ComboBox<DifficultyType>(difficultyModel);

        upVoteButton = new Button("Up Vote");
        upVoteButton.addCallback(new Runnable() { public void run() { vote(true); } });

        downVoteButton = new Button("Down Vote");
        downVoteButton.addCallback(new Runnable() { public void run() { vote(false); } });

        exitButton = new Button("Exit To Title");
        exitButton.addCallback(new Runnable() {
            public void run() {
                // Return to game selector or main menu
                GUIManager().closeGameTestMenu();
            }
        });


        // --- Main Layout ---
        insideScrollPaneLayout.setHorizontalGroup(
            insideScrollPaneLayout.createSequentialGroup()
                .addWidget(tabbedPane)
                .addGroup(insideScrollPaneLayout.createParallelGroup()
                    .addWidget(hideVotedButton)
                    .addWidget(selectedGameLabel)
                    .addWidget(resetButton)
                    .addWidget(difficultyBox)
                    .addGroup(insideScrollPaneLayout.createSequentialGroup().addWidget(upVoteButton).addWidget(downVoteButton))
                    .addWidget(exitButton)
                )
        );

        insideScrollPaneLayout.setVerticalGroup(
            insideScrollPaneLayout.createParallelGroup()
                .addWidget(tabbedPane)
                .addGroup(insideScrollPaneLayout.createSequentialGroup()
                    .addWidget(hideVotedButton)
                    .addWidget(selectedGameLabel)
                    .addWidget(resetButton)
                    .addWidget(difficultyBox)
                    .addGroup(insideScrollPaneLayout.createParallelGroup().addWidget(upVoteButton).addWidget(downVoteButton))
                    .addWidget(exitButton)
                )
        );

        // Populate difficulty model (using built-in as default)
        if(GameLogic.getBuiltInGameTypes().size() > 0) {
            for(DifficultyType dt : GameLogic.getBuiltInGameTypes().get(0).difficultyTypes) {
                difficultyModel.addElement(dt);
            }
            if(difficultyModel.getNumEntries()>0) difficultyBox.setSelected(0);
        }

        refreshLists();
    }

    private void refreshLists() {
        gameTypesModel.clear();
        for(GameType gt : GameFileUtils.getGameTypeList()) {
            if(shouldShow(gt.yourVote)) gameTypesModel.addElement(gt);
        }
        // Also add built-ins
        for(GameType gt : GameLogic.getBuiltInGameTypes()) {
             if(shouldShow(gt.yourVote)) gameTypesModel.addElement(gt);
        }

        gameSequencesModel.clear();
        for(GameSequence gs : GameFileUtils.getGameSequenceList()) {
            if(shouldShow(gs.yourVote)) gameSequencesModel.addElement(gs);
        }
    }

    private boolean shouldShow(String vote) {
        if(!hideVotedModel.getValue()) return true;
        return vote == null || "none".equals(vote) || "".equals(vote);
    }

    private void onGameTypeSelect() {
        int sel = gameTypesList.getSelected();
        if(sel >= 0) {
            GameType gt = gameTypesModel.getEntry(sel);
            currentPreviewSequence = new GameSequence();
            currentPreviewSequence.gameTypes.add(gt);
            currentPreviewSequence.name = gt.name;
            selectedGameLabel.setText("Current: " + gt.name);
            initPreviewGame();
        }
    }

    private void onGameSequenceSelect() {
        int sel = gameSequencesList.getSelected();
        if(sel >= 0) {
            GameSequence gs = gameSequencesModel.getEntry(sel);
            currentPreviewSequence = gs;
            selectedGameLabel.setText("Current: " + gs.name);
            initPreviewGame();
        }
    }

    private void initPreviewGame() {
        if(currentPreviewSequence != null && GUIManager() != null && GUIManager().ND() != null && GUIManager().ND().getGame() instanceof BobsGame) {
            BobsGame bg = (BobsGame)GUIManager().ND().getGame();

            int diffSel = difficultyBox.getSelected();
            if(diffSel >= 0) {
                currentPreviewSequence.currentDifficultyName = difficultyModel.getEntry(diffSel).name;
            }

            // We want to run this in the background, but TWL renders on top.
            // BobsGame logic: if menu is open, update game in background?
            // The C++ version renders the game into an FBO and displays it on the panel.
            // For Java/TWL, we might just let the game run underneath (transparent background?) or
            // just set the game logic and let it render normally behind the UI if the UI is non-blocking.
            // But this is a MenuPanel, which usually blocks input.
            // However, this menu is "GameTestMenuControl" which docks.
            // In Java, we can just set the game sequence on the Engine.

            bg.ME.setGameSequence(currentPreviewSequence);
            bg.ME.currentRoom.endlessMode = true; // Preview usually endless?

            // To allow playing while menu is up?
            // C++: getPlayer1Game()->update(...) if windowOpen==false.
            // It seems the C++ menu covers the screen but has a preview area.
            // For this Java implementation, we'll just let the game run in the background.
            // We might need to ensure input goes to the game if not clicking UI.
            // But TWL consumes input.
            // Let's just set it for now. User might need to close menu to play fully, or we implement a "Test" mode.
            // Actually, "Preview" usually implies watching it play or testing it.
            // If "Test", we probably close the menu temporarily?
            // C++ has a preview window.
            // Let's stick to configuring it. The user can "Exit to Title" to stop.
            // Or maybe "Reset / Preview" just restarts logic.
        }
    }

    private void vote(boolean up) {
        // Same logic as GameSetupMenu
        if(currentPreviewSequence == null) return;

        String uuid = "";
        String type = "GameSequence";
        if(currentPreviewSequence.gameTypes.size() == 1) {
            uuid = currentPreviewSequence.gameTypes.get(0).uuid;
            type = "GameType";
        } else {
            uuid = currentPreviewSequence.uuid;
        }

        String vote = up ? "up" : "down";
        String packet = BobNet.Bobs_Game_GameTypesAndSequences_Vote_Request + type + ":" + uuid + ":" + vote + ":" + BobNet.endline;

        if(Network() != null) {
            Network().connectAndAuthorizeAndWriteToChannel(packet);
        }

        // Optimistic update
        if(currentPreviewSequence.gameTypes.size() == 1) {
             currentPreviewSequence.gameTypes.get(0).yourVote = vote;
        } else {
             currentPreviewSequence.yourVote = vote;
        }

        refreshLists();
    }
}
