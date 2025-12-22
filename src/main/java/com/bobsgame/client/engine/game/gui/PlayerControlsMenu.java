package com.bobsgame.client.engine.game.gui;

import com.bobsgame.client.engine.game.nd.bobsgame.game.GameLogic;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.model.SimpleBooleanModel;

public class PlayerControlsMenu extends MenuPanel {

    private GameLogic gameLogic;

    private ToggleButton slamWithUpButton;
    private SimpleBooleanModel slamWithUpModel;

    private ToggleButton slamLockButton;
    private SimpleBooleanModel slamLockModel;

    private ToggleButton singleDownLockButton;
    private SimpleBooleanModel singleDownLockModel;

    private ToggleButton doubleDownLockButton;
    private SimpleBooleanModel doubleDownLockModel;

    private Button backButton;

    public PlayerControlsMenu() {
        super();

        Label title = new Label("Player Control Settings");

        slamWithUpModel = new SimpleBooleanModel();
        slamWithUpModel.addCallback(new Runnable() { public void run() { updateValues(); } });
        slamWithUpButton = new ToggleButton(slamWithUpModel);
        slamWithUpButton.setText("Slam With Up");

        slamLockModel = new SimpleBooleanModel();
        slamLockModel.addCallback(new Runnable() { public void run() { updateValues(); } });
        slamLockButton = new ToggleButton(slamLockModel);
        slamLockButton.setText("Slam Lock");

        singleDownLockModel = new SimpleBooleanModel();
        singleDownLockModel.addCallback(new Runnable() { public void run() { updateValues(); } });
        singleDownLockButton = new ToggleButton(singleDownLockModel);
        singleDownLockButton.setText("Single Down Lock");

        doubleDownLockModel = new SimpleBooleanModel();
        doubleDownLockModel.addCallback(new Runnable() { public void run() { updateValues(); } });
        doubleDownLockButton = new ToggleButton(doubleDownLockModel);
        doubleDownLockButton.setText("Double Down Lock");

        backButton = new Button("Done");
        backButton.addCallback(new Runnable() {
            public void run() {
                GUIManager().closePlayerControlsMenu();
            }
        });

        insideScrollPaneLayout.setHorizontalGroup(
            insideScrollPaneLayout.createParallelGroup()
                .addWidget(title)
                .addWidget(slamWithUpButton)
                .addWidget(slamLockButton)
                .addWidget(singleDownLockButton)
                .addWidget(doubleDownLockButton)
                .addWidget(backButton)
        );

        insideScrollPaneLayout.setVerticalGroup(
            insideScrollPaneLayout.createSequentialGroup()
                .addWidget(title)
                .addWidget(slamWithUpButton)
                .addWidget(slamLockButton)
                .addWidget(singleDownLockButton)
                .addWidget(doubleDownLockButton)
                .addWidget(backButton)
        );
    }

    public void setGameLogic(GameLogic gl) {
        this.gameLogic = gl;
        refresh();
    }

    public void refresh() {
        if(gameLogic != null) {
            slamWithUpModel.setValue(gameLogic.slamWithUp);
            slamLockModel.setValue(gameLogic.slamLock);
            singleDownLockModel.setValue(gameLogic.singleDownLock);
            doubleDownLockModel.setValue(gameLogic.doubleDownLock);
        }
    }

    private void updateValues() {
        if(gameLogic != null) {
            gameLogic.slamWithUp = slamWithUpModel.getValue();
            gameLogic.slamLock = slamLockModel.getValue();
            gameLogic.singleDownLock = singleDownLockModel.getValue();
            gameLogic.doubleDownLock = doubleDownLockModel.getValue();
        }
    }
}
