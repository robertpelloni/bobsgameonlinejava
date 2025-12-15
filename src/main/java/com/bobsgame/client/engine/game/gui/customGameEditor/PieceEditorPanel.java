package com.bobsgame.client.engine.game.gui.customGameEditor;

import com.bobsgame.client.engine.game.nd.bobsgame.game.PieceType;
import com.bobsgame.shared.BobColor;

import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.EditField;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.model.SimpleBooleanModel;

public class PieceEditorPanel extends DialogLayout {

    private PieceType pieceType;

    private EditField nameField;
    private EditField spriteField;
    private EditField colorR;
    private EditField colorG;
    private EditField colorB;

    private SimpleBooleanModel useNormalModel;
    private SimpleBooleanModel useGarbageModel;
    private SimpleBooleanModel useFillerModel;
    private SimpleBooleanModel disallowFirstModel;
    private SimpleBooleanModel flashingModel;
    private SimpleBooleanModel bombModel;
    private SimpleBooleanModel weightModel;

    public PieceEditorPanel() {
        //this.setTheme("pieceeditorpanel");

        nameField = new EditField();
        nameField.addCallback(new EditField.Callback() {
            public void callback(int key) { if(pieceType != null) pieceType.name = nameField.getText(); }
        });

        spriteField = new EditField();
        spriteField.addCallback(new EditField.Callback() {
            public void callback(int key) { if(pieceType != null) pieceType.spriteName = spriteField.getText(); }
        });

        EditField.Callback colorCallback = new EditField.Callback() { public void callback(int key) { updateColor(); } };
        colorR = new EditField(); colorR.addCallback(colorCallback);
        colorG = new EditField(); colorG.addCallback(colorCallback);
        colorB = new EditField(); colorB.addCallback(colorCallback);

        useNormalModel = new SimpleBooleanModel();
        useNormalModel.addCallback(new Runnable() { public void run() { if(pieceType!=null) pieceType.useAsNormalPiece = useNormalModel.getValue(); } });

        useGarbageModel = new SimpleBooleanModel();
        useGarbageModel.addCallback(new Runnable() { public void run() { if(pieceType!=null) pieceType.useAsGarbagePiece = useGarbageModel.getValue(); } });

        useFillerModel = new SimpleBooleanModel();
        useFillerModel.addCallback(new Runnable() { public void run() { if(pieceType!=null) pieceType.useAsPlayingFieldFillerPiece = useFillerModel.getValue(); } });

        disallowFirstModel = new SimpleBooleanModel();
        disallowFirstModel.addCallback(new Runnable() { public void run() { if(pieceType!=null) pieceType.disallowAsFirstPiece = disallowFirstModel.getValue(); } });

        flashingModel = new SimpleBooleanModel();
        flashingModel.addCallback(new Runnable() { public void run() { if(pieceType!=null) pieceType.flashingSpecialType = flashingModel.getValue(); } });

        bombModel = new SimpleBooleanModel();
        bombModel.addCallback(new Runnable() { public void run() { if(pieceType!=null) pieceType.bombPiece = bombModel.getValue(); } });

        weightModel = new SimpleBooleanModel();
        weightModel.addCallback(new Runnable() { public void run() { if(pieceType!=null) pieceType.weightPiece = weightModel.getValue(); } });

        ToggleButton useNormalBtn = new ToggleButton(useNormalModel); useNormalBtn.setText("Normal");
        ToggleButton useGarbageBtn = new ToggleButton(useGarbageModel); useGarbageBtn.setText("Garbage");
        ToggleButton useFillerBtn = new ToggleButton(useFillerModel); useFillerBtn.setText("Filler");
        ToggleButton disallowFirstBtn = new ToggleButton(disallowFirstModel); disallowFirstBtn.setText("No First");
        ToggleButton flashingBtn = new ToggleButton(flashingModel); flashingBtn.setText("Flashing");
        ToggleButton bombBtn = new ToggleButton(bombModel); bombBtn.setText("Bomb");
        ToggleButton weightBtn = new ToggleButton(weightModel); weightBtn.setText("Weight");

        Group hGroup = createParallelGroup()
            .addGroup(createSequentialGroup().addWidget(new Label("Name")).addWidget(nameField))
            .addGroup(createSequentialGroup().addWidget(new Label("Sprite")).addWidget(spriteField))
            .addGroup(createSequentialGroup().addWidget(new Label("Color RGB")).addWidget(colorR).addWidget(colorG).addWidget(colorB))
            .addWidget(useNormalBtn)
            .addWidget(useGarbageBtn)
            .addWidget(useFillerBtn)
            .addWidget(disallowFirstBtn)
            .addWidget(flashingBtn)
            .addWidget(bombBtn)
            .addWidget(weightBtn);

        Group vGroup = createSequentialGroup()
            .addGroup(createParallelGroup().addWidget(new Label("Name")).addWidget(nameField))
            .addGroup(createParallelGroup().addWidget(new Label("Sprite")).addWidget(spriteField))
            .addGroup(createParallelGroup().addWidget(new Label("Color RGB")).addWidget(colorR).addWidget(colorG).addWidget(colorB))
            .addWidget(useNormalBtn)
            .addWidget(useGarbageBtn)
            .addWidget(useFillerBtn)
            .addWidget(disallowFirstBtn)
            .addWidget(flashingBtn)
            .addWidget(bombBtn)
            .addWidget(weightBtn);

        setHorizontalGroup(hGroup);
        setVerticalGroup(vGroup);
    }

    public void setPieceType(PieceType p) {
        this.pieceType = p;
        if(p != null) {
            nameField.setText(p.name);
            spriteField.setText(p.spriteName);
            if(p.color != null) {
                colorR.setText(""+p.color.getRed());
                colorG.setText(""+p.color.getGreen());
                colorB.setText(""+p.color.getBlue());
            } else {
                colorR.setText("255"); colorG.setText("255"); colorB.setText("255");
            }
            useNormalModel.setValue(p.useAsNormalPiece);
            useGarbageModel.setValue(p.useAsGarbagePiece);
            useFillerModel.setValue(p.useAsPlayingFieldFillerPiece);
            disallowFirstModel.setValue(p.disallowAsFirstPiece);
            flashingModel.setValue(p.flashingSpecialType);
            bombModel.setValue(p.bombPiece);
            weightModel.setValue(p.weightPiece);
        }
    }

    private void updateColor() {
        if(pieceType == null) return;
        try {
            int r = Integer.parseInt(colorR.getText());
            int g = Integer.parseInt(colorG.getText());
            int b = Integer.parseInt(colorB.getText());
            pieceType.color = new BobColor(r,g,b);
        } catch (NumberFormatException e) {}
    }
}
