package com.bobsgame.client.settings;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GlobalSettings {

    private static GlobalSettings instance;
    private static final String FILENAME = "global_settings.json";

    // Audio
    public int musicVolume = 100;

    // Graphics
    public float brightness = 1.0f;
    public float contrast = 1.0f;
    public float saturation = 1.0f;
    public float gamma = 1.0f;
    public float hue = 0.0f;

    // Game
    public float bobsGame_screenFlashOnLevelUpAlpha = 0.5f;
    public boolean bobsGame_showDetailedGameInfoCaptions = false;
    public boolean bobsGame_showScoreBarsInSinglePlayer = false;

    // Chat & Social
    public boolean censorBadWords = true;
    public boolean hideChat = false;
    public boolean hideNotifications = false;

    // Input
    public boolean useAnalogSticks = false;
    public boolean useXInput = true; // Not strictly used in Java but kept for parity

    public static GlobalSettings getInstance() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    public static GlobalSettings load() {
        File file = new File(FILENAME);
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                Gson gson = new Gson();
                GlobalSettings s = gson.fromJson(br, GlobalSettings.class);
                if(s != null) return s;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new GlobalSettings();
    }

    public void save() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(FILENAME)))) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(this);
            bw.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setDefaults() {
        GlobalSettings defaults = new GlobalSettings();
        this.musicVolume = defaults.musicVolume;
        this.brightness = defaults.brightness;
        this.contrast = defaults.contrast;
        this.saturation = defaults.saturation;
        this.gamma = defaults.gamma;
        this.hue = defaults.hue;
        this.bobsGame_screenFlashOnLevelUpAlpha = defaults.bobsGame_screenFlashOnLevelUpAlpha;
        this.bobsGame_showDetailedGameInfoCaptions = defaults.bobsGame_showDetailedGameInfoCaptions;
        this.bobsGame_showScoreBarsInSinglePlayer = defaults.bobsGame_showScoreBarsInSinglePlayer;
        this.censorBadWords = defaults.censorBadWords;
        this.hideChat = defaults.hideChat;
        this.hideNotifications = defaults.hideNotifications;
    }
}
