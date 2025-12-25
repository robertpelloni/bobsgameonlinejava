# Project Dashboard

## Overview
**Project**: BobsGameOnline
**Version**: 0.1.1 (See [VERSION.md](../VERSION.md))
**Build System**: Gradle 8.8
**Java Version**: 21

## Directory Structure

*   **`src/main/java`**: Core Java source code.
    *   `com.bobsgame.client`: Client-side logic, rendering, input.
    *   `com.bobsgame.server`: Server-side logic.
    *   `com.bobsgame.shared`: Shared utilities and data structures.
*   **`src/main/resources`**: Java resources (config, etc.).
*   **`libs/`**: External dependencies managed as Git submodules.
*   **`lua/`**: Lua scripts for game logic.
*   **`res/`**: Game assets (images, sounds, data files).
*   **`docs/`**: Project documentation.
*   **`cpp_repo/`**: C++ native code repository.

## Submodules Status

| Submodule | Path | Version/Commit | Status |
| :--- | :--- | :--- | :--- |
| **GeoIP2-java** | `libs/GeoIP2-java` | `10d5699` (v4.3.0+) | Up to date |
| **commons-lang** | `libs/commons-lang` | `6a4979b` (3.20.0+) | Up to date |
| **jinput** | `libs/jinput` | `45fe725` (2.0.10+) | Up to date |
| **lwjgl3** | `libs/lwjgl3` | `50f3b0e` (3.3.6+) | Up to date |
| **lz4-java** | `libs/lz4-java` | `be9ce57` (1.8.0+) | Up to date |
| **micromod** | `libs/micromod` | `68f2741` (master) | Up to date |
| **mysql-connector-j** | `libs/mysql-connector-j` | `a7b3c94` (9.5.0) | Up to date |
| **twl-lwjgl3** | `libs/twl-lwjgl3` | `647ec34` (master) | Up to date |
| **xpp3** | `libs/xpp3` | `68498e7` (1.1.4c+) | Up to date |
| **xz-java** | `libs/xz-java` | `e52d9ad` (v1.11+) | Up to date |

## Recent Activity
*   **2025-12-25**: Updated all submodules to latest upstream.
*   **2025-12-25**: Fixed build issues in `GameSave.java`, `CustomGameEditor.java`, and `GLUtils.java`.
*   **2025-12-25**: Refactored documentation and instructions.

## Build Status
*   **Last Build**: Successful (Gradle build & test)
*   **Date**: 2025-12-25
