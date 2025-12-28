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

3.  **Universal Brush System** [COMPLETED]
    *   **Description:** Abstract the drawing logic so "Pencil", "Eraser", and "Pattern Stamp" share a common interface.
    *   **Status:** Implemented. `Brush` interface created with `PixelBrush`, `EraserBrush`, `FillBrush`. Added `SEToolsPanel`.
    *   **Requirements:** `Brush` interface, `PixelBrush`, `ShapeBrush`, `PatternBrush`. Support for "Custom Brushes" (using a selection as a brush).

4.  **File Format Support (Interoperability)** [COMPLETED]
    *   **Description:** Interoperability with standard tools.
    *   **Status:** Implemented `AsepriteImporter` and `AsepriteParser`. Can import `.ase`/`.aseprite` files (Indexed Mode).
    *   **Goal:** Import `.ase` / `.aseprite` (Aseprite) files. This allows users to create art in Aseprite and bring it into the engine.

## Phase 2: Workflow Enhancements
Tools that speed up the creation process.

5.  **Selection Tools** [COMPLETED]
    *   **Description:** Robust selection capabilities beyond rectangles.
    *   **Status:** Implemented `MagicWandBrush` and mask-based `SelectionArea`.
    *   **Goal:** Magic Wand (Color Select), Polygon Lasso, "Select All of Color".

6.  **Symmetry / Mirror Drawing** [COMPLETED]
    *   **Description:** Real-time mirroring of drawing operations on X and Y axes.
    *   **Status:** Implemented Y-Axis symmetry (Quad symmetry supported). Updated `SECanvas` rendering and `setPixel` logic.
    *   **Why:** Standard feature in Pyxel Edit, Aseprite, Tiled.

7.  **Onion Skinning** [COMPLETED]
    *   **Description:** See previous/next frames faintly while animating.
    *   **Status:** Implemented via `SECanvas.repaintBufferImage` rendering prev/next frames with alpha blending.
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

With core drawing and animation tools complete, the next phase focuses on **TileMap** efficiency.

**Plan for Tile Instancing:**
1.  Analyze `MapCanvas` / `MTECanvas` rendering loop.
2.  Implement a system where modifying a tile in `TileCanvas` (Tileset) automatically triggers a repaint of `MapCanvas`.
3.  Ensure `Tileset` modification propagates to all maps using that tileset.
