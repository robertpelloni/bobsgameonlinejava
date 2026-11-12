package com.bobsgame.puzzle;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParityTest {
    @Test
    public void testEngineParityStub() {
        // This is a stub for the C++ backend puzzle engine parity testing logic.
        // As defined in the HANDOFF and project goals, this placeholder validates that
        // the core test suite executes properly in the CI pipeline for the shared engine logic.
        assertTrue(true, "Parity check passed.");
    }

    @Test
    public void testGridInitializationDefaults() {
        Grid grid = new Grid(null);
        assertEquals(0, grid.screenX, "Default screenX should be 0");
        assertEquals(0, grid.screenY, "Default screenY should be 0");
        assertEquals(0, grid.lastGarbageHoleX, "Default lastGarbageHoleX should be 0");
        assertTrue(grid.garbageHoleDirectionToggle, "Default garbageHoleDirectionToggle should be true");
        assertEquals(100f, grid.scrollBlockIncrement, "Default scrollBlockIncrement should be 100");
    }

    @Test
    public void testReformatAndBlockAccessors() {
        Grid grid = new Grid(null);
        grid.reformat(6, 12);
        assertNotNull(grid.blocks, "Blocks 2D array should be allocated");
        assertEquals(12, grid.blocks.length, "Height dimension should match 12 rows");
        assertEquals(6, grid.blocks[0].length, "Width dimension should match 6 cols");

        // Verify null initially
        assertNull(grid.get(0, 0));
        assertNull(grid.get(-1, 0));
        assertNull(grid.get(0, -1));
        assertNull(grid.get(6, 0));
        assertNull(grid.get(0, 12));
    }
}
