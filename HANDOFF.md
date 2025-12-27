# Handoff

**Date:** 2025-12-27
**Version:** 0.1.4
**Build Status:** Passing (Java 21)

## Completed Tasks
1.  **Environment Fix**: Switched to Java 21 to resolve Gradle 8.8 compatibility issues.
2.  **Documentation**:
    *   Consolidated `LLM_INSTRUCTIONS.md`.
    *   Updated `docs/dashboard.md` with latest submodule status.
    *   Updated `CHANGELOG.md` and `VERSION.md`.
3.  **Feature Implementation**:
    *   Implemented "Move Map Up/Down" functionality in `Map.java` (`shiftMap` method).
    *   Added "Move Up" and "Move Down" buttons to `ControlPanel.java`.
4.  **Cleanup**: Updated `docs/todo.txt` to reflect completed tasks.

## Next Steps
*   **Undo System**: "make undo better/not wrap around/skip" is the next logical step.
*   **Performance**: "drawing/moving/filling/copy/paste tiles REALLY SLOW with sprite layer on and lots of Sprites."
*   **UI Improvements**: "pull off tile panel/ control panel. dockable?"

## Notes
*   The project is now fully compatible with Java 21.
*   The "Merge Project" feature from the previous session is confirmed to be compiling and integrated.
