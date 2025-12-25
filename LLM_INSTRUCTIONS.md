# Universal LLM Instructions

This file contains the core instructions for all AI models working on **BobsGameOnline**. All models (Claude, Copilot, Gemini, GPT, etc.) must adhere to these protocols.

## Project Context
*   **Language**: Java (Modern versions preferred, currently Java 21).
*   **Build System**: Gradle 8.8.
*   **Key Libraries**: LWJGL 3, GeoIP2, LZ4, JInput, TWL.
*   **Architecture**: Client-Server model with Lua scripting support.

## Workflow Protocols

### 1. Version Control
*   **Commit Messages**: Clear, descriptive, imperative mood (e.g., "Add feature X", "Fix bug Y").
*   **Submodules**: Always check for submodule updates when pulling. Use `git submodule update --init --recursive`.
*   **Branches**: Create feature branches for new work. Merge to `main` via PR or direct merge if authorized.

### 2. Versioning & Changelog
*   **Source of Truth**: `VERSION.md` contains the current version number (e.g., `0.1.0`).
*   **Updates**:
    *   **Every significant change or build MUST increment the version number.**
    *   Update `CHANGELOG.md` with a new entry under the new version.
    *   Commit message should reference the version bump (e.g., "Bump version to 0.1.1").
    *   Internal code should reference `VERSION.md` if possible, rather than hardcoding strings.

### 3. Code Style
*   Follow standard Java naming conventions.
*   Prioritize readability and maintainability.
*   Add Javadoc for public methods and classes.
*   Use meaningful variable names.

### 4. Documentation
*   Keep `docs/` updated.
*   Update `docs/dashboard.md` if project structure changes or submodules are updated.
*   Maintain `ROADMAP.md`.

## Model-Specific Instructions
*   **Claude**: Refer to `CLAUDE.md` (Appends to this file).
*   **Copilot**: Refer to `.github/copilot-instructions.md` (Appends to this file).
*   **Gemini**: Refer to `GEMINI.md` (Appends to this file).
*   **GPT**: Refer to `GPT.md` (Appends to this file).
