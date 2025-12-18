package com.bobsgame.client.engine.game.nd.bobsgame.game;

import com.bobsgame.client.engine.game.nd.bobsgame.game.GameType.GarbageSpawnRule;
import com.bobsgame.client.engine.game.nd.bobsgame.game.GameType.VSGarbageRule;

public class Room {

    public String uuid = "";
    public String name = "New Room";

    public GameSequence gameSequence = null;
    public String room_DifficultyName = "";

    // Multiplayer Options
    public boolean multiplayer_AllowDifferentGameSequences = false;
    public boolean multiplayer_AllowDifferentDifficulties = false;
    public boolean multiplayer_PrivateRoom = false;
    public boolean multiplayer_TournamentRoom = false;
    public int multiplayer_MaxPlayers = 0; // 0 = unlimited
    public boolean multiplayer_AllowNewPlayersDuringGame = true;
    public boolean multiplayer_UseTeams = false;

    public boolean multiplayer_GameEndsWhenOnePlayerRemains = true;
    public boolean multiplayer_GameEndsWhenSomeoneCompletesCreditsLevel = true;
    public boolean multiplayer_DisableVSGarbage = false;
    public float multiplayer_GarbageMultiplier = 1.0f;
    public int multiplayer_GarbageLimit = 0; // 0 = no limit
    public boolean multiplayer_GarbageScaleByDifficulty = false;

    // 0 = All, 1 = All 50%, 2 = Random, 3 = Rotate, 4 = Least Blocks
    public int multiplayer_SendGarbageTo = 0;

    // Single Player / General Options that override GameType if set
    public boolean endlessMode = false;

    public float gameSpeedStart = 0.0f;
    public float gameSpeedChangeRate = 0.02f;
    public float gameSpeedMaximum = 1.0f;

    public float levelUpMultiplier = 1.0f;
    public float levelUpCompoundMultiplier = 0.0f;

    public int floorSpinLimit = -1; // -1 = no limit
    public int totalYLockDelayLimit = -1;
    public float lockDelayDecreaseRate = 0.0f;
    public int lockDelayMinimum = 500;

    public int stackWaitLimit = -1;
    public int spawnDelayLimit = -1;
    public float spawnDelayDecreaseRate = 0.0f;
    public int spawnDelayMinimum = 500;
    public int dropDelayMinimum = 500;

    public Room() {
        gameSequence = new GameSequence();
        // ensure at least one game type
        if(gameSequence.gameTypes.size() == 0) {
            gameSequence.gameTypes.add(new GameType());
        }
    }

    public boolean isDefaultSettings() {
        // TODO: Implement check
        return true;
    }

    public void setDefaults() {
        gameSpeedStart = 0.0f;
        gameSpeedChangeRate = 0.02f;
        gameSpeedMaximum = 1.0f;
        levelUpMultiplier = 1.0f;
        levelUpCompoundMultiplier = 0.0f;
        floorSpinLimit = -1;
        totalYLockDelayLimit = -1;
        lockDelayDecreaseRate = 0.0f;
        lockDelayMinimum = 500;
        stackWaitLimit = -1;
        spawnDelayLimit = -1;
        spawnDelayDecreaseRate = 0.0f;
        spawnDelayMinimum = 500;
        dropDelayMinimum = 500;

        multiplayer_GameEndsWhenOnePlayerRemains = true;
        multiplayer_GameEndsWhenSomeoneCompletesCreditsLevel = true;
        multiplayer_DisableVSGarbage = false;
        multiplayer_AllowNewPlayersDuringGame = true;
        multiplayer_UseTeams = false;
        multiplayer_GarbageScaleByDifficulty = false;
        multiplayer_SendGarbageTo = 0;
    }
}
