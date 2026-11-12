package com.bobsgame.puzzle;

import com.bobsgame.puzzle.GameType.*;
import java.util.ArrayList;
import java.util.Random;

public class Grid {
    public GameLogic game;
    public Block[][] blocks;
    public int screenX = 0;
    public int screenY = 0;

    public int lastGarbageHoleX = 0;
    public boolean garbageHoleDirectionToggle = true;

    public float scrollPlayingFieldY = 0;
    public float scrollBlockIncrement = 100;

    public Grid(GameLogic game) {
        this.game = game;
    }

    public int getWidth() {
        if (game != null) return game.gridW();
        if (blocks != null && blocks.length > 0 && blocks[0] != null) return blocks[0].length;
        return 0;
    }
    public int getHeight() {
        if (game != null) return game.gridH();
        if (blocks != null) return blocks.length;
        return 0;
    }

    public void reformat(int w, int h) {
        blocks = new Block[h][w];
    }

    public Block get(int x, int y) {
        if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight()) return null;
        return blocks[y][x];
    }

    public void set(int x, int y, Block b) {
        if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight()) return;
        blocks[y][x] = b;
        if (b != null) { b.xGrid = x; b.yGrid = y; }
    }

    public void add(int x, int y, Block b) { set(x, y, b); }

    public Block remove(int x, int y, boolean destroy, boolean explode) {
        Block b = get(x, y);
        if (b != null) {
            blocks[y][x] = null;
        }
        return b;
    }

    public void removeBlock(Block b, boolean destroy, boolean explode) {
        remove(b.xGrid, b.yGrid, destroy, explode);
    }

    public boolean contains(int x, int y) {
        return get(x, y) != null;
    }

    public int getNumberOfFilledCells() {
        int count = 0;
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                if (contains(x, y)) count++;
            }
        }
        return count;
    }

    public boolean doesPieceFit(Piece p) {
        return doesPieceFit(p, p.xGrid, p.yGrid, p.currentRotation);
    }

    public boolean doesPieceFit(Piece p, int gridX, int gridY, int rot) {
        Piece.RotationSet rs = p.pieceType.rotationSet;
        if (rs == null || rot < 0 || rot >= rs.size()) return false;
        Piece.Rotation r = rs.get(rot);
        for (Piece.BlockOffset bo : r.blockOffsets) {
            int x = gridX + bo.x; int y = gridY + bo.y;
            if (x < 0 || x >= getWidth() || y >= getHeight()) return false;
            if (y >= 0 && get(x, y) != null) return false;
        }
        return true;
    }

    public void setPiece(Piece p) {
        Piece.Rotation r = p.pieceType.rotationSet.get(p.currentRotation);
        for (int i = 0; i < r.blockOffsets.size(); i++) {
            Piece.BlockOffset bo = r.blockOffsets.get(i);
            int targetX = p.xGrid + bo.x;
            int targetY = p.yGrid + bo.y;
            if (targetY >= 0 && targetY < getHeight() && targetX >= 0 && targetX < getWidth()) {
                Block b = p.blocks.get(i);
                b.xGrid = targetX;
                b.yGrid = targetY;
                add(targetX, targetY, b);
            }
        }
        p.setInGrid = true;
    }

    public BlockType getRandomBlockType(ArrayList<BlockType> types) {
        if (types == null || types.isEmpty()) return BlockType.squareBlockType;
        return types.get(new Random().nextInt(types.size()));
    }

    public BlockType getRandomBlockTypeDisregardingSpecialFrequency(ArrayList<BlockType> types) {
        return getRandomBlockType(types);
    }

    public void cursorSwapBetweenTwoBlocksHorizontal(Piece cursor) {
        Block a = get(cursor.xGrid, cursor.yGrid);
        Block b = get(cursor.xGrid + 1, cursor.yGrid);
        if (a != null && a.interpolateSwappingWithX == 0 && !a.flashingToBeRemoved) {
            if (b == null || b.interpolateSwappingWithX == 0) a.interpolateSwappingWithX = 1;
        }
        if (b != null && b.interpolateSwappingWithX == 0 && !b.flashingToBeRemoved) {
            if (a == null || a.interpolateSwappingWithX == 1) b.interpolateSwappingWithX = -1;
        }
    }

    public void cursorSwapBetweenTwoBlocksVertical(Piece cursor) {
        Block a = get(cursor.xGrid, cursor.yGrid);
        Block b = get(cursor.xGrid, cursor.yGrid + 1);
        if (a != null && a.interpolateSwappingWithY == 0 && !a.flashingToBeRemoved) {
            if (b == null || b.interpolateSwappingWithY == 0) a.interpolateSwappingWithY = 1;
        }
        if (b != null && b.interpolateSwappingWithY == 0 && !b.flashingToBeRemoved) {
            if (a == null || a.interpolateSwappingWithY == 1) b.interpolateSwappingWithY = -1;
        }
    }

    public boolean moveDownDisconnectedBlocksAboveBlankSpacesOneLine(ArrayList<BlockType> ignore) {
        boolean moved = false;
        for (int y = getHeight() - 2; y >= 0; y--) {
            for (int x = 0; x < getWidth(); x++) {
                Block b = get(x, y);
                if (b != null && !ignore.contains(b.blockType) && get(x, y + 1) == null) {
                    remove(x, y, false, false);
                    add(x, y + 1, b);
                    moved = true;
                }
            }
        }
        return moved;
    }

    public boolean moveDownAnyBlocksAboveBlankSpacesOneLine(ArrayList<BlockType> ignore) {
        return moveDownDisconnectedBlocksAboveBlankSpacesOneLine(ignore);
    }

    public boolean moveDownLinesAboveBlankLinesOneLine() {
        boolean moved = false;
        for (int y = getHeight() - 2; y >= 0; y--) {
            boolean isFull = true;
            for (int x = 0; x < getWidth(); x++) if (get(x, y) == null) { isFull = false; break; }
            if (isFull) {
                boolean isEmptyBelow = true;
                for (int x = 0; x < getWidth(); x++) if (get(x, y + 1) != null) { isEmptyBelow = false; break; }
                if (isEmptyBelow) {
                    for (int x = 0; x < getWidth(); x++) {
                        Block b = remove(x, y, false, false);
                        add(x, y + 1, b);
                    }
                    moved = true;
                }
            }
        }
        return moved;
    }

    public ArrayList<Block> checkLines(ArrayList<BlockType> ignore, ArrayList<BlockType> mustContain) {
        ArrayList<Block> chain = new ArrayList<>();
        for (int y = 0; y < getHeight(); y++) {
            boolean isFull = true;
            boolean containsMust = mustContain.isEmpty();
            ArrayList<Block> line = new ArrayList<>();
            for (int x = 0; x < getWidth(); x++) {
                Block b = get(x, y);
                if (b == null || ignore.contains(b.blockType)) { isFull = false; break; }
                if (!containsMust && mustContain.contains(b.blockType)) containsMust = true;
                line.add(b);
            }
            if (isFull && containsMust) chain.addAll(line);
        }
        return chain;
    }

    public void setColorConnections(ArrayList<BlockType> ignore) {
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                Block b = get(x, y);
                if (b != null) {
                    b.connectedUp = doBlocksMatchColor(b, get(x, y - 1), ignore);
                    b.connectedDown = doBlocksMatchColor(b, get(x, y + 1), ignore);
                    b.connectedLeft = doBlocksMatchColor(b, get(x - 1, y), ignore);
                    b.connectedRight = doBlocksMatchColor(b, get(x + 1, y), ignore);
                }
            }
        }
    }

    public boolean doBlocksMatchColor(Block a, Block b, ArrayList<BlockType> ignore) {
        if (b == null || ignore.contains(b.blockType)) return false;
        return a.getColor().equals(b.getColor());
    }

    public void recursivelyGetAllMatchingBlocksConnectedToBlockToArrayIfNotInItAlready(Block b, ArrayList<Block> arr, ArrayList<BlockType> ignore) {
        if (!arr.contains(b)) arr.add(b);
        for (Block n : getConnectedBlocksUpDownLeftRight(b)) {
            if (doBlocksMatchColor(b, n, ignore) && !arr.contains(n)) {
                recursivelyGetAllMatchingBlocksConnectedToBlockToArrayIfNotInItAlready(n, arr, ignore);
            }
        }
    }

    public void addBlocksConnectedToBlockToArrayIfNotInItAlreadyIfInRowAtLeastAmount(Block b, ArrayList<Block> arr, int amount, int x0, int x1, int y0, int y1, ArrayList<BlockType> ignore, ArrayList<BlockType> mustContain) {
        ArrayList<Block> row = new ArrayList<>();
        row.add(b);
        for (int x = b.xGrid - 1; x >= x0; x--) { Block next = get(x, b.yGrid); if (doBlocksMatchColor(b, next, ignore)) row.add(next); else break; }
        for (int x = b.xGrid + 1; x < x1; x++) { Block next = get(x, b.yGrid); if (doBlocksMatchColor(b, next, ignore)) row.add(next); else break; }
        if (row.size() >= amount) {
            if (!mustContain.isEmpty()) {
                boolean containsMandatory = false;
                for (Block r : row) if (mustContain.contains(r.blockType)) containsMandatory = true;
                if (!containsMandatory) return;
            }
            for (Block r : row) if (!arr.contains(r)) arr.add(r);
        }
    }

    public void addBlocksConnectedToBlockToArrayIfNotInItAlreadyIfInColumnAtLeastAmount(Block b, ArrayList<Block> arr, int amount, int x0, int x1, int y0, int y1, ArrayList<BlockType> ignore, ArrayList<BlockType> mustContain) {
        ArrayList<Block> col = new ArrayList<>();
        col.add(b);
        for (int y = b.yGrid - 1; y >= y0; y--) { Block next = get(b.xGrid, y); if (doBlocksMatchColor(b, next, ignore)) col.add(next); else break; }
        for (int y = b.yGrid + 1; y < y1; y++) { Block next = get(b.xGrid, y); if (doBlocksMatchColor(b, next, ignore)) col.add(next); else break; }
        if (col.size() >= amount) {
            if (!mustContain.isEmpty()) {
                boolean containsMandatory = false;
                for (Block c : col) if (mustContain.contains(c.blockType)) containsMandatory = true;
                if (!containsMandatory) return;
            }
            for (Block c : col) if (!arr.contains(c)) arr.add(c);
        }
    }

    public void addBlocksConnectedToBlockToArrayIfNotInItAlreadyIfDiagonalAtLeastAmount(Block b, ArrayList<Block> arr, int amount, int x0, int x1, int y0, int y1, ArrayList<BlockType> ignore, ArrayList<BlockType> mustContain) {
        // Simple diagonal check
    }

    public void checkRecursiveConnectedRowOrColumn(ArrayList<Block> arr, int amount, int x0, int x1, int y0, int y1, ArrayList<BlockType> ignore, ArrayList<BlockType> mustContain) {
        for (int y = y0; y < y1; y++) {
            for (int x = x0; x < x1; x++) {
                Block b = get(x, y);
                if (b != null && !ignore.contains(b.blockType)) {
                    ArrayList<Block> connected = new ArrayList<>();
                    addBlocksConnectedToBlockToArrayIfNotInItAlreadyIfInRowAtLeastAmount(b, connected, 2, x0, x1, y0, y1, ignore, mustContain);
                    addBlocksConnectedToBlockToArrayIfNotInItAlreadyIfInColumnAtLeastAmount(b, connected, 2, x0, x1, y0, y1, ignore, mustContain);
                    if (!connected.isEmpty()) {
                        int size = connected.size();
                        for (int i = 0; i < size; i++) {
                            addBlocksConnectedToBlockToArrayIfNotInItAlreadyIfInRowAtLeastAmount(connected.get(i), connected, 2, x0, x1, y0, y1, ignore, mustContain);
                            addBlocksConnectedToBlockToArrayIfNotInItAlreadyIfInColumnAtLeastAmount(connected.get(i), connected, 2, x0, x1, y0, y1, ignore, mustContain);
                            if (connected.size() > size) { size = connected.size(); i = -1; }
                        }
                        if (connected.size() >= amount) for (Block c : connected) if (!arr.contains(c)) arr.add(c);
                    }
                }
            }
        }
    }

    public ArrayList<Block> checkBreakerBlocks(int toRow, ArrayList<BlockType> ignore, ArrayList<BlockType> mustContain) {
        ArrayList<Block> breakBlocks = new ArrayList<>();
        for (int y = 0; y < toRow; y++) {
            for (int x = 0; x < getWidth(); x++) {
                Block b = get(x, y);
                if (b != null && b.blockType.isBreaker) {
                    ArrayList<Block> connected = new ArrayList<>();
                    recursivelyGetAllMatchingBlocksConnectedToBlockToArrayIfNotInItAlready(b, connected, ignore);
                    if (connected.size() >= 2) {
                        for (Block c : connected) if (!breakBlocks.contains(c)) breakBlocks.add(c);
                        for (Block d : getConnectedBlocksUpDownLeftRight(b)) if (ignore.contains(d.blockType)) if (!breakBlocks.contains(d)) breakBlocks.add(d);
                    }
                }
            }
        }
        return breakBlocks;
    }

    public ArrayList<Block> getConnectedBlocksUpDownLeftRight(Block b) {
        ArrayList<Block> res = new ArrayList<>();
        Block u = get(b.xGrid, b.yGrid - 1); if (u != null) res.add(u);
        Block d = get(b.xGrid, b.yGrid + 1); if (d != null) res.add(d);
        Block l = get(b.xGrid - 1, b.yGrid); if (l != null) res.add(l);
        Block r = get(b.xGrid + 1, b.yGrid); if (r != null) res.add(r);
        return res;
    }

    public boolean areAnyBlocksPopping() {
        for (int y = 0; y < getHeight(); y++) for (int x = 0; x < getWidth(); x++) { Block b = get(x, y); if (b != null && b.popping) return true; }
        return false;
    }

    public void update() {
        for (int y = 0; y < getHeight(); y++) for (int x = 0; x < getWidth(); x++) { Block b = get(x, y); if (b != null) b.update(); }
    }

    public void replaceAllBlocksWithNewGameBlocks() {
        reformat(getWidth(), getHeight());
    }

    public void randomlyFillGridWithPlayingFieldPieces(int amount, int startY) {
        ArrayList<BlockType> bt = game.currentGameType.getPlayingFieldBlockTypes(game.getCurrentDifficulty());
        ArrayList<PieceType> pt = game.currentGameType.getPlayingFieldPieceTypes(game.getCurrentDifficulty());
        for (int i = 0; i < amount; i++) {
            int x = (int)(Math.random() * getWidth());
            int y = startY + (int)(Math.random() * (getHeight() - startY));
            if (get(x, y) == null) putOneBlockPieceInGridCheckingForFillRules(x, y, pt, bt);
        }
    }

    public void buildRandomStackRetainingExistingBlocks(int amount, int startY) {
        randomlyFillGridWithPlayingFieldPieces(amount, startY);
    }

    public Piece getRandomPiece() {
        ArrayList<PieceType> pt = game.currentGameType.getNormalPieceTypes(game.getCurrentDifficulty());
        ArrayList<BlockType> bt = game.currentGameType.getNormalBlockTypes(game.getCurrentDifficulty());
        return new Piece(game, this, pt.get((int)(Math.random() * pt.size())), bt);
    }

    public Piece putOneBlockPieceInGridCheckingForFillRules(int x, int y, ArrayList<PieceType> pt, ArrayList<BlockType> bt) {
        Piece p = new Piece(game, this, pt.get((int)(Math.random() * pt.size())), bt);
        p.init();
        p.xGrid = x; p.yGrid = y;
        setPiece(p);
        return p;
    }

    public boolean continueSwappingBlocks() {
        boolean swappingAny = false;
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                Block a = get(x, y);
                if (a != null) {
                    if (a.interpolateSwappingWithX != 0) {
                        Block b = get(x + a.interpolateSwappingWithX, y);
                        swappingAny = true;
                        if (a.swapTicks < 17 * 6) { a.swapTicks += game.ticks(); if (b != null) b.swapTicks = a.swapTicks; }
                        else {
                            a.swapTicks = 0; if (b != null) b.swapTicks = 0;
                            remove(x, y, false, false);
                            if (contains(x + a.interpolateSwappingWithX, y)) remove(x + a.interpolateSwappingWithX, y, false, false);
                            if (b != null) add(x, y, b);
                            add(x + a.interpolateSwappingWithX, y, a);
                            a.interpolateSwappingWithX = 0; if (b != null) b.interpolateSwappingWithX = 0;
                        }
                    }
                    if (a.interpolateSwappingWithY != 0) {
                        Block b = get(x, y + a.interpolateSwappingWithY);
                        swappingAny = true;
                        if (a.swapTicks < 17 * 6) { a.swapTicks += game.ticks(); if (b != null) b.swapTicks = a.swapTicks; }
                        else {
                            a.swapTicks = 0; if (b != null) b.swapTicks = 0;
                            remove(x, y, false, false);
                            if (contains(x, y + a.interpolateSwappingWithY)) remove(x, y + a.interpolateSwappingWithY, false, false);
                            if (b != null) add(x, y, b);
                            add(x, y + a.interpolateSwappingWithY, a);
                            a.interpolateSwappingWithY = 0; if (b != null) b.interpolateSwappingWithY = 0;
                        }
                    }
                }
            }
        }
        return swappingAny;
    }

    public boolean scrollUpStack(Piece cursorPiece, int amt) {
        scrollPlayingFieldY -= amt;
        if (scrollPlayingFieldY < 0 - scrollBlockIncrement) {
            for (int x = 0; x < getWidth(); x++) if (get(x, GameLogic.aboveGridBuffer) != null) { scrollPlayingFieldY = 0 - scrollBlockIncrement; return false; }
            if (cursorPiece != null) {
                cursorPiece.yGrid -= 1;
                if (cursorPiece.yGrid < 1 + GameLogic.aboveGridBuffer) cursorPiece.yGrid += 1;
            }
            scrollPlayingFieldY += scrollBlockIncrement;
            moveAllRowsUpOne();
            ArrayList<BlockType> bt = game.currentGameType.getPlayingFieldBlockTypes(game.getCurrentDifficulty());
            ArrayList<PieceType> pt = game.currentGameType.getPlayingFieldPieceTypes(game.getCurrentDifficulty());
            int y = getHeight() - 1;
            for (int x = 0; x < getWidth(); x++) putOneBlockPieceInGridCheckingForFillRules(x, y, pt, bt);
            game.piecesMadeThisGame++;
        }
        return true;
    }

    public boolean isAnythingAboveThreeQuarters() {
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < (getHeight() - GameLogic.aboveGridBuffer) / 4; y++) {
                if (get(x, y + GameLogic.aboveGridBuffer) != null) return true;
            }
        }
        return false;
    }

    public void moveAllRowsUpOne() {
        for (int x = 0; x < getWidth(); x++) {
            if (contains(x, 0)) {
                Block b = get(x, 0);
                removeBlock(b, true, true);
            }
        }
        for (int y = 0; y < getHeight() - 1; y++) {
            for (int x = 0; x < getWidth(); x++) {
                if (contains(x, y + 1)) {
                    Block b = remove(x, y + 1, false, false);
                    add(x, y, b);
                }
            }
        }
    }

    public Piece putGarbageBlock(int x, int y) {
        ArrayList<BlockType> bt = game.currentGameType.getGarbageBlockTypes(game.getCurrentDifficulty());
        ArrayList<PieceType> pt = game.currentGameType.getGarbagePieceTypes(game.getCurrentDifficulty());
        if (pt.isEmpty()) pt.add(PieceType.emptyPieceType);
        return putOneBlockPieceInGridCheckingForFillRules(x, y, pt, bt);
    }

    public void putGarbageBlockFromFloor(int x, int y) {
        Piece p = putGarbageBlock(x, y);
        if (p != null) {
            for (Block b : p.blocks) {
                b.ticksSinceLastMovement = 0;
            }
        }
    }

    public void makeGarbageRowFromFloor() {
        moveAllRowsUpOne();
        int y = getHeight() - 1;
        GarbageType rule = game.currentGameType.playingFieldGarbageType;
        if (rule == GarbageType.MATCH_BOTTOM_ROW) {
            for (int x = 0; x < getWidth(); x++) if (get(x, y - 1) != null) putGarbageBlockFromFloor(x, y);
        } else if (rule == GarbageType.RANDOM) {
            for (int x = 0; x < getWidth(); x++) if ((int)(Math.random() * 2) == 0) putGarbageBlockFromFloor(x, y);
        } else if (rule == GarbageType.ZIGZAG_PATTERN) {
            for (int x = 0; x < getWidth(); x++) if (x != lastGarbageHoleX) putGarbageBlockFromFloor(x, y);
            if (garbageHoleDirectionToggle) {
                lastGarbageHoleX++;
                if (lastGarbageHoleX >= getWidth()) { lastGarbageHoleX = getWidth() - 1; garbageHoleDirectionToggle = false; }
            } else {
                lastGarbageHoleX--;
                if (lastGarbageHoleX < 0) { lastGarbageHoleX = 0; garbageHoleDirectionToggle = true; }
            }
        }
    }

    public void makeGarbageRowFromCeiling() {
        int y = 0;
        for (int x = 0; x < getWidth(); x++) {
            Piece p = putGarbageBlock(x, y);
            if (p != null) {
                for (Block b : p.blocks) {
                    b.ticksSinceLastMovement = 0;
                }
            }
        }
    }

    public int cellW() { return game.blockWidth + game.currentGameType.gridPixelsBetweenColumns; }
    public int cellH() { return game.blockHeight + game.currentGameType.gridPixelsBetweenRows; }
    public float getXInFBO() { return screenX; }
    public float getYInFBO() { return screenY; }

    public Integer[][] getState() {
        int h = getHeight();
        int w = getWidth();
        Integer[][] state = new Integer[h][w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Block b = get(x, y);
                if (b != null) {
                    state[y][x] = b.getColor().toInt();
                } else {
                    state[y][x] = null;
                }
            }
        }
        return state;
    }

    public void applyState(Integer[][] state) {
        if (state == null || state.length == 0) return;
        int h = state.length;
        int w = state[0].length;
        reformat(w, h);
        BlockType bt = game.currentGameType.getNormalBlockTypes(game.getCurrentDifficulty()).get(0);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Integer colorInt = state[y][x];
                if (colorInt != null) {
                    Block b = new Block(game, this, null, bt);
                    b.getColor().copyFrom(com.bobsgame.shared.BobColor.fromInt(colorInt));
                    set(x, y, b);
                }
            }
        }
    }
}
