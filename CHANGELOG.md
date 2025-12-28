# Changelog

## [0.0.2] - 2024-05-23
### Added
- **Puzzle Engine Core:** Implemented `GameLogic`, `Grid`, `Piece`, `Block` classes in `com.bobsgame.puzzle`.
- **Game Configuration:** Implemented `GameType`, `DifficultyType`, `GameSequence` and related Enums.
- **Networking:** Added `Bobs_Game_Frame_Packet` to `BobNet`. Updated `GameClientTCP` to handle frame packets and forward to `GameLogic`.
- **Editor:** Created `CustomGameEditor` UI structure using TWL.
- **Documentation:** Added `VERSION.md`, `docs/DASHBOARD.md`, `HANDOFF.md`.

### Changed
- **Refactoring:** Updated `BobsGame.java` and `ClientGameEngine.java` to integrate with the new `com.bobsgame.puzzle` package.
- **Legacy Fixes:** Resolved Netty 3 vs 4 conflicts in `FriendUDPConnection` and `BobsGame` by removing `MessageEvent` dependencies and using string-based message handling.
- **Build:** Fixed compilation errors in `GameLogic` and `FriendCharacter`.

## [0.0.1] - 2024-05-22
### Added
- Initial project structure analysis.
- Backported `FriendsPanel` and `MessagesPanel` UI.
- Created initial documentation structure.
