package com.udacity.security.data;


import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.prefs.Preferences;

/**
 *  通过Preferences来存储和管理数据
 *  Store and manage data through Preferences
 */
public class PreferencesStorage implements Storage{
    private final Logger log = LoggerFactory.getLogger(PreferencesStorage.class);
    private final Preferences prefs;
    private final Gson gson;

    @Inject
    public PreferencesStorage(Preferences prefs, Gson gson){
        this.prefs = Objects.requireNonNull(prefs,"Preferences must not be null");
        this.gson = Objects.requireNonNull(gson,"Gson must not be null");
    }

    @Override
    public <T> void saveToJSON(String key, T value) {
        try {
            prefs.put(key, gson.toJson(value));
        } catch (Exception e) {
            throw new RuntimeException("Failed to save data as JSON. Key: " + key, e);
        }
    }

    @Override
    public void put(String key, String value) {
        try {
            prefs.put(key, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to store key-value pair. Key: " + key, e);
        }
    }

    @Override
    public <T> T load(String key, Type typeOfT, T defaultValueObject) {
        String json = prefs.get(key, null);
        if (json == null) {
            return defaultValueObject;
        }
        try {
            return gson.fromJson(json, typeOfT);
        } catch (JsonSyntaxException e) {
            log.error("JSON syntax error for key: {}. Returning default value.", key, e);
        } catch (Exception e) {
            log.error("Failed to parse JSON for key: {}. Returning default value.", key, e);
        }
        return defaultValueObject;
    }

    @Override
    public String get(String key, String defaultValue) {
        try {
            return prefs.get(key, defaultValue);
        } catch (Exception e) {
            log.error("Failed to get value for key: {}. Returning default value.", key);
            return defaultValue;
        }
    }

}
