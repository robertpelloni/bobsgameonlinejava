See `HANDOFF.md` for session history and `docs/DASHBOARD.md` for project structure.

## LLM Guidelines
1.  **Versioning:** Always check `VERSION.md` and `CHANGELOG.md`. Increment version on significant changes.
2.  **Code Style:** Follow existing Java patterns. Use `com.bobsgame.puzzle` for new puzzle engine code.
3.  **Refactoring:** Be careful when modifying `BobsGame.java` or `ClientGameEngine.java` as they bridge legacy and new code.
4.  **Networking:** Use `BobNet` constants for packet headers. `GameClientTCP` handles low-level I/O, `GameLogic` handles game state sync.
5.  **Build:** Always verify with `./gradlew clean build`.
