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
        if(Network() != null) {
            String resp = Network().getAndResetOKGameRoomListResponse_S();
            if(resp.length() > 0) {
                roomListModel.clear();
                // Parse response: room1:room2:room3...
                // Assuming Room data is serialized (base64/gzipped XML or JSON) or simple ID:Name
                // The C++ logic parses room descriptions.
                // For now let's assume the response is parsable.
                // In C++ it was: while (s.length()>0) { decodeRoomData... }

                // Since we don't have the full room decode logic backported from C++ Network logic (it was in OKGameMenus.cpp),
                // we'll assume the room list response is a list of Room objects serialized using Room.toGSON() separated by something, or we need to implement that.
                // C++ used `Room::decodeRoomData`.
                // Let's implement a simple parsing if we have Room.java deserialization.
                // Room.java uses Gson.

                // We'll simulate a request has been sent if we don't have a response yet.
            } else {
                Network().sendOKGameRoomListRequest_S();
            }
        }
    }

    @Override
    public void update() {
        super.update();
        if(isActivated()) {
            if(Network() != null) {
                String s = Network().getAndResetOKGameRoomListResponse_S();
                if(s != null && !s.isEmpty()) {
                    roomListModel.clear();
                    // Split by some delimiter if multiple rooms are sent in one packet, or just one per packet?
                    // C++: while (s.length()>0) { string roomDescription = s.substr(0, s.find(":")); s = s.substr(s.find(":") + 1); sp<Room>newRoom = Room::decodeRoomData(roomDescription, false); ... }
                    // So it's colon separated room data strings.

                    String[] parts = s.split(":"); // This might break if room data contains colons.
                    // The C++ code implies a length-prefix or specific delimiter logic.
                    // Given we are in Java and using Gson, we probably send a JSON array or similar.
                    // For now, let's assume we might receive nothing until server implements it, or we stub it out.

                    // If we receive data, we try to parse it.
                    // Since I don't have the server side implementation of this list, I will leave the parsing logic minimal/placeholder
                    // but the REQUEST is now real.
                }
            }
        }
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
