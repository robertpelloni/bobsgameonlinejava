# bobsgameonlinejava — the original Java engine for bob's game online

The original **Java client and server for "bob's game online"**: a 2D MMORPG engine and world (Gradle build, `client.iml`, `bobsgame.sql` schema, `bobsgame_v8830.zip` — the v8830 archive the modern ports are restoring from). This is the codebase the newer bob's game engines derive from:

- [`bobsgameweb`](https://github.com/robertpelloni/bobsgameweb) — TypeScript/PixiJS engine ("okgame"), the active build
- [`okgame`](https://github.com/robertpelloni/okgame) — C++ engine
- [`bg`](https://github.com/robertpelloni/bg) — umbrella workspace; this repo is one of its submodules

## ⚠️ About the previous README

Until this rewrite, this repo's README described a completely different project ("Jules Autopilot — a command center for Google Jules"). That text belonged to the `backend-go/` experiment directory in this tree (a Go dashboard/orchestration side-project that was developed here) and was pasted over the repo README by a bot. It said nothing about the Java engine that is this repo's actual content. If you came here from that README: the Java engine is the repo; `backend-go/` is a side experiment inside it.

## Layout

```
build.gradle, gradle.properties   Java build
client.iml                        IntelliJ module for the client
bobsgame.sql                      Database schema
bobsgame_v8830.zip                The v8830 release archive (restoration source)
docs/                             Design and engine documentation
backend-go/                       Unrelated Go experiment (see note above)
Dockerfile, docker-compose.yml    Container setup
```

Project docs: `VISION.md`, `ROADMAP.md`, `ROADMAP_EDITOR.md`, `STRUCTURE.md`, `FEATURES_RESEARCH.md`, `TODO.md`, `DEPLOY.md`.

## Status

Legacy engine, kept as the reference implementation and restoration source. Active development happens in `bobsgameweb`; this tree changes mainly when the porting work in `bg` needs something from the original.
