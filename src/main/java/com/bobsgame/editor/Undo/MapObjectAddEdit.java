package com.bobsgame.editor.Undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.bobsgame.editor.Project.Map.Area;
import com.bobsgame.editor.Project.Map.Door;
import com.bobsgame.editor.Project.Map.Entity;
import com.bobsgame.editor.Project.Map.Light;
import com.bobsgame.editor.Project.Map.Map;

public class MapObjectAddEdit extends AbstractUndoableEdit {
    private Map map;
    private Object object;
    
    public MapObjectAddEdit(Map map, Object object) {
        this.map = map;
        this.object = object;
    }
    
    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        if (object instanceof Entity) map.getSelectedState().removeEntity((Entity)object);
        else if (object instanceof Door) map.removeDoor((Door)object);
        else if (object instanceof Light) map.getSelectedState().removeLight((Light)object);
        else if (object instanceof Area) map.getSelectedState().removeArea((Area)object);
    }
    
    @Override
    public void redo() throws CannotRedoException {
        super.redo();
        if (object instanceof Entity) map.addEntity((Entity)object);
        else if (object instanceof Door) map.addDoor((Door)object);
        else if (object instanceof Light) map.addLight((Light)object);
        else if (object instanceof Area) map.addArea((Area)object);
    }
}
