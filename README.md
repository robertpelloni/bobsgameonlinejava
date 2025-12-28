# Bob's Game Modernization

This repository contains the modernized source code for "Bob's Game" (2012), updated to run on modern Java 21 infrastructure.

## Architecture

The project is split into three Gradle modules:

- **`:client`**: The game client (LWJGL 3, OpenGL, OpenAL).
- **`:server`**: The backend infrastructure (Game Server, Index Server, STUN Server).
- **`:shared`**: Shared logic, networking packets, and utilities.

For a detailed breakdown of the project structure and dependencies, see [STRUCTURE.md](STRUCTURE.md).

## Prerequisites

- JDK 21
- Docker (optional, for server deployment)

## Building

To build all modules:

```bash
./gradlew build
```

## Running the Client

```bash
./gradlew :client:run
```

## Running the Editor

To run the legacy Swing-based Level Editor:

```bash
./gradlew :client:runEditor
```

**New Editor Features:**
- **Select All (Ctrl+A)**: Selects the entire map, sprite, or tileset in the respective editors.
- **Replace Color**: New menu item in "Palette Tools" to swap a color index globally across the tileset.

## Running the Server

You can run the server locally or via Docker.

### Local

```bash
./gradlew :server:run
```

### Docker

Build and start the full stack (Game Server, STUN Server, MySQL Database):

```bash
docker-compose up --build
```

Configuration is handled via Environment Variables (see `docker-compose.yml`) or a `server.properties` file in the working directory.

## Features

- **Modern Tech Stack**: Java 21, Gradle 8.5, LWJGL 3, Netty 4, HikariCP.
- **Security**: BCrypt password hashing with automatic legacy migration.
- **Containerization**: Full Docker support.
- **CI/CD**: GitHub Actions workflow included.

## Changelog

### Modernization Phase (Current)
- **Migrated to Gradle**: Multi-module project structure.
- **Updated Java**: Targeted Java 21.
- **Upgraded LWJGL**: Migrated from 2 to 3 (GLFW, OpenAL, STB).
- **Upgraded Netty**: Migrated from 3 to 4.
- **Security**: Added BCrypt password hashing.
- **Infrastructure**: Added Docker and CI/CD support.
- **Editor**: Re-enabled legacy Swing Editor, added 'Select All' and 'Replace Color' features.

## Roadmap

- [x] Modernize Build System (Gradle)
- [x] Upgrade Java to 21
- [x] Migrate Networking to Netty 4
- [x] Migrate Graphics to LWJGL 3
- [x] Re-enable Level Editor
- [ ] Port Editor to LibGDX / Scene2D (Future)
- [ ] Implement remaining TODO items
