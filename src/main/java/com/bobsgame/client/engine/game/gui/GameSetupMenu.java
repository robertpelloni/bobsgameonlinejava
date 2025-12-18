package com.bobsgame.client.engine.game.gui;

import com.bobsgame.client.engine.game.nd.bobsgame.game.Room;
import com.bobsgame.client.engine.game.nd.bobsgame.game.GameSequence;
import com.bobsgame.client.engine.game.nd.bobsgame.game.GameType;
import com.bobsgame.client.engine.game.nd.bobsgame.game.DifficultyType;
import com.bobsgame.client.engine.game.nd.bobsgame.BobsGame;
import com.bobsgame.net.BobNet;
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

    private Button voteUpButton;
    private Button voteDownButton;

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

        voteUpButton = new Button("Vote Up");
        voteUpButton.addCallback(new Runnable() {
            public void run() {
                vote(true);
            }
        });

        voteDownButton = new Button("Vote Down");
        voteDownButton.addCallback(new Runnable() {
            public void run() {
                vote(false);
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
                .addGroup(insideScrollPaneLayout.createSequentialGroup().addWidget(voteUpButton).addWidget(voteDownButton))
                .addWidget(startButton)
                .addWidget(backButton)
        );

        insideScrollPaneLayout.setVerticalGroup(
            insideScrollPaneLayout.createSequentialGroup()
                .addWidget(titleLabel)
                .addGroup(insideScrollPaneLayout.createParallelGroup().addWidget(gameLabel).addWidget(gameValueLabel))
                .addGroup(insideScrollPaneLayout.createParallelGroup().addWidget(difficultyLabel).addWidget(difficultyComboBox))
                .addWidget(roomOptionsButton)
                .addGroup(insideScrollPaneLayout.createParallelGroup().addWidget(voteUpButton).addWidget(voteDownButton))
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

            // Update Vote Buttons
            String vote = room.gameSequence.yourVote;
            if(room.gameSequence.gameTypes.size() == 1) {
                vote = room.gameSequence.gameTypes.get(0).yourVote;
            }

            if("up".equals(vote)) {
                voteUpButton.setText("Voted Up!");
                voteDownButton.setText("Vote Down");
            } else if("down".equals(vote)) {
                voteUpButton.setText("Vote Up");
                voteDownButton.setText("Voted Down!");
            } else {
                voteUpButton.setText("Vote Up");
                voteDownButton.setText("Vote Down");
            }

            boolean canVote = !room.gameSequence.downloaded; // If not downloaded (i.e. built-in), maybe disable?
            // C++: if (g->downloaded == false) continue; (filters OUT non-downloaded for voting list)
            // So we should only vote on downloaded games.
            // But built-in games might be "downloaded=false".
            // Let's enable voting always for now, server handles it.

        }
    }

    private void vote(boolean up) {
        if(GUIManager() == null || GUIManager().ND() == null || !(GUIManager().ND().getGame() instanceof BobsGame)) return;
        BobsGame bg = (BobsGame)GUIManager().ND().getGame();

        if(room == null || room.gameSequence == null) return;

        String uuid = "";
        String type = "GameSequence";
        if(room.gameSequence.gameTypes.size() == 1) {
            uuid = room.gameSequence.gameTypes.get(0).uuid;
            type = "GameType";
        } else {
            uuid = room.gameSequence.uuid;
        }

        String vote = up ? "up" : "down";

        // BobNet.Bobs_Game_GameTypesAndSequences_Vote_Request + type + ":" + uuid + ":" + vote + ":" + BobNet.endline
        String packet = BobNet.Bobs_Game_GameTypesAndSequences_Vote_Request + type + ":" + uuid + ":" + vote + ":" + BobNet.endline;

        if(Network() != null) {
            Network().connectAndAuthorizeAndWriteToChannel(packet);
        }

        // Optimistic update
        if(room.gameSequence.gameTypes.size() == 1) {
             room.gameSequence.gameTypes.get(0).yourVote = vote;
        } else {
             room.gameSequence.yourVote = vote;
        }
        refresh();
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
