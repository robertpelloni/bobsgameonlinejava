package com.bobsgame.puzzle;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.ComboBox;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.EditField;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ListBox;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.TabbedPane;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.model.SimpleChangableListModel;
import de.matthiasmann.twl.CallbackWithReason;
import de.matthiasmann.twl.ListBox.CallbackReason;
import java.util.UUID;

public class CustomGameEditor extends Widget {

    private TabbedPane tabbedPane;
    private PuzzleGameType currentGameType;

    // Settings Widgets
    private EditField nameField;
    private EditField rules1Field;
    private ComboBox<ScoreType> scoreTypeBox;
    private ToggleButton nextPieceEnabledBtn;

    // Block Widgets
    private ListBox<String> blockListBox;
    private SimpleChangableListModel<String> blockListModel;
    private EditField blockNameField;
    private EditField blockSpriteField;
    private ToggleButton blockUseInNormalPiecesBtn;
    private ToggleButton blockUseAsGarbageBtn;
    private BlockType currentEditingBlock = null;

    public CustomGameEditor(PuzzleGameType gameType) {
        this.currentGameType = gameType != null ? gameType : new PuzzleGameType();

        tabbedPane = new TabbedPane();

        // Settings Tab
        Widget settingsTab = createSettingsTab();
        tabbedPane.addTab("Game Settings", settingsTab);

        // Block Tab
        Widget blockTab = createBlockTab();
        tabbedPane.addTab("Block Designer", blockTab);

        // Piece Tab
        Widget pieceTab = new Label("Piece Designer (TODO)");
        tabbedPane.addTab("Piece Designer", pieceTab);

        // Difficulty Tab
        Widget diffTab = new Label("Difficulty Editor (TODO)");
        tabbedPane.addTab("Difficulty Editor", diffTab);

        add(tabbedPane);
    }

    private Widget createSettingsTab() {
        DialogLayout layout = new DialogLayout();

        // General Rules
        nameField = new EditField();
        nameField.setText(currentGameType.name);

        rules1Field = new EditField();
        rules1Field.setText(currentGameType.rules1);

        SimpleChangableListModel<ScoreType> scoreModel = new SimpleChangableListModel<>(ScoreType.values());
        scoreTypeBox = new ComboBox<>(scoreModel);
        scoreTypeBox.setSelected(0); // TODO: sync with currentGameType

        nextPieceEnabledBtn = new ToggleButton("Next Piece Enabled");
        nextPieceEnabledBtn.setActive(currentGameType.nextPieceEnabled);

        layout.setHorizontalGroup(layout.createParallelGroup()
            .addWidget(new Label("Name"))
            .addWidget(nameField)
            .addWidget(new Label("Rules 1"))
            .addWidget(rules1Field)
            .addWidget(new Label("Score Type"))
            .addWidget(scoreTypeBox)
            .addWidget(nextPieceEnabledBtn)
        );

        layout.setVerticalGroup(layout.createSequentialGroup()
            .addWidget(new Label("Name"))
            .addWidget(nameField)
            .addWidget(new Label("Rules 1"))
            .addWidget(rules1Field)
            .addWidget(new Label("Score Type"))
            .addWidget(scoreTypeBox)
            .addWidget(nextPieceEnabledBtn)
        );

        ScrollPane scroll = new ScrollPane(layout);
        scroll.setFixed(ScrollPane.Fixed.HORIZONTAL);
        return scroll;
    }

    private Widget createBlockTab() {
        DialogLayout layout = new DialogLayout();

        blockListModel = new SimpleChangableListModel<>();
        for (BlockType b : currentGameType.blockTypes) {
            blockListModel.addElement(b.name);
        }

        blockListBox = new ListBox<>(blockListModel);
        blockListBox.addCallback(new CallbackWithReason<CallbackReason>() {
            public void callback(CallbackReason reason) {
                if (reason == CallbackReason.MOUSE_CLICK || reason == CallbackReason.SET_SELECTED) {
                    int selected = blockListBox.getSelected();
                    if (selected >= 0 && selected < currentGameType.blockTypes.size()) {
                        loadBlock(currentGameType.blockTypes.get(selected));
                    }
                }
            }
        });

        Button addBlockBtn = new Button("Add Block");
        addBlockBtn.addCallback(new Runnable() {
            public void run() {
                BlockType newBlock = new BlockType();
                newBlock.name = "New Block";
                newBlock.uuid = UUID.randomUUID().toString();
                currentGameType.blockTypes.add(newBlock);
                blockListModel.addElement(newBlock.name);
                blockListBox.setSelected(blockListModel.getNumEntries() - 1);
            }
        });

        blockNameField = new EditField();
        blockSpriteField = new EditField();
        blockUseInNormalPiecesBtn = new ToggleButton("Use in Normal Pieces");
        blockUseAsGarbageBtn = new ToggleButton("Use as Garbage");

        // Manual save button (or auto-save on change)
        Button saveBlockBtn = new Button("Save Changes to Block");
        saveBlockBtn.addCallback(new Runnable() {
             public void run() {
                 if (currentEditingBlock != null) {
                     currentEditingBlock.name = blockNameField.getText();
                     currentEditingBlock.spriteName = blockSpriteField.getText();
                     currentEditingBlock.useInNormalPieces = blockUseInNormalPiecesBtn.isActive();
                     currentEditingBlock.useAsGarbage = blockUseAsGarbageBtn.isActive();

                     // Update list label
                     int idx = currentGameType.blockTypes.indexOf(currentEditingBlock);
                     if (idx != -1) {
                         // Hacky update
                         blockListModel.removeElement(idx);
                         blockListModel.insertElement(idx, currentEditingBlock.name);
                         blockListBox.setSelected(idx);
                     }
                 }
             }
        });

        DialogLayout.Group hGroup = layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup()
                .addWidget(new Label("Blocks"))
                .addWidget(blockListBox)
                .addWidget(addBlockBtn))
            .addGap(20)
            .addGroup(layout.createParallelGroup()
                .addWidget(new Label("Name"))
                .addWidget(blockNameField)
                .addWidget(new Label("Sprite"))
                .addWidget(blockSpriteField)
                .addWidget(blockUseInNormalPiecesBtn)
                .addWidget(blockUseAsGarbageBtn)
                .addWidget(saveBlockBtn));

        DialogLayout.Group vGroup = layout.createParallelGroup()
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                     .addWidget(new Label("Blocks"))
                     .addWidget(new Label("Name")))
                .addGroup(layout.createParallelGroup()
                     .addWidget(blockListBox)
                     .addGroup(layout.createSequentialGroup()
                         .addWidget(blockNameField)
                         .addWidget(new Label("Sprite"))
                         .addWidget(blockSpriteField)
                         .addWidget(blockUseInNormalPiecesBtn)
                         .addWidget(blockUseAsGarbageBtn)
                         .addWidget(saveBlockBtn)))
                .addWidget(addBlockBtn));

        layout.setHorizontalGroup(hGroup);
        layout.setVerticalGroup(vGroup);

        return layout;
    }

    private void loadBlock(BlockType b) {
        currentEditingBlock = b;
        blockNameField.setText(b.name);
        blockSpriteField.setText(b.spriteName);
        blockUseInNormalPiecesBtn.setActive(b.useInNormalPieces);
        blockUseAsGarbageBtn.setActive(b.useAsGarbage);
    }

    @Override
    protected void layout() {
        tabbedPane.setSize(getInnerWidth(), getInnerHeight());
        tabbedPane.setPosition(getInnerX(), getInnerY());
    }

    public void save() {
        // Apply changes to currentGameType
        currentGameType.name = nameField.getText();
        currentGameType.rules1 = rules1Field.getText();
        currentGameType.nextPieceEnabled = nextPieceEnabledBtn.isActive();
        // ... rest
    }
}
