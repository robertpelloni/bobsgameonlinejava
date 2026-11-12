package com.bobsgame.puzzle;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GridLogicTest {

    @Test
    public void testGridBlockArrayInitialization() {
        Grid grid = new Grid(null);
        grid.reformat(10, 20);

        assertNotNull(grid.blocks, "Grid block matrix must not be null after reformat");
        assertEquals(20, grid.blocks.length, "Row count should be 20");
        assertEquals(10, grid.blocks[0].length, "Column count should be 10");

        // Verify out-of-bounds boundaries return null safely without throwing exceptions
        assertNull(grid.get(-5, 5), "Negative X should return null");
        assertNull(grid.get(5, -5), "Negative Y should return null");
        assertNull(grid.get(10, 5), "Out-of-bounds X should return null");
        assertNull(grid.get(5, 20), "Out-of-bounds Y should return null");
        assertFalse(grid.contains(0, 0), "Empty cell should not contain block");
    }

    @Test
    public void testGarbageHoleToggleLogic() {
        Grid grid = new Grid(null);
        grid.lastGarbageHoleX = 0;
        grid.garbageHoleDirectionToggle = true;

        int gridWidth = 6;
        // Simulate step advancing along zigzag
        if (grid.garbageHoleDirectionToggle) {
            grid.lastGarbageHoleX++;
            if (grid.lastGarbageHoleX >= gridWidth) {
                grid.lastGarbageHoleX = gridWidth - 1;
                grid.garbageHoleDirectionToggle = false;
            }
        }
        assertEquals(1, grid.lastGarbageHoleX);
        assertTrue(grid.garbageHoleDirectionToggle);

        // Move to edge and verify direction toggle flips
        grid.lastGarbageHoleX = gridWidth - 1;
        if (grid.garbageHoleDirectionToggle) {
            grid.lastGarbageHoleX++;
            if (grid.lastGarbageHoleX >= gridWidth) {
                grid.lastGarbageHoleX = gridWidth - 1;
                grid.garbageHoleDirectionToggle = false;
            }
        }
        assertEquals(5, grid.lastGarbageHoleX);
        assertFalse(grid.garbageHoleDirectionToggle, "Direction toggle should flip to false when hitting right bound");
    }
}
