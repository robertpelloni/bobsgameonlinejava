# Project Structure & Dashboard

## Modules

### Root Project
*   `build.gradle`: Root build configuration (versions, plugins, repositories).
*   `settings.gradle`: Module definitions (`include 'client', 'server', 'shared'`).

### `:shared`
*   **Path**: `shared/src/main/java/com/bobsgame`
*   **Purpose**: Common logic shared between Client and Server.
*   **Key Packages**:
    *   `utils`: `HexDumper`, `Compression`, `Ternary`.
    *   `net`: `WaveData` (Audio), Networking constants.

### `:server`
*   **Path**: `server/src/main/java/com/bobsgame`
*   **Purpose**: The authoritative game server and STUN server.
*   **Key Components**:
    *   `ServerMain`: Entry point.
    *   `GameServerTCP`: Netty-based TCP server handler.
    *   `UDPConnection`: Netty-based UDP handling.
    *   `DBHandler`: Database interaction (HikariCP/MySQL).
    *   `Match`: Handles game session logic.

### `:client`
*   **Path**: `client/src/main/java/com/bobsgame`
*   **Purpose**: The Game Client (LWJGL 3) and Level Editor (Swing).
*   **Key Components**:
    *   `ClientMain`: Game entry point.
    *   `EditorMain`: Level Editor entry point (Swing).
    *   `GameClientTCP`: Netty-based client networking.
    *   `Texture`, `Font`: Custom rendering wrappers (stb/GL).

## Resources (`res/`)
*   `gfx/`: Sprites and textures.
*   `sfx/`: Audio files.
*   `music/`: Background music.
*   `maps/`: Game maps (`.map`).
*   `tilesets/`: Tileset definitions.

## Documentation
*   `README.md`: General usage and setup.
*   `HANDOFF.md`: Modernization details and notes for developers.
*   `docs/`: Legacy documentation and todo lists.
