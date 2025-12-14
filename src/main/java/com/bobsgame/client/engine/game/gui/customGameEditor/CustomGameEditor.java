package com.bobsgame.client.engine.game.gui.customGameEditor;

import com.bobsgame.client.engine.game.gui.MenuPanel;
import com.bobsgame.client.engine.game.nd.bobsgame.game.GameType;
import com.bobsgame.client.engine.game.nd.bobsgame.game.GameFileUtils;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.EditField;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ComboBox;
import de.matthiasmann.twl.TabbedPane;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.model.SimpleChangableListModel;

public class CustomGameEditor extends MenuPanel {

    public GameType currentGameType;

    private TabbedPane tabbedPane;

    // Settings Tab
    private DialogLayout settingsPanel;
    private EditField nameField;
    private ComboBox<GameType.GameMode> gameModeBox;
    private SimpleChangableListModel<GameType.GameMode> modeModel;

    // Blocks Tab (Placeholder)
    private DialogLayout blocksPanel;

    // Pieces Tab (Placeholder)
    private DialogLayout piecesPanel;

    private Button saveButton;
    private Button loadButton;
    private Button newButton;

    public CustomGameEditor() {
        super();

        tabbedPane = new TabbedPane();

        // --- Settings Tab ---
        settingsPanel = new DialogLayout();
        nameField = new EditField();
        nameField.addCallback(new EditField.Callback() {
            public void callback(int key) {
                if(currentGameType != null) currentGameType.name = nameField.getText();
            }
        });

        modeModel = new SimpleChangableListModel<GameType.GameMode>();
        modeModel.addElement(GameType.GameMode.DROP);
        modeModel.addElement(GameType.GameMode.STACK);
        gameModeBox = new ComboBox<GameType.GameMode>(modeModel);

        settingsPanel.setHorizontalGroup(
            settingsPanel.createParallelGroup()
                .addGroup(settingsPanel.createSequentialGroup().addWidget(new Label("Name")).addWidget(nameField))
                .addGroup(settingsPanel.createSequentialGroup().addWidget(new Label("Mode")).addWidget(gameModeBox))
        );
        settingsPanel.setVerticalGroup(
            settingsPanel.createSequentialGroup()
                .addGroup(settingsPanel.createParallelGroup().addWidget(new Label("Name")).addWidget(nameField))
                .addGroup(settingsPanel.createParallelGroup().addWidget(new Label("Mode")).addWidget(gameModeBox))
        );

        tabbedPane.addTab("Settings", settingsPanel);

        // --- Blocks Tab ---
        blocksPanel = new DialogLayout();
        blocksPanel.setHorizontalGroup(blocksPanel.createSequentialGroup().addWidget(new Label("Blocks Editor (TODO)")));
        blocksPanel.setVerticalGroup(blocksPanel.createSequentialGroup().addWidget(new Label("Blocks Editor (TODO)")));
        tabbedPane.addTab("Blocks", blocksPanel);

        // --- Pieces Tab ---
        piecesPanel = new DialogLayout();
        piecesPanel.setHorizontalGroup(piecesPanel.createSequentialGroup().addWidget(new Label("Pieces Editor (TODO)")));
        piecesPanel.setVerticalGroup(piecesPanel.createSequentialGroup().addWidget(new Label("Pieces Editor (TODO)")));
        tabbedPane.addTab("Pieces", piecesPanel);


        // --- Main Buttons ---
        saveButton = new Button("Save");
        saveButton.addCallback(new Runnable() {
            public void run() {
                saveToGameType();
                if(currentGameType != null) {
                    GameFileUtils.saveGameType(currentGameType);
                }
            }
        });

        loadButton = new Button("Load");
        newButton = new Button("New");
        newButton.addCallback(new Runnable() {
            public void run() {
                setGameType(new GameType());
            }
        });


        // --- Main Layout ---
        insideScrollPaneLayout.setHorizontalGroup(
            insideScrollPaneLayout.createParallelGroup()
                .addWidget(tabbedPane)
                .addGroup(insideScrollPaneLayout.createSequentialGroup()
                    .addWidget(newButton)
                    .addWidget(saveButton)
                    .addWidget(loadButton)
                )
        );

        insideScrollPaneLayout.setVerticalGroup(
            insideScrollPaneLayout.createSequentialGroup()
                .addWidget(tabbedPane)
                .addGroup(insideScrollPaneLayout.createParallelGroup()
                    .addWidget(newButton)
                    .addWidget(saveButton)
                    .addWidget(loadButton)
                )
        );

        setGameType(new GameType());
    }

    public void setGameType(GameType type) {
        this.currentGameType = type;
        if(type != null) {
            nameField.setText(type.name);

            for(int i=0; i<modeModel.getNumEntries(); i++) {
                if(modeModel.getEntry(i) == type.gameMode) {
                    gameModeBox.setSelected(i);
                    break;
                }
            }
        }
    }

    public void saveToGameType() {
        if(currentGameType != null) {
            currentGameType.name = nameField.getText();
            int sel = gameModeBox.getSelected();
            if(sel >= 0) currentGameType.gameMode = modeModel.getEntry(sel);
        }
    }
}
