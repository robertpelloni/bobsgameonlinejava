# Changelog

All notable changes to this project will be documented in this file.

## [2.0.33] - 2026-11-08
### Added
- Configured Gradle `test` source set to include `shared/src/test/java`, enabling automated CI and local execution of shared engine parity test suites.
- Added comprehensive unit tests for `Grid` initialization, dimensions, boundary bounds, and garbage pattern toggling in `GridLogicTest` and `ParityTest`.
- Made `Grid.getWidth()` and `Grid.getHeight()` null-safe to support standalone grid instances when running unit tests outside of full `GameLogic` runtime.
- Refactored `tools/generate_dashboard.sh` to resolve root paths dynamically and regenerate `DASHBOARD.md` cleanly without polluting submodules.

## [2.0.32] - 2026-05-01
### Added
- Implemented Polygon Lasso tool in Sprite Editor using ray-casting point-in-polygon mask generation. Added `onMouseMove` to `Brush` interface to support interactive previews.

## [2.0.31] - 2026-04-27

### Fixed
- Stabilized documentation and submodule sync loop.

## [2.0.31] - 2026-04-26

### Added
- Created `VISION.md`, `MEMORY.md`, `DEPLOY.md`, and `TODO.md` to comprehensively document project goals, observations, deployment steps, and upcoming tasks.
- Added new references for Defold, Love2D, Phaser, and `bobui` to the git submodules for feature analysis and integration.
- Updated `AGENTS.md`, `CLAUDE.md`, `GEMINI.md`, `GPT.md`, `copilot-instructions.md`, and `LLM_INSTRUCTIONS.md` to point to the universal LLM instructions.

### Changed
- Bumped `VERSION.md` to `2.0.31`.

## [2.0.30] - 2026-04-06

### Added
- **Scene2D Unified Template Library:** Added a combined Scene2D library table that displays built-in templates, preset slots, and recent history entries in a single list with mixed-source badges and direct load/apply actions.

### Changed
- Refactored Scene2D table rebuilding to support mixed-source template cards.
- Bumped `VERSION.md` to `2.0.30`.

### Validation
- `./gradlew compileJava` ✅

## [2.0.29] - 2026-04-06

### Added
- **Scene2D Template-to-Slot Shortcuts:** `CustomGameEditor.java` now lets creators save built-in templates directly into preset slots 1–3 from the Scene2D template browser without first overwriting the active draft.

### Changed
- Scene2D built-in template browsing and saved-slot authoring now connect through direct slot-save actions, making the template workflow less fragmented.
- Bumped `VERSION.md` to `2.0.29`.

### Validation
- `./gradlew compileJava` ✅

## [2.0.28] - 2026-04-06

### Added
- **Scene2D Template Browser:** `CustomGameEditor.java` now renders a browsable preset catalog table with family summaries and one-click apply buttons for the built-in starter templates.

### Changed
- Scene2D preset browsing now surfaces family, mode, grid, gravity/lock, preview, and chain summaries directly in the editor instead of relying only on the top preset rows.
- Bumped `VERSION.md` to `2.0.28`.

### Validation
- `./gradlew compileJava` ✅

## [2.0.27] - 2026-04-06

### Added
- **Scene2D Compact Preset Families:** `CustomGameEditor.java` now groups starter presets into clearer family rows and adds curated `Sprint Drop`, `Zen Garden`, and `Micro Stack` templates alongside the original presets.

### Changed
- Expanded preset application so the Scene2D editor now seeds gravity, lock delay, and next-piece preview counts more explicitly for each family starter.
- Bumped `VERSION.md` to `2.0.27`.

### Validation
- `./gradlew compileJava` ✅

## [2.0.26] - 2026-04-06

### Added
- **Scene2D Saved Template Slot Summaries:** `CustomGameEditor.java` now renders compact preset-slot status rows so each in-memory slot advertises its stored ruleset name, mode, piece count, and rotation count.

### Changed
- Preset slot saves/loads now refresh a dedicated Scene2D template summary table, reducing guesswork when authoring multiple rulesets in one session.
- Bumped `VERSION.md` to `2.0.26`.

### Validation
- `./gradlew compileJava` ✅

## [2.0.25] - 2026-04-05

### Added
- **Scene2D Conversion-Pair Editing:** `CustomGameEditor.java` now supports focused first-pass touching-block conversion authoring with add/clear conversion flows and visible conversion summaries.

### Changed
- Expanded block detail and summary reporting so conversion-pair data appears alongside reward, chain, palette, and field-effect authoring.
- Bumped `VERSION.md` to `2.0.25`.

### Validation
- `./gradlew compileJava` ✅

## [2.0.24] - 2026-04-05

### Added
- **Scene2D Advanced Field-Effect Toggles:** `CustomGameEditor.java` now exposes engine-backed field-effect toggles for ignoring move-down gravity, removing same-color field blocks on set, and diamond-color field swaps.

### Changed
- Expanded block behavior reporting so field-wide consequences appear alongside the existing chain and reward hooks.
- Bumped `VERSION.md` to `2.0.24`.

### Validation
- `./gradlew compileJava` ✅

## [2.0.23] - 2026-04-05

### Added
- **Scene2D Block Gameplay Hooks:** `CustomGameEditor.java` now exposes reward-piece assignment plus focused chain-behavior toggles for selected blocks, including clear-every-other-line, ignore-chain, required-in-chain, and exploding-chain-link hooks.

### Changed
- Expanded block detail and summary readouts so reward-piece and chain-behavior authoring is visible alongside the existing block palette and special-color data.
- Bumped `VERSION.md` to `2.0.23`.

### Validation
- `./gradlew compileJava` ✅

## [2.0.22] - 2026-04-05

### Added
- **Scene2D Richer Block Color Set Editing:** `CustomGameEditor.java` now adds palette color growth/shrink controls and surfaces the full authored palette in the block detail readout.

### Changed
- Reused the current primary color as the palette-source color for new palette entries, keeping Scene2D color-set editing simple and coherent with the existing dialog-driven UI.
- Bumped `VERSION.md` to `2.0.22`.

### Validation
- `./gradlew compileJava` ✅

## [2.0.21] - 2026-04-05

### Added
- **Scene2D Deeper Block Rule Editing:** `CustomGameEditor.java` now exposes a focused second-pass block authoring set with special color, special chance/frequency, flashing special, match-any-color, and counter-type controls.

### Changed
- Expanded block detail readouts so authored special behavior is visible alongside color and usage data.
- Bumped `VERSION.md` to `2.0.21`.

### Validation
- `./gradlew compileJava` ✅

## [2.0.20] - 2026-04-05

### Added
- **Scene2D Block Type / Color Controls:** `CustomGameEditor.java` now exposes a focused first-pass block authoring workflow with block navigation, rename/color editing, normal/garbage/filler usage toggles, and block override assignment for the selected piece.

### Changed
- Bumped `VERSION.md` to `2.0.20`.

### Validation
- `./gradlew compileJava` ✅

## [2.0.19] - 2026-04-05

### Added
- **Scene2D Recent Action Breadcrumbs:** `CustomGameEditor.java` now shows a bounded recent-action panel that tracks meaningful authoring operations like presets, imports, shares, piece/rotation edits, and cleanup helpers.

### Changed
- Kept the breadcrumb feed intentionally focused on meaningful edits rather than noisy navigation clicks, making it useful as lightweight workflow memory without pretending to be full undo.
- Bumped `VERSION.md` to `2.0.19`.

### Validation
- `./gradlew compileJava` ✅

## [2.0.18] - 2026-04-05

### Added
- **Scene2D Center-All Helper:** `CustomGameEditor.java` now provides a one-click helper to center every rotation in the selected piece, making full-set layout cleanup faster after import, duplication, and normalize-all passes.

### Changed
- Reused a shared centering helper so single-rotation and full-set centering follow the same geometry logic.
- Bumped `VERSION.md` to `2.0.18`.

### Validation
- `./gradlew compileJava` ✅

## [2.0.17] - 2026-04-05

### Added
- **Scene2D Recent Share / Import History:** `CustomGameEditor.java` now adds Scene2D import/share entry points plus a bounded recent-history list with quick load/copy actions.

### Changed
- Share flow now copies to the system clipboard when available and falls back to a manual-copy string dialog when clipboard access is unavailable.
- Bumped `VERSION.md` to `2.0.17`.

### Validation
- `./gradlew compileJava` ✅

## [2.0.16] - 2026-04-05

### Added
- **Scene2D Empty Rotation Cleanup:** `CustomGameEditor.java` now provides a one-click helper to remove empty rotations after confirmation, improving cleanup after experimentation and duplication.

### Changed
- Bumped `VERSION.md` to `2.0.16`.

### Validation
- `./gradlew compileJava` ✅

## [2.0.15] - 2026-04-05

### Added
- **Scene2D Normalize-All Helper:** `CustomGameEditor.java` now provides a one-click helper to normalize every rotation in the selected piece, making full-set cleanup much faster.

### Changed
- Bumped `VERSION.md` to `2.0.15`.

### Validation
- `./gradlew compileJava` ✅

## [2.0.14] - 2026-04-05

### Added
- **Scene2D Duplicate Cleanup Helper:** `CustomGameEditor.java` now provides a one-click duplicate-rotation cleanup action that removes redundant rotations after confirmation.

### Changed
- Bumped `VERSION.md` to `2.0.14`.

### Validation
- `./gradlew compileJava` ✅

## [2.0.13] - 2026-04-05

### Added
- **Scene2D Normalize / Center Helpers:** `CustomGameEditor.java` now provides one-click normalize and center helpers for the current rotation, making custom piece cleanup much faster.

### Changed
- Bumped `VERSION.md` to `2.0.13`.

### Validation
- `./gradlew compileJava` ✅

## [2.0.12] - 2026-04-05

### Added
- **Scene2D Symmetry & Duplicate Hints:** `CustomGameEditor.java` now reports current-rotation symmetry and highlights duplicate rotations in the rotation overview, giving authors direct feedback about redundant states.

### Changed
- Bumped `VERSION.md` to `2.0.12`.

### Validation
- `./gradlew compileJava` ✅

## [2.0.11] - 2026-04-05

### Added
- **Scene2D Rotation Analytics:** `CustomGameEditor.java` now reports current rotation bounding-box size plus unique-vs-duplicate rotation counts for the selected piece, and the rotation overview buttons include bounding-box information.

### Changed
- Bumped `VERSION.md` to `2.0.11`.

### Validation
- `./gradlew compileJava` ✅

## [2.0.10] - 2026-04-05

### Added
- **Scene2D Preset Shortcut Buttons:** `CustomGameEditor.java` now includes one-click Classic Drop, Cascade Puzzle, and Stack Arcade preset buttons layered on top of the in-session preset-slot workflow.

### Changed
- Bumped `VERSION.md` to `2.0.10`.

### Validation
- `./gradlew compileJava` ✅

## [2.0.9] - 2026-04-05

### Added
- **Scene2D Preset Slots:** `CustomGameEditor.java` now supports quick in-session preset save/load slots using deep-cloned `GameType` snapshots, enabling fast iteration without replacing the active draft permanently.

### Changed
- Bumped `VERSION.md` to `2.0.9`.

### Validation
- `./gradlew compileJava` ✅

## [2.0.8] - 2026-04-05

### Added
- **Scene2D Movement / Randomizer Toggles:** `CustomGameEditor.java` now exposes Scene2D checkboxes for next-piece preview, hold piece, bag randomizer, hard-drop punch-through, and multiple kick/movement options.

### Changed
- The Java editor summary now includes enabled movement/randomizer rules together with the earlier advanced rule set.
- Bumped `VERSION.md` to `2.0.8`.

### Validation
- `./gradlew compileJava` ✅

## [2.0.7] - 2026-04-05

### Added
- **Scene2D Advanced Rule Toggles:** `CustomGameEditor.java` now exposes Scene2D checkboxes for cascade gravity, disconnected-only gravity, row/column/diagonal chain checks, and recursive chain search.

### Changed
- The Java editor summary now reports enabled advanced rules explicitly.
- Bumped `VERSION.md` to `2.0.7`.

### Validation
- `./gradlew compileJava` ✅

## [2.0.6] - 2026-04-05

### Added
- **Scene2D Rotation Overview:** `CustomGameEditor.java` now builds a clickable rotation overview strip with per-rotation occupied-cell counts, allowing direct selection instead of only cycling next/previous.

### Changed
- Bumped `VERSION.md` to `2.0.6`.

### Validation
- `./gradlew compileJava` ✅

## [2.0.5] - 2026-04-05

### Added
- **Scene2D Duplication Workflow:** `CustomGameEditor.java` now supports duplicating the selected piece and duplicating the selected rotation, making iterative ruleset authoring much faster.

### Changed
- Bumped `VERSION.md` to `2.0.5`.

### Validation
- `./gradlew compileJava` ✅

## [2.0.4] - 2026-04-05

### Changed
- **Safer Scene2D Deletes:** Piece and rotation removals in `CustomGameEditor.java` now go through `Scene2DYesNoDialog` confirmations instead of deleting immediately.
- Bumped `VERSION.md` to `2.0.4`.

### Validation
- `./gradlew compileJava` ✅

## [2.0.3] - 2026-04-05

### Added
- **Scene2D Delete Controls:** `CustomGameEditor.java` now supports removing the selected piece and removing the selected rotation, extending the Scene2D editor beyond pure additive editing.

### Changed
- **Editor Guardrails:** Grid buttons are disabled when no piece is selected, and the editor now recovers selection cleanly after deleting a piece or rotation.
- Bumped `VERSION.md` to `2.0.3`.

### Validation
- `./gradlew compileJava` ✅

## [2.0.2] - 2026-04-05

### Fixed
- **nD Puzzle Bootstrap:** `NDPuzzleGame.java` now constructs `GameLogic` with a local `GameManager`/`Room` context, assigns the active `PuzzlePlayer`, and calls `update(0, 1)` with the modern signature.
- **TWL/LWJGL Input Bridge:** Added a tracked parent-repo override at `src/main/java/de/matthiasmann/twl/input/lwjgl/LWJGLInput.java` plus a Gradle exclude for the conflicting nested-submodule source file, avoiding the compile-time API drift that previously broke `compileJava`.

### Changed
- Bumped `VERSION.md` to `2.0.2`.

### Validation
- `./gradlew compileJava` ✅

## [2.0.1] - 2026-04-05

### Added
- **Scene2D Custom Piece Editing:** `CustomGameEditor.java` now maintains a live `GameType`, supports adding piece types and rotations, and exposes an interactive 4x4 rotation grid for sketching custom puzzle pieces.
- **Editor Summary Feedback:** Added current piece / rotation labels plus a summary line showing grid, mode, piece count, rotation count, and filled-cell count for the active rotation.

### Changed
- Bumped `VERSION.md` to `2.0.1`.
- The Scene2D custom editor now serves as a real editing surface instead of placeholder buttons.

### Validation
- `./gradlew compileJava` still fails, but the remaining failures are pre-existing and unrelated to this editor change:
  - `NDPuzzleGame.java` constructor/update mismatch with `GameLogic`
  - `libs/twl-lwjgl3` `GUI` API mismatches (`handleKey`, `handleMouse`, `handleMouseWheel`)


## [0.1.7] - 2026-02-07

### Fixed
- **Submodules**: Fixed `libs/aseprite-file` submodule pointer to a valid commit (`06b6189`) as the previous one was missing on the remote, causing clone failures.

## [0.1.6] - 2026-02-03

### Changed
- **Maintenance**: Automated cleanup and merge of feature branches.
- **Submodules**: Removed broken `GeoIP2-java` submodule.

## [0.1.5] - 2025-12-27

### Changed
- **Submodules**: Updated all submodules to latest upstream versions and merged upstream changes.
- **Dashboard**: Updated `docs/dashboard.md` with latest submodule status and project structure explanation.
- **Maintenance**: Merged all feature branches into `main` and ensured clean state.

## [0.1.4] - 2025-12-27

### Changed
- **Environment**: Standardized development environment on Java 21 to resolve Gradle 8.8 compatibility issues.
- **Documentation**: Consolidated and updated `LLM_INSTRUCTIONS.md` to serve as the single source of truth for all AI models.
- **Documentation**: Updated `docs/dashboard.md` with current submodule hashes and build status.

## [0.1.3] - 2025-12-26

### Added
- **Project Merging**: Implemented full project merging capabilities. Users can now merge maps, tilesets, palettes, sprites, and other data from another project zip file into the current project.
- **Documentation**: Updated `LLM_INSTRUCTIONS.md` with strict versioning protocols.
- **Dashboard**: Updated `docs/dashboard.md` with latest submodule status.

### Changed
- **Submodules**: Updated all submodules to latest upstream versions.
- **Branches**: Merged all local feature branches into `main`.
- **Versioning**: Incremented version to 0.1.3.

## [0.1.2] - 2025-12-25

### Added
- **Import Image to Tileset**: New feature in `Tileset Tools` menu allowing users to import BMP/PNG images directly into the tileset. Supports appending or overwriting, and automatically matches or adds colors to the palette.
- **Pattern Fill**: `MapCanvas` now supports filling a selection with a pattern from the tileset selection (tiling).
- **Random Sprite Export**: Added "Export Random Sprites" to Sprite Editor to export sprites marked as random.
- **Grid Layout**: Sprite Editor now displays frames in a responsive grid layout instead of a single line.
- **Palette Synchronization**: Implemented synchronized sorting and color addition across all tileset palettes to maintain consistency when modifying the master palette.

### Changed
- **Optimized Redo**: Improved performance of the Redo operation in `MapCanvas`.
- **Documentation**: Updated `dashboard.md`, `LLM_INSTRUCTIONS.md`, and `ROADMAP.md`.
- **Versioning**: Project now references `VERSION.md`.

### Fixed
- Fixed `GameSave.java` compilation errors (previous session).
- Fixed `SEFrameCanvas` painting logic.

## [0.1.1] - 2025-12-25

### Fixed
- Resolved compilation errors in `GameSave.java` by removing duplicate methods and fixing structure.
- Fixed duplicate field definitions in `CustomGameEditor.java`.
- Fixed missing method calls in `GLUtils.java` by correcting `BitmapFont` import.
- Fixed missing variables in `GameLogic.java`.

## [0.1.0] - 2025-12-25

### Added
- `docs/dashboard.md`: Project dashboard with submodule status and structure explanation.
- `VERSION.md`: Single source of truth for project version.
- `LLM_INSTRUCTIONS.md`: Unified instructions for AI assistants.

### Changed
- Merged feature branches:
    - `modernize-codebase-final-polish`
    - `modernize-codebase-polish`
    - `modernize-codebase-polish-2`
    - `modernize-java-project-part-2`
    - `new-feature-branch`
- Updated all submodules to latest upstream versions.
- Fixed `cpp_repo` submodule issue.
