package com.bobsgame.client.engine.game.gui;

import java.util.ArrayList;
import com.bobsgame.client.engine.game.nd.bobsgame.game.GameLogic;
import com.bobsgame.client.engine.game.nd.bobsgame.game.GameSequence;
import com.bobsgame.client.engine.game.nd.bobsgame.game.GameType;
import com.bobsgame.client.engine.game.nd.bobsgame.game.Room;
import com.bobsgame.client.engine.game.Player;
import com.bobsgame.client.engine.game.nd.bobsgame.BobsGame;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ListBox;
import de.matthiasmann.twl.model.SimpleChangableListModel;

public class NetworkLobbyMenu extends MenuPanel {

    private Label statusLabel;
    private Button joinRoomButton;
    private Button createRoomButton;
    private Button backButton;

    private ListBox<Room> roomList;
    private SimpleChangableListModel<Room> roomListModel;

    private Button refreshButton;

    // Placeholder for actual network logic interaction

    public NetworkLobbyMenu() {
        super();

        Label title = new Label("Network Multiplayer Lobby");

        statusLabel = new Label("Logged in as: User");

        roomListModel = new SimpleChangableListModel<Room>();
        roomList = new ListBox<Room>(roomListModel);

        refreshButton = new Button("Refresh Rooms");
        refreshButton.addCallback(new Runnable() {
            public void run() {
                refreshRooms();
            }
        });

        joinRoomButton = new Button("Join Selected Room");
        joinRoomButton.addCallback(new Runnable() {
            public void run() {
                int sel = roomList.getSelected();
                if(sel >= 0) {
                    Room r = roomListModel.getEntry(sel);
                    joinRoom(r);
                }
            }
        });

        createRoomButton = new Button("Create Room");
        createRoomButton.addCallback(new Runnable() {
            public void run() {
                createRoom();
            }
        });

        backButton = new Button("Back");
        backButton.addCallback(new Runnable() {
            public void run() {
                GUIManager().closeNetworkLobbyMenu();
            }
        });

        insideScrollPaneLayout.setHorizontalGroup(
            insideScrollPaneLayout.createParallelGroup()
                .addWidget(title)
                .addWidget(statusLabel)
                .addWidget(roomList)
                .addGroup(insideScrollPaneLayout.createSequentialGroup().addWidget(refreshButton).addWidget(joinRoomButton).addWidget(createRoomButton))
                .addWidget(backButton)
        );

        insideScrollPaneLayout.setVerticalGroup(
            insideScrollPaneLayout.createSequentialGroup()
                .addWidget(title)
                .addWidget(statusLabel)
                .addWidget(roomList)
                .addGroup(insideScrollPaneLayout.createParallelGroup().addWidget(refreshButton).addWidget(joinRoomButton).addWidget(createRoomButton))
                .addWidget(backButton)
        );
    }

    private void refreshRooms() {
        // Mockup: Request rooms from server
        // In real impl, this would send a packet and wait for response to populate list
        roomListModel.clear();
        // Add dummy room for testing if list empty?
    }

    private void joinRoom(Room r) {
        // Join logic
        // This likely involves setting the current room in GameLogic and transitioning to a 'Lobby Wait' or 'Player Join' screen
    }

    private void createRoom() {
        // Transition to RoomOptionsMenu to configure, then become host
        if(GUIManager() != null) {
            GUIManager().openRoomOptionsMenu(); // Config room first
            // Ideally we'd pass a flag saying "Creating Network Room"
        }
    }

    @Override
    public void setActivated(boolean active) {
        super.setActivated(active);
        if(active) {
            refreshRooms();
        }
    }
}
