package com.bobsgame.client.engine.game.gui.customGameEditor;

import com.bobsgame.client.engine.game.gui.MenuPanel;
import com.bobsgame.client.engine.game.nd.bobsgame.game.GameType;
import com.bobsgame.client.engine.game.nd.bobsgame.game.GameFileUtils;
import com.bobsgame.client.engine.game.nd.bobsgame.game.BlockType;
import com.bobsgame.client.engine.game.nd.bobsgame.game.PieceType;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.EditField;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ComboBox;
import de.matthiasmann.twl.TabbedPane;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.ListBox;
import de.matthiasmann.twl.model.SimpleChangableListModel;

public class CustomGameEditor extends MenuPanel {

    public GameType currentGameType;

    private TabbedPane tabbedPane;

    // Settings Tab
    private DialogLayout settingsPanel;
    private EditField nameField;
    private ComboBox<GameType.GameMode> gameModeBox;
    private SimpleChangableListModel<GameType.GameMode> modeModel;

    // Blocks Tab
    private DialogLayout blocksPanel;
    private ListBox<BlockType> blockList;
    private SimpleChangableListModel<BlockType> blockListModel;
    private BlockEditorPanel blockEditorPanel;
    private Button addBlockButton;
    private Button removeBlockButton;

    // Pieces Tab
    private DialogLayout piecesPanel;
    private ListBox<PieceType> pieceList;
    private SimpleChangableListModel<PieceType> pieceListModel;
    private PieceEditorPanel pieceEditorPanel;
    private Button addPieceButton;
    private Button removePieceButton;
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

        blockListModel = new SimpleChangableListModel<BlockType>();
        blockList = new ListBox<BlockType>(blockListModel);
        blockEditorPanel = new BlockEditorPanel();

        addBlockButton = new Button("Add Block");
        addBlockButton.addCallback(new Runnable() {
            public void run() {
                if(currentGameType != null) {
                    BlockType b = new BlockType();
                    currentGameType.blockTypes.add(b);
                    refreshBlockList();
                }
            }
        });

        removeBlockButton = new Button("Remove Block");
        removeBlockButton.addCallback(new Runnable() {
            public void run() {
                int sel = blockList.getSelected();
                if(sel >= 0 && currentGameType != null) {
                    currentGameType.blockTypes.remove(sel);
                    refreshBlockList();
                    blockEditorPanel.setBlockType(null);
                }
            }
        });

        blockList.getSelectionModel().addCallback(new Runnable() {
            public void run() {
                int sel = blockList.getSelected();
                if(sel >= 0 && sel < blockListModel.getNumEntries()) {
                    blockEditorPanel.setBlockType(blockListModel.getEntry(sel));
                } else {
                    blockEditorPanel.setBlockType(null);
                }
            }
        });

        blocksPanel.setHorizontalGroup(
            blocksPanel.createSequentialGroup()
                .addGroup(blocksPanel.createParallelGroup()
                    .addWidget(blockList)
                    .addGroup(blocksPanel.createSequentialGroup().addWidget(addBlockButton).addWidget(removeBlockButton))
                )
                .addWidget(blockEditorPanel)
        );
        blocksPanel.setVerticalGroup(
            blocksPanel.createParallelGroup()
                .addGroup(blocksPanel.createSequentialGroup()
                    .addWidget(blockList)
                    .addGroup(blocksPanel.createParallelGroup().addWidget(addBlockButton).addWidget(removeBlockButton))
                )
                .addWidget(blockEditorPanel)
        );

        blocksPanel.setHorizontalGroup(blocksPanel.createSequentialGroup().addWidget(new Label("Blocks Editor (TODO)")));
        blocksPanel.setVerticalGroup(blocksPanel.createSequentialGroup().addWidget(new Label("Blocks Editor (TODO)")));
        tabbedPane.addTab("Blocks", blocksPanel);

        // --- Pieces Tab ---
        piecesPanel = new DialogLayout();

        pieceListModel = new SimpleChangableListModel<PieceType>();
        pieceList = new ListBox<PieceType>(pieceListModel);
        pieceEditorPanel = new PieceEditorPanel();

        addPieceButton = new Button("Add Piece");
        addPieceButton.addCallback(new Runnable() {
            public void run() {
                if(currentGameType != null) {
                    PieceType p = new PieceType();
                    currentGameType.pieceTypes.add(p);
                    refreshPieceList();
                }
            }
        });

        removePieceButton = new Button("Remove Piece");
        removePieceButton.addCallback(new Runnable() {
            public void run() {
                int sel = pieceList.getSelected();
                if(sel >= 0 && currentGameType != null) {
                    currentGameType.pieceTypes.remove(sel);
                    refreshPieceList();
                    pieceEditorPanel.setPieceType(null);
                }
            }
        });

        pieceList.getSelectionModel().addCallback(new Runnable() {
            public void run() {
                int sel = pieceList.getSelected();
                if(sel >= 0 && sel < pieceListModel.getNumEntries()) {
                    pieceEditorPanel.setPieceType(pieceListModel.getEntry(sel));
                } else {
                    pieceEditorPanel.setPieceType(null);
                }
            }
        });

        piecesPanel.setHorizontalGroup(
            piecesPanel.createSequentialGroup()
                .addGroup(piecesPanel.createParallelGroup()
                    .addWidget(pieceList)
                    .addGroup(piecesPanel.createSequentialGroup().addWidget(addPieceButton).addWidget(removePieceButton))
                )
                .addWidget(pieceEditorPanel)
        );
        piecesPanel.setVerticalGroup(
            piecesPanel.createParallelGroup()
                .addGroup(piecesPanel.createSequentialGroup()
                    .addWidget(pieceList)
                    .addGroup(piecesPanel.createParallelGroup().addWidget(addPieceButton).addWidget(removePieceButton))
                )
                .addWidget(pieceEditorPanel)
        );

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
            refreshBlockList();
            refreshPieceList();
        }
    }

    public void refreshBlockList() {
        blockListModel.clear();
        if(currentGameType != null) {
            for(BlockType b : currentGameType.blockTypes) {
                blockListModel.addElement(b);
            }
        }
    }

    public void refreshPieceList() {
        pieceListModel.clear();
        if(currentGameType != null) {
            for(PieceType p : currentGameType.pieceTypes) {
                pieceListModel.addElement(p);
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
