# Project Dashboard

**Last Updated:** 2025-12-27
**Build Status:** Passing (Java 21)
**Current Version:** 0.1.3

## Project Structure

The project is organized as follows:

*   `src/main/java`: Main Java source code.
*   `src/main/resources`: Game assets and resources.
*   `libs/`: External dependencies managed as git submodules.
*   `docs/`: Project documentation.
*   `lua/`: Lua scripts for game logic.
*   `res/`: Additional resources (GeoIP database, text files).

## Submodules Status

| Submodule | Path | Version/Commit |
| :--- | :--- | :--- |
| **GeoIP2-java** | `libs/GeoIP2-java` | `10d56995` (v4.3.0+) |
| **commons-lang** | `libs/commons-lang` | `6a4979ba` (rel/commons-lang-3.20.0+) |
| **jinput** | `libs/jinput` | `45fe7256` (2.0.10+) |
| **lwjgl3** | `libs/lwjgl3` | `50f3b0e6` (3.3.6+) |
| **lz4-java** | `libs/lz4-java` | `be9ce575` (1.8.0+) |
| **micromod** | `libs/micromod` | `68f27411` (master) |
| **mysql-connector-j** | `libs/mysql-connector-j` | `a7b3c94f` (9.5.0) |
| **twl-lwjgl3** | `libs/twl-lwjgl3` | `647ec347` (master) |
| **xpp3** | `libs/xpp3` | `68498e76` (xpp3-1.1.4c.0+) |
| **xz-java** | `libs/xz-java` | `e52d9ad6` (v1.11+) |

## Recent Activity
*   **Feature**: Implemented "Merge Project" functionality.
*   **Maintenance**: Updated all submodules to latest upstream.
*   **Environment**: Standardized on Java 21.
