# Developer Handoff - Modernization Phase 1

## Overview
This repository contains the modernized source code for "Bob's Game" (2012). The codebase has been migrated from a legacy Eclipse/Ant structure using Java 6/7, LWJGL 2, and Netty 3 to a modern **Gradle** multi-module project using **Java 21**, **LWJGL 3**, and **Netty 4**.

## Achievements
1.  **Build System**:
    *   Converted to Gradle 8.5.
    *   Split into modules: `:client` (Game/Editor), `:server` (Backend), `:shared` (Common logic).
    *   Added Docker support (`Dockerfile`, `docker-compose.yml`) for the server.

2.  **Core Libraries**:
    *   **LWJGL 3**: Migrated windowing (`GLFW`), input (`GLFW` callbacks), and audio (`OpenAL`). Replaced Slick2D text/texture loading with custom `stb` based implementations.
    *   **Netty 4**: Rewrote networking layer (`GameServerTCP`, `GameClientTCP`, `UDPConnection`) to use modern Netty pipelines and handlers.
    *   **Database**: Migrated to `HikariCP` and modern MySQL drivers.
    *   **Security**: Implemented `BCrypt` password hashing with legacy migration support.

3.  **Level Editor**:
    *   Restored functionality of the legacy Swing-based Editor (`EditorMain`).
    *   Implemented new features:
        *   **Replace Color**: Swap palette colors globally.
        *   **Select All (Ctrl+A)**: Select full map/sprite bounds.
        *   **Paste Non-Zero (Ctrl+Shift+V)**: Paste bitmask/transparently.

## Key Files & Locations
*   **Build**: `build.gradle`, `settings.gradle`, `client/build.gradle`, `server/build.gradle`.
*   **Client Entry**: `com.bobsgame.ClientMain` (LWJGL 3 game).
*   **Editor Entry**: `com.bobsgame.EditorMain` (Swing editor).
*   **Server Entry**: `com.bobsgame.ServerMain`, `com.bobsgame.STUNServerMain`.
*   **Documentation**: `README.md`, `STRUCTURE.md`.

## How to Run
*   **Game**: `./gradlew :client:run`
*   **Editor**: `./gradlew :client:runEditor`
*   **Server**: `./gradlew :server:run` (or via Docker)
*   **Tests**: `./gradlew test`

## Known Issues / TODOs
*   **Editor Rendering**: The Editor uses AWT/Swing. Interaction with the LWJGL 3 game engine (if integrated deeply) might need `lwjgl3-awt` in the future, but currently, they operate reasonably well side-by-side or decoupled.
*   **Legacy TODOs**: See `docs/todo.txt` for original feature requests.
*   **Spotless**: Code formatting was disabled due to legacy code incompatibilities. It can be re-enabled incrementally.

## Next Steps for Next Developer
1.  **Optimization**: Address "Redo Faster" or "Copy Enabled Layers" from `docs/todo.txt`.
2.  **LibGDX Migration**: Evaluate porting the custom engine to LibGDX for better cross-platform support (Web/Mobile), as hinted by the previous developer.
3.  **AI Integration**: The modernized stack (Java 21) allows easier integration with modern AI tools/libraries.
