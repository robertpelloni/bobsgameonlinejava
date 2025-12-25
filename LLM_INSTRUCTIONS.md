# Universal LLM Instructions

This file contains the core instructions for all AI models working on **BobsGameOnline**.

## Project Context
*   **Language**: Java (Modern versions preferred).
*   **Build System**: Gradle.
*   **Key Libraries**: LWJGL 3, GeoIP2, LZ4, JInput.
*   **Architecture**: Client-Server model with Lua scripting support.

## Workflow Protocols

### 1. Version Control
*   **Commit Messages**: Clear, descriptive, imperative mood (e.g., "Add feature X", "Fix bug Y").
*   **Submodules**: Always check for submodule updates when pulling. Use `git submodule update --init --recursive`.
*   **Branches**: Create feature branches for new work. Merge to `main` via PR or direct merge if authorized.

### 2. Versioning & Changelog
*   **Source of Truth**: `VERSION.md` contains the current version number (e.g., `0.1.0`).
*   **Updates**:
    *   Every significant change or build should increment the version number.
    *   Update `CHANGELOG.md` with a new entry under the new version.
    *   Commit message should reference the version bump (e.g., "Bump version to 0.1.1").

### 3. Code Style
*   Follow standard Java naming conventions.
*   Prioritize readability and maintainability.
*   Add Javadoc for public methods and classes.
*   Use meaningful variable names.

### 4. Documentation
*   Keep `docs/` updated.
*   Update `docs/dashboard.md` if project structure changes.
*   Maintain `ROADMAP.md`.

## Model-Specific Instructions
*   **Claude**: Refer to `CLAUDE.md` for specific context.
*   **Copilot**: Refer to `.github/copilot-instructions.md`.
*   **Gemini**: Refer to `GEMINI.md`.
*   **GPT**: Refer to `GPT.md`.
