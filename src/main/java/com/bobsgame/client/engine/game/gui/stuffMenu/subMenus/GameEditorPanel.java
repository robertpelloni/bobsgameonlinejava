package com.bobsgame.client.engine.game.gui.stuffMenu.subMenus;

import com.bobsgame.client.engine.game.gui.stuffMenu.SubPanel;
import com.bobsgame.puzzle.CustomGameEditor;
import com.bobsgame.puzzle.PuzzleGameType;

public class GameEditorPanel extends SubPanel {

    public CustomGameEditor customGameEditor;

    public GameEditorPanel() {
        super();

        // Pass null initially, or a new default type
        customGameEditor = new CustomGameEditor(new PuzzleGameType());

        insideLayout.setHorizontalGroup(insideLayout.createParallelGroup(customGameEditor));
        insideLayout.setVerticalGroup(insideLayout.createParallelGroup(customGameEditor));
    }

    @Override
    public void layout() {
        super.layout();
        // Ensure the editor takes up space
        customGameEditor.setSize(insideLayout.getInnerWidth(), insideLayout.getInnerHeight());
    }
}
