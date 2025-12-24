package com.bobsgame.client.engine.game.gui;

import com.bobsgame.client.engine.game.nd.bobsgame.game.Room;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.Scrollbar;
import de.matthiasmann.twl.ToggleButton;

public class RoomOptionsMenu extends MenuPanel {

    private Room room;

    private Label gameSpeedStartLabel;
    private Scrollbar gameSpeedStartScrollbar;
    private Label gameSpeedStartValueLabel;

    private Label gameSpeedChangeLabel;
    private Scrollbar gameSpeedChangeScrollbar;
    private Label gameSpeedChangeValueLabel;

    private Label gameSpeedMaxLabel;
    private Scrollbar gameSpeedMaxScrollbar;
    private Label gameSpeedMaxValueLabel;

    private Label floorMoveLabel;
    private Scrollbar floorMoveScrollbar;
    private Label floorMoveValueLabel;

    private Label lockDelayLabel;
    private Scrollbar lockDelayScrollbar;
    private Label lockDelayValueLabel;

    private Label spawnDelayLabel;
    private Scrollbar spawnDelayScrollbar;
    private Label spawnDelayValueLabel;

    private Label multiplayerGarbageLabel;
    private Scrollbar multiplayerGarbageScrollbar;
    private Label multiplayerGarbageValueLabel;

    private ToggleButton endlessModeButton;
    private Label endlessModeLabel;

    private ToggleButton privateRoomButton;
    private Label privateRoomLabel;

    private ToggleButton tournamentRoomButton;
    private Label tournamentRoomLabel;

    private Label maxPlayersLabel;
    private Scrollbar maxPlayersScrollbar;
    private Label maxPlayersValueLabel;

    private Label sendGarbageToLabel;
    private Scrollbar sendGarbageToScrollbar;
    private Label sendGarbageToValueLabel;

    private Button backButton;

    public RoomOptionsMenu() {
        super();

        Label title = new Label("Room Options");

        // Game Speed Start
        gameSpeedStartLabel = new Label("Start Speed:");
        gameSpeedStartScrollbar = new Scrollbar(Scrollbar.Orientation.HORIZONTAL);
        gameSpeedStartScrollbar.setMinMaxValue(0, 100);
        gameSpeedStartScrollbar.addCallback(new Runnable() { public void run() { updateValues(); } });
        gameSpeedStartValueLabel = new Label("0%");

        // Game Speed Change
        gameSpeedChangeLabel = new Label("Speed Change Rate:");
        gameSpeedChangeScrollbar = new Scrollbar(Scrollbar.Orientation.HORIZONTAL);
        gameSpeedChangeScrollbar.setMinMaxValue(0, 200); // Represents -100% to +100%? Or 0-200%? Logic in C++: -1.0 to 1.0. So 0-200, val-100 / 100.
        gameSpeedChangeScrollbar.setValue(102); // default 0.02
        gameSpeedChangeScrollbar.addCallback(new Runnable() { public void run() { updateValues(); } });
        gameSpeedChangeValueLabel = new Label("2%");

        // Game Speed Max
        gameSpeedMaxLabel = new Label("Max Speed:");
        gameSpeedMaxScrollbar = new Scrollbar(Scrollbar.Orientation.HORIZONTAL);
        gameSpeedMaxScrollbar.setMinMaxValue(0, 500); // 0-5.0
        gameSpeedMaxScrollbar.setValue(100);
        gameSpeedMaxScrollbar.addCallback(new Runnable() { public void run() { updateValues(); } });
        gameSpeedMaxValueLabel = new Label("100%");

        // Floor Move Limit
        floorMoveLabel = new Label("Floor Moves:");
        floorMoveScrollbar = new Scrollbar(Scrollbar.Orientation.HORIZONTAL);
        floorMoveScrollbar.setMinMaxValue(-1, 128);
        floorMoveScrollbar.setValue(-1);
        floorMoveScrollbar.addCallback(new Runnable() { public void run() { updateValues(); } });
        floorMoveValueLabel = new Label("Unlimited");

        // Lock Delay
        lockDelayLabel = new Label("Lock Delay (ms):");
        lockDelayScrollbar = new Scrollbar(Scrollbar.Orientation.HORIZONTAL);
        lockDelayScrollbar.setMinMaxValue(0, 5000);
        lockDelayScrollbar.setValue(500);
        lockDelayScrollbar.addCallback(new Runnable() { public void run() { updateValues(); } });
        lockDelayValueLabel = new Label("500ms");

        // Spawn Delay
        spawnDelayLabel = new Label("Spawn Delay (ms):");
        spawnDelayScrollbar = new Scrollbar(Scrollbar.Orientation.HORIZONTAL);
        spawnDelayScrollbar.setMinMaxValue(0, 2000);
        spawnDelayScrollbar.setValue(0);
        spawnDelayScrollbar.addCallback(new Runnable() { public void run() { updateValues(); } });
        spawnDelayValueLabel = new Label("0ms");

        // Garbage Multiplier
        multiplayerGarbageLabel = new Label("Garbage Multiplier:");
        multiplayerGarbageScrollbar = new Scrollbar(Scrollbar.Orientation.HORIZONTAL);
        multiplayerGarbageScrollbar.setMinMaxValue(0, 200);
        multiplayerGarbageScrollbar.setValue(100);
        multiplayerGarbageScrollbar.addCallback(new Runnable() {
            public void run() {
                updateValues();
            }
        });
        multiplayerGarbageValueLabel = new Label("100%");

        // Endless Mode
        endlessModeLabel = new Label("Endless Mode");
        endlessModeButton = new ToggleButton("");
        endlessModeButton.addCallback(new Runnable() {
            public void run() {
                updateValues();
            }
        });
        endlessModeLabel.setLabelFor(endlessModeButton);

        // Private Room
        privateRoomLabel = new Label("Private Room");
        privateRoomButton = new ToggleButton("");
        privateRoomButton.addCallback(new Runnable() { public void run() { updateValues(); } });
        privateRoomLabel.setLabelFor(privateRoomButton);

        // Tournament Room
        tournamentRoomLabel = new Label("Tournament Mode");
        tournamentRoomButton = new ToggleButton("");
        tournamentRoomButton.addCallback(new Runnable() { public void run() { updateValues(); } });
        tournamentRoomLabel.setLabelFor(tournamentRoomButton);

        // Max Players
        maxPlayersLabel = new Label("Max Players (0=Unl):");
        maxPlayersScrollbar = new Scrollbar(Scrollbar.Orientation.HORIZONTAL);
        maxPlayersScrollbar.setMinMaxValue(0, 50); // Arbitrary max
        maxPlayersScrollbar.addCallback(new Runnable() { public void run() { updateValues(); } });
        maxPlayersValueLabel = new Label("0");

        // Send Garbage To
        sendGarbageToLabel = new Label("Send Garbage To:");
        sendGarbageToScrollbar = new Scrollbar(Scrollbar.Orientation.HORIZONTAL);
        sendGarbageToScrollbar.setMinMaxValue(0, 4); // 0-4 options
        sendGarbageToScrollbar.addCallback(new Runnable() { public void run() { updateValues(); } });
        sendGarbageToValueLabel = new Label("All");


        backButton = new Button("Done");
        backButton.addCallback(new Runnable() {
            public void run() {
                GUIManager().closeRoomOptionsMenu();
            }
        });

        insideScrollPaneLayout.setHorizontalGroup(
            insideScrollPaneLayout.createParallelGroup()
                .addWidget(title)
                .addGroup(insideScrollPaneLayout.createSequentialGroup().addWidget(gameSpeedStartLabel).addWidget(gameSpeedStartScrollbar).addWidget(gameSpeedStartValueLabel))
                .addGroup(insideScrollPaneLayout.createSequentialGroup().addWidget(gameSpeedChangeLabel).addWidget(gameSpeedChangeScrollbar).addWidget(gameSpeedChangeValueLabel))
                .addGroup(insideScrollPaneLayout.createSequentialGroup().addWidget(gameSpeedMaxLabel).addWidget(gameSpeedMaxScrollbar).addWidget(gameSpeedMaxValueLabel))
                .addGroup(insideScrollPaneLayout.createSequentialGroup().addWidget(floorMoveLabel).addWidget(floorMoveScrollbar).addWidget(floorMoveValueLabel))
                .addGroup(insideScrollPaneLayout.createSequentialGroup().addWidget(lockDelayLabel).addWidget(lockDelayScrollbar).addWidget(lockDelayValueLabel))
                .addGroup(insideScrollPaneLayout.createSequentialGroup().addWidget(spawnDelayLabel).addWidget(spawnDelayScrollbar).addWidget(spawnDelayValueLabel))
                .addGroup(insideScrollPaneLayout.createSequentialGroup().addWidget(multiplayerGarbageLabel).addWidget(multiplayerGarbageScrollbar).addWidget(multiplayerGarbageValueLabel))
                .addGroup(insideScrollPaneLayout.createSequentialGroup().addWidget(endlessModeLabel).addWidget(endlessModeButton))
                .addGroup(insideScrollPaneLayout.createSequentialGroup().addWidget(privateRoomLabel).addWidget(privateRoomButton))
                .addGroup(insideScrollPaneLayout.createSequentialGroup().addWidget(tournamentRoomLabel).addWidget(tournamentRoomButton))
                .addGroup(insideScrollPaneLayout.createSequentialGroup().addWidget(maxPlayersLabel).addWidget(maxPlayersScrollbar).addWidget(maxPlayersValueLabel))
                .addGroup(insideScrollPaneLayout.createSequentialGroup().addWidget(sendGarbageToLabel).addWidget(sendGarbageToScrollbar).addWidget(sendGarbageToValueLabel))
                .addWidget(backButton)
        );

        insideScrollPaneLayout.setVerticalGroup(
            insideScrollPaneLayout.createSequentialGroup()
                .addWidget(title)
                .addGroup(insideScrollPaneLayout.createParallelGroup().addWidget(gameSpeedStartLabel).addWidget(gameSpeedStartScrollbar).addWidget(gameSpeedStartValueLabel))
                .addGroup(insideScrollPaneLayout.createParallelGroup().addWidget(gameSpeedChangeLabel).addWidget(gameSpeedChangeScrollbar).addWidget(gameSpeedChangeValueLabel))
                .addGroup(insideScrollPaneLayout.createParallelGroup().addWidget(gameSpeedMaxLabel).addWidget(gameSpeedMaxScrollbar).addWidget(gameSpeedMaxValueLabel))
                .addGroup(insideScrollPaneLayout.createParallelGroup().addWidget(floorMoveLabel).addWidget(floorMoveScrollbar).addWidget(floorMoveValueLabel))
                .addGroup(insideScrollPaneLayout.createParallelGroup().addWidget(lockDelayLabel).addWidget(lockDelayScrollbar).addWidget(lockDelayValueLabel))
                .addGroup(insideScrollPaneLayout.createParallelGroup().addWidget(spawnDelayLabel).addWidget(spawnDelayScrollbar).addWidget(spawnDelayValueLabel))
                .addGroup(insideScrollPaneLayout.createParallelGroup().addWidget(multiplayerGarbageLabel).addWidget(multiplayerGarbageScrollbar).addWidget(multiplayerGarbageValueLabel))
                .addGroup(insideScrollPaneLayout.createParallelGroup().addWidget(endlessModeLabel).addWidget(endlessModeButton))
                .addGroup(insideScrollPaneLayout.createParallelGroup().addWidget(privateRoomLabel).addWidget(privateRoomButton))
                .addGroup(insideScrollPaneLayout.createParallelGroup().addWidget(tournamentRoomLabel).addWidget(tournamentRoomButton))
                .addGroup(insideScrollPaneLayout.createParallelGroup().addWidget(maxPlayersLabel).addWidget(maxPlayersScrollbar).addWidget(maxPlayersValueLabel))
                .addGroup(insideScrollPaneLayout.createParallelGroup().addWidget(sendGarbageToLabel).addWidget(sendGarbageToScrollbar).addWidget(sendGarbageToValueLabel))
                .addWidget(backButton)
        );
    }

    public void setRoom(Room r) {
        this.room = r;
        refresh();
    }

    public void refresh() {
        if(room != null) {
            gameSpeedStartScrollbar.setValue((int)(room.gameSpeedStart * 100));
            gameSpeedChangeScrollbar.setValue((int)((room.gameSpeedChangeRate + 1.0f) * 100));
            gameSpeedMaxScrollbar.setValue((int)(room.gameSpeedMaximum * 100));
            floorMoveScrollbar.setValue(room.floorSpinLimit);
            lockDelayScrollbar.setValue(room.lockDelayMinimum);
            spawnDelayScrollbar.setValue(room.spawnDelayMinimum);

            multiplayerGarbageScrollbar.setValue((int)(room.multiplayer_GarbageMultiplier * 100));
            endlessModeButton.setActive(room.endlessMode);
            privateRoomButton.setActive(room.multiplayer_PrivateRoom);
            tournamentRoomButton.setActive(room.multiplayer_TournamentRoom);
            maxPlayersScrollbar.setValue(room.multiplayer_MaxPlayers);
            sendGarbageToScrollbar.setValue(room.multiplayer_SendGarbageTo);
            updateLabels();
        }
    }

    private void updateValues() {
        if(room != null) {
            room.gameSpeedStart = gameSpeedStartScrollbar.getValue() / 100.0f;
            room.gameSpeedChangeRate = (gameSpeedChangeScrollbar.getValue() / 100.0f) - 1.0f;
            room.gameSpeedMaximum = gameSpeedMaxScrollbar.getValue() / 100.0f;
            room.floorSpinLimit = floorMoveScrollbar.getValue();
            room.lockDelayMinimum = lockDelayScrollbar.getValue();
            room.spawnDelayMinimum = spawnDelayScrollbar.getValue();

            room.multiplayer_GarbageMultiplier = multiplayerGarbageScrollbar.getValue() / 100.0f;
            room.endlessMode = endlessModeButton.isActive();
            room.multiplayer_PrivateRoom = privateRoomButton.isActive();
            room.multiplayer_TournamentRoom = tournamentRoomButton.isActive();
            room.multiplayer_MaxPlayers = maxPlayersScrollbar.getValue();
            room.multiplayer_SendGarbageTo = sendGarbageToScrollbar.getValue();
            updateLabels();
        }
    }

    private void updateLabels() {
        gameSpeedStartValueLabel.setText(gameSpeedStartScrollbar.getValue() + "%");
        gameSpeedChangeValueLabel.setText((gameSpeedChangeScrollbar.getValue() - 100) + "%");
        gameSpeedMaxValueLabel.setText(gameSpeedMaxScrollbar.getValue() + "%");
        floorMoveValueLabel.setText(floorMoveScrollbar.getValue() == -1 ? "Unlimited" : ""+floorMoveScrollbar.getValue());
        lockDelayValueLabel.setText(lockDelayScrollbar.getValue() + "ms");
        spawnDelayValueLabel.setText(spawnDelayScrollbar.getValue() + "ms");

        multiplayerGarbageValueLabel.setText(multiplayerGarbageScrollbar.getValue() + "%");
        maxPlayersValueLabel.setText(maxPlayersScrollbar.getValue() == 0 ? "Unlimited" : String.valueOf(maxPlayersScrollbar.getValue()));

        String sendTo = "Unknown";
        switch(sendGarbageToScrollbar.getValue()) {
            case 0: sendTo = "All"; break;
            case 1: sendTo = "All 50%"; break;
            case 2: sendTo = "Random"; break;
            case 3: sendTo = "Rotate"; break;
            case 4: sendTo = "Least Blocks"; break;
        }
        sendGarbageToValueLabel.setText(sendTo);
    }
}
