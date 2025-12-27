package com.bobsgame.editor.Undo;

import java.util.Stack;

public class UndoManager extends AbstractUndoableEdit {
    protected Stack<UndoableEdit> edits = new Stack<>();
    protected UndoableEdit nextEdit = null;
    protected int limit = 100;

    public UndoManager() {
        super();
    }

    public void setLimit(int l) {
        limit = l;
        trimEdits();
    }

    public int getLimit() {
        return limit;
    }

    protected void trimEdits() {
        while (edits.size() > limit) {
            UndoableEdit edit = edits.remove(0);
            edit.die();
        }
    }

    public synchronized void discardAllEdits() {
        for (UndoableEdit e : edits) {
            e.die();
        }
        edits.clear();
        nextEdit = null;
    }

    protected void trimForAdd() {
        if (nextEdit != null) {
            // We are adding a new edit, so any redoable edits (from nextEdit onwards) must be discarded
            int index = edits.indexOf(nextEdit);
            if (index != -1) {
                while (edits.size() > index) {
                    UndoableEdit e = edits.pop();
                    e.die();
                }
            }
            nextEdit = null;
        }
        trimEdits();
    }

    @Override
    public synchronized boolean addEdit(UndoableEdit anEdit) {
        trimForAdd();
        edits.push(anEdit);
        trimEdits();
        return true;
    }

    @Override
    public synchronized boolean canUndo() {
        if (nextEdit != null) {
            // If nextEdit is set, we can undo if there are edits before it
            return edits.indexOf(nextEdit) > 0;
        }
        // If nextEdit is null, we are at the top of the stack, so we can undo if stack is not empty
        return !edits.isEmpty();
    }

    @Override
    public synchronized boolean canRedo() {
        if (nextEdit != null) {
            return true;
        }
        return false;
    }

    @Override
    public synchronized void undo() {
        if (!canUndo()) {
            throw new RuntimeException("Cannot undo");
        }

        UndoableEdit edit;
        if (nextEdit != null) {
            int index = edits.indexOf(nextEdit);
            edit = edits.get(index - 1);
        } else {
            edit = edits.peek();
        }

        edit.undo();
        nextEdit = edit;
    }

    @Override
    public synchronized void redo() {
        if (!canRedo()) {
            throw new RuntimeException("Cannot redo");
        }

        UndoableEdit edit = nextEdit;
        edit.redo();
        
        int index = edits.indexOf(nextEdit);
        if (index + 1 < edits.size()) {
            nextEdit = edits.get(index + 1);
        } else {
            nextEdit = null;
        }
    }
    
    public synchronized String getUndoPresentationName() {
        if (canUndo()) {
            UndoableEdit edit;
            if (nextEdit != null) {
                int index = edits.indexOf(nextEdit);
                edit = edits.get(index - 1);
            } else {
                edit = edits.peek();
            }
            return edit.getUndoPresentationName();
        }
        return "Undo";
    }

    public synchronized String getRedoPresentationName() {
        if (canRedo()) {
            return nextEdit.getRedoPresentationName();
        }
        return "Redo";
    }
}
