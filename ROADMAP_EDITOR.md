# Editor Roadmap

This roadmap outlines the prioritized implementation of features for the Bob's Game Editor (Legacy Swing Editor), based on research from industry-standard tools like Aseprite, Tiled, and Pyxel Edit.

## Phase 1: Foundations (Immediate Priority)
These features are essential for any usable editor and are currently missing or underdeveloped.

1.  **Layer System** [COMPLETED]
    *   **Description:** Move from a single flat image to a multi-layer stack.
    *   **Status:** Implemented. Includes `List<Layer>` model, Composite Rendering, and `SELayerPanel` UI.
    *   **Note:** Layers are flattened to a single image on export for game engine compatibility.

2.  **Project Persistence** [COMPLETED]
    *   **Description:** Save editor state (layers, visibility, opacity) to a dedicated project file (`.sprproj` / JSON).
    *   **Status:** Implemented. Uses `SpriteProject` class with GZIP+Base64 encoding for pixel data.
    *   **Why:** Enables non-destructive editing and resuming work with layers intact.

3.  **Universal Brush System**
    *   **Description:** Abstract the drawing logic so "Pencil", "Eraser", and "Pattern Stamp" share a common interface.
    *   **Requirements:** `Brush` interface, `PixelBrush`, `ShapeBrush`, `PatternBrush`. Support for "Custom Brushes" (using a selection as a brush).

4.  **File Format Support (Interoperability)**
    *   **Description:** Interoperability with standard tools.
    *   **Goal:** Import `.ase` / `.aseprite` (Aseprite) files. This allows users to create art in Aseprite and bring it into the engine.

## Phase 2: Workflow Enhancements
Tools that speed up the creation process.

5.  **Selection Tools**
    *   **Description:** Robust selection capabilities beyond rectangles.
    *   **Goal:** Magic Wand (Color Select), Polygon Lasso, "Select All of Color".

6.  **Symmetry / Mirror Drawing**
    *   **Description:** Real-time mirroring of drawing operations on X and Y axes.
    *   **Why:** Standard feature in Pyxel Edit, Aseprite, Tiled.

7.  **Onion Skinning**
    *   **Description:** See previous/next frames faintly while animating.
    *   **Requirements:** Animation timeline integration.

## Phase 3: Advanced Features
Differentiation features that provide unique value.

8.  **Tile Instancing (Pyxel Edit Style)**
    *   **Description:** Editing a tile on the map updates the source tile and all other instances of it instantly.
    *   **Why:** Huge time saver for tilemap creation.

9.  **Auto-tiling (Wang/Blob)**
    *   **Description:** Automatically selecting the correct tile variation based on neighbors (corners, edges).

10. **Reference Layers**
    *   **Description:** Layers that hold reference images but are excluded from the final export/game data.

## Execution Plan (Next Steps)

With Layers and Persistence complete, the next step is to upgrade the drawing tools.

**Plan for Universal Brush System:**
1.  Define `Brush` interface in `com.bobsgame.editor.Tools`.
2.  Refactor `SECanvas` mouse handling to delegate to the active `Brush`.
3.  Implement `PixelBrush` (default 1x1), `EraserBrush` (Transparent color), and `StampBrush` (Clipboard).
