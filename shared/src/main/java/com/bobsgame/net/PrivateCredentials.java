package com.bobsgame.net;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PrivateCredentials {
    public static String passwordSalt = "ok";

    public static String AMAZON_RDS_URL = "jdbc:mysql://localhost/bobsgame";
    public static String AMAZON_RDS_USERNAME = "root";
    public static String AMAZON_RDS_PASSWORD = "";

    public static String DREAMHOST_SQL_URL = "jdbc:mysql://localhost/bobsgame";
    public static String DREAMHOST_SQL_USERNAME = "root";
    public static String DREAMHOST_SQL_PASSWORD = "";
    public static String facebookAppID = "";
    public static String facebookAppSecret = "";
    public static String emailHost = "";
    public static String emailUsername = "";
    public static String emailPassword = "";
    public static String emailPort = "";

    static {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream("server.properties")) {
            props.load(in);
        } catch (IOException e) {
            // System.out.println("Could not load server.properties, using defaults/env.");
        }

        passwordSalt = get("PASSWORD_SALT", props, passwordSalt);
        AMAZON_RDS_URL = get("AMAZON_RDS_URL", props, AMAZON_RDS_URL);
        AMAZON_RDS_USERNAME = get("AMAZON_RDS_USERNAME", props, AMAZON_RDS_USERNAME);
        AMAZON_RDS_PASSWORD = get("AMAZON_RDS_PASSWORD", props, AMAZON_RDS_PASSWORD);
        DREAMHOST_SQL_URL = get("DREAMHOST_SQL_URL", props, DREAMHOST_SQL_URL);
        DREAMHOST_SQL_USERNAME = get("DREAMHOST_SQL_USERNAME", props, DREAMHOST_SQL_USERNAME);
        DREAMHOST_SQL_PASSWORD = get("DREAMHOST_SQL_PASSWORD", props, DREAMHOST_SQL_PASSWORD);
        facebookAppID = get("FACEBOOK_APP_ID", props, facebookAppID);
        facebookAppSecret = get("FACEBOOK_APP_SECRET", props, facebookAppSecret);
        emailHost = get("EMAIL_HOST", props, emailHost);
        emailUsername = get("EMAIL_USERNAME", props, emailUsername);
        emailPassword = get("EMAIL_PASSWORD", props, emailPassword);
        emailPort = get("EMAIL_PORT", props, emailPort);
    }

    private static String get(String key, Properties props, String defaultValue) {
        String env = System.getenv(key);
        if (env != null) return env;
        return props.getProperty(key, defaultValue);
    }
}
