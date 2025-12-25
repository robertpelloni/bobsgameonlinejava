# Project Dashboard

## Project Overview
**BobsGameOnline** is a Java-based game project utilizing LWJGL 3 for graphics and input, with various other libraries for compression, networking, and data handling.

## Directory Structure

*   **`src/`**: Main source code directory. Contains the Java source files and resources.
*   **`libs/`**: External dependencies managed as git submodules.
*   **`lua/`**: Lua scripts used for game logic and scripting.
*   **`res/`**: Game resources such as textures, sounds, and data files.
*   **`docs/`**: Project documentation.
*   **`gradle/`**: Gradle wrapper files.
*   **`target/`**: Build output directory.

## Submodules Status

| Submodule | Path | Description |
| :--- | :--- | :--- |
| **GeoIP2-java** | `libs/GeoIP2-java` | GeoIP2 API for Java. |
| **lwjgl3** | `libs/lwjgl3` | Lightweight Java Game Library 3. |
| **lz4-java** | `libs/lz4-java` | LZ4 compression for Java. |
| **jinput** | `libs/jinput` | Java Game Controller API. |
| **micromod** | `libs/micromod` | MOD player. |
| **twl-lwjgl3** | `libs/twl-lwjgl3` | Themable Widget Library for LWJGL 3. |
| **mysql-connector-j** | `libs/mysql-connector-j` | MySQL JDBC driver. |
| **xpp3** | `libs/xpp3` | XML Pull Parser. |
| **xz-java** | `libs/xz-java` | XZ data compression. |
| **commons-lang** | `libs/commons-lang` | Apache Commons Lang. |

## Build Information
*   **Build System**: Gradle
*   **Java Version**: (Check `build.gradle` or `java -version`)

## Recent Updates
*   Submodules updated to latest upstream versions.
*   Feature branches merged into main.
