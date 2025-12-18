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

    private Label multiplayerGarbageLabel;
    private Scrollbar multiplayerGarbageScrollbar;
    private Label multiplayerGarbageValueLabel;

    private ToggleButton endlessModeButton;
    private Label endlessModeLabel;

    private Button backButton;

    public RoomOptionsMenu() {
        super();

        Label title = new Label("Room Options");

        // Game Speed Start
        gameSpeedStartLabel = new Label("Start Speed:");
        gameSpeedStartScrollbar = new Scrollbar(Scrollbar.Orientation.HORIZONTAL);
        gameSpeedStartScrollbar.setMinMaxValue(0, 100);
        gameSpeedStartScrollbar.addCallback(new Runnable() {
            public void run() {
                updateValues();
            }
        });
        gameSpeedStartValueLabel = new Label("0%");

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
                .addGroup(insideScrollPaneLayout.createSequentialGroup().addWidget(multiplayerGarbageLabel).addWidget(multiplayerGarbageScrollbar).addWidget(multiplayerGarbageValueLabel))
                .addGroup(insideScrollPaneLayout.createSequentialGroup().addWidget(endlessModeLabel).addWidget(endlessModeButton))
                .addWidget(backButton)
        );

        insideScrollPaneLayout.setVerticalGroup(
            insideScrollPaneLayout.createSequentialGroup()
                .addWidget(title)
                .addGroup(insideScrollPaneLayout.createParallelGroup().addWidget(gameSpeedStartLabel).addWidget(gameSpeedStartScrollbar).addWidget(gameSpeedStartValueLabel))
                .addGroup(insideScrollPaneLayout.createParallelGroup().addWidget(multiplayerGarbageLabel).addWidget(multiplayerGarbageScrollbar).addWidget(multiplayerGarbageValueLabel))
                .addGroup(insideScrollPaneLayout.createParallelGroup().addWidget(endlessModeLabel).addWidget(endlessModeButton))
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
            multiplayerGarbageScrollbar.setValue((int)(room.multiplayer_GarbageMultiplier * 100));
            endlessModeButton.setActive(room.endlessMode);
            updateLabels();
        }
    }

    private void updateValues() {
        if(room != null) {
            room.gameSpeedStart = gameSpeedStartScrollbar.getValue() / 100.0f;
            room.multiplayer_GarbageMultiplier = multiplayerGarbageScrollbar.getValue() / 100.0f;
            room.endlessMode = endlessModeButton.isActive();
            updateLabels();
        }
    }

    private void updateLabels() {
        gameSpeedStartValueLabel.setText(gameSpeedStartScrollbar.getValue() + "%");
        multiplayerGarbageValueLabel.setText(multiplayerGarbageScrollbar.getValue() + "%");
    }
}
