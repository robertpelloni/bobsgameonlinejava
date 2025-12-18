package com.bobsgame.client.engine.game.gui;

import com.bobsgame.client.engine.game.nd.bobsgame.game.Room;
import com.bobsgame.client.engine.game.nd.bobsgame.game.GameSequence;
import com.bobsgame.client.engine.game.nd.bobsgame.game.GameType;
import com.bobsgame.client.engine.game.nd.bobsgame.game.DifficultyType;
import com.bobsgame.client.engine.game.nd.bobsgame.BobsGame;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ComboBox;
import de.matthiasmann.twl.model.SimpleChangableListModel;

public class GameSetupMenu extends MenuPanel {

    private Room room;

    private Label titleLabel;

    private Label gameLabel;
    private Label gameValueLabel;

    private Label difficultyLabel;
    private ComboBox<DifficultyType> difficultyComboBox;
    private SimpleChangableListModel<DifficultyType> difficultyModel;

    private Button roomOptionsButton;
    private Button startButton;
    private Button backButton;

    public GameSetupMenu() {
        super();

        titleLabel = new Label("Game Setup");
        //titleLabel.setTheme("titleLabel");

        gameLabel = new Label("Game:");
        gameValueLabel = new Label("");

        difficultyLabel = new Label("Difficulty:");
        difficultyModel = new SimpleChangableListModel<DifficultyType>();
        difficultyComboBox = new ComboBox<DifficultyType>(difficultyModel);

        roomOptionsButton = new Button("Room Options...");
        roomOptionsButton.addCallback(new Runnable() {
            public void run() {
                GUIManager().openRoomOptionsMenu();
            }
        });

        startButton = new Button("Start Game");
        startButton.addCallback(new Runnable() {
            public void run() {
                startGame();
            }
        });

        backButton = new Button("Back");
        backButton.addCallback(new Runnable() {
            public void run() {
                GUIManager().openGameSelector(); // Go back to selector
            }
        });

        insideScrollPaneLayout.setHorizontalGroup(
            insideScrollPaneLayout.createParallelGroup()
                .addWidget(titleLabel)
                .addGroup(insideScrollPaneLayout.createSequentialGroup().addWidget(gameLabel).addWidget(gameValueLabel))
                .addGroup(insideScrollPaneLayout.createSequentialGroup().addWidget(difficultyLabel).addWidget(difficultyComboBox))
                .addWidget(roomOptionsButton)
                .addWidget(startButton)
                .addWidget(backButton)
        );

        insideScrollPaneLayout.setVerticalGroup(
            insideScrollPaneLayout.createSequentialGroup()
                .addWidget(titleLabel)
                .addGroup(insideScrollPaneLayout.createParallelGroup().addWidget(gameLabel).addWidget(gameValueLabel))
                .addGroup(insideScrollPaneLayout.createParallelGroup().addWidget(difficultyLabel).addWidget(difficultyComboBox))
                .addWidget(roomOptionsButton)
                .addWidget(startButton)
                .addWidget(backButton)
        );
    }

    public void setRoom(Room r) {
        this.room = r;
        refresh();
    }

    public void refresh() {
        if(room == null) return;

        if(room.gameSequence != null) {
            gameValueLabel.setText(room.gameSequence.name);

            difficultyModel.clear();
            // Assuming the first game type dictates difficulties available, or we use a standard set
            // For now, let's use the first game type's difficulties if available
            if(room.gameSequence.gameTypes.size() > 0) {
                GameType gt = room.gameSequence.gameTypes.get(0);
                for(DifficultyType dt : gt.difficultyTypes) {
                    difficultyModel.addElement(dt);
                }
            }

            // Select current difficulty
            if(room.room_DifficultyName != null && !room.room_DifficultyName.isEmpty()) {
                for(int i=0; i<difficultyModel.getNumEntries(); i++) {
                    if(difficultyModel.getEntry(i).name.equals(room.room_DifficultyName)) {
                        difficultyComboBox.setSelected(i);
                        break;
                    }
                }
            } else if (difficultyModel.getNumEntries() > 0) {
                difficultyComboBox.setSelected(0);
            }
        }
    }

    private void startGame() {
        if(room != null) {
            int sel = difficultyComboBox.getSelected();
            if(sel >= 0) {
                DifficultyType dt = difficultyModel.getEntry(sel);
                room.room_DifficultyName = dt.name;

                // Propagate difficulty to sequence for GameLogic to pick up
                // (This matches C++ logic where difficulty name is stored in sequence)
                if(room.gameSequence != null) {
                    room.gameSequence.currentDifficultyName = dt.name;
                }
            }

            if(GUIManager() != null && GUIManager().ND() != null && GUIManager().ND().getGame() instanceof BobsGame) {
                 BobsGame bg = (BobsGame)GUIManager().ND().getGame();
                 bg.ME.currentRoom = this.room; // Set the configured room
                 bg.ME.setGameSequence(this.room.gameSequence); // This triggers initGame which reads from currentRoom
                 GUIManager().closeAllMenusAndND();
                 GUIManager().ND().setActivated(true);
            }
        }
    }
}
