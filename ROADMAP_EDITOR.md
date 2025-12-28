# Editor Roadmap

This roadmap outlines the prioritized implementation of features for the Bob's Game Editor (Legacy Swing Editor), based on research from industry-standard tools like Aseprite, Tiled, and Pyxel Edit.

## Phase 1: Foundations (Immediate Priority)
These features are essential for any usable editor and are currently missing or underdeveloped.

1.  **Layer System (Implementation Goal)**
    *   **Description:** Move from a single flat image to a multi-layer stack.
    *   **Requirements:** `Layer` class, `LayerManager`, UI for Layer List (Add, Remove, Toggle Visibility, Opacity).
    *   **Why:** Foundational for all advanced editing.

2.  **Universal Brush System**
    *   **Description:** Abstract the drawing logic so "Pencil", "Eraser", and "Pattern Stamp" share a common interface.
    *   **Requirements:** `Brush` interface, `PixelBrush`, `ShapeBrush`, `PatternBrush`. Support for "Custom Brushes" (using a selection as a brush).

3.  **File Format Support**
    *   **Description:** Interoperability with standard tools.
    *   **Goal:** Import `.ase` / `.aseprite` (Aseprite) files. This allows users to create art in Aseprite and bring it into the engine.

## Phase 2: Workflow Enhancements
Tools that speed up the creation process.

4.  **Selection Tools**
    *   **Description:** Robust selection capabilities beyond rectangles.
    *   **Goal:** Magic Wand (Color Select), Polygon Lasso, "Select All of Color".

5.  **Symmetry / Mirror Drawing**
    *   **Description:** Real-time mirroring of drawing operations on X and Y axes.
    *   **Why:** Standard feature in Pyxel Edit, Aseprite, Tiled.

6.  **Onion Skinning**
    *   **Description:** See previous/next frames faintly while animating.
    *   **Requirements:** Animation timeline integration.

## Phase 3: Advanced Features
Differentiation features that provide unique value.

7.  **Tile Instancing (Pyxel Edit Style)**
    *   **Description:** Editing a tile on the map updates the source tile and all other instances of it instantly.
    *   **Why:** Huge time saver for tilemap creation.

8.  **Auto-tiling (Wang/Blob)**
    *   **Description:** Automatically selecting the correct tile variation based on neighbors (corners, edges).

9.  **Reference Layers**
    *   **Description:** Layers that hold reference images but are excluded from the final export/game data.

## Execution Plan (Immediate)

I will begin by implementing the **Layer System**. This is the prerequisite for almost all other features (Animation, Reference Layers, Onion Skinning).

**Plan for Layer System:**
1.  Refactor `MapCanvas` / `SpriteEditor` to use a `List<Layer>` instead of a single `BufferedImage`.
2.  Create a `Layer` class (name, visible, opacity, BufferedImage).
3.  Add a `LayersPanel` to the UI to manage these layers.
