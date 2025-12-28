package com.bobsgame.editor.SpriteEditor.Tools;

import java.awt.Graphics;
import com.bobsgame.editor.SpriteEditor.SECanvas;

public class PixelBrush implements Brush {

    @Override
    public String getName() {
        return "Pencil";
    }

    @Override
    public void onMousePress(SECanvas canvas, int x, int y, int color, int modifiers) {
        canvas.setPixel(x, y, color, canvas.getCurrentEdit());
    }

    @Override
    public void onMouseDrag(SECanvas canvas, int x, int y, int color, int modifiers) {
        canvas.setPixel(x, y, color, canvas.getCurrentEdit());
    }

    @Override
    public void onMouseRelease(SECanvas canvas, int x, int y, int color, int modifiers) {
        // No action
    }

    @Override
    public void onPaint(Graphics g, SECanvas canvas) {
        // Could draw cursor highlight here
    }
}
