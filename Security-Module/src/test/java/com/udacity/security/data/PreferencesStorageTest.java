package com.udacity.security.data;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.udacity.constant.enums.SensorType;
import com.udacity.security.model.Sensor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.prefs.Preferences;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PreferencesStorageTest {
    @Mock
    private Preferences prefs;
    // 默认情况下，Mockito 不能 mock final 类或 final 方法。
    private Gson gson;
    private PreferencesStorage storage;

    @BeforeEach
    void setUp(){
        gson = new Gson();
        storage = new PreferencesStorage(prefs,gson);
    }

    private Sensor createSensor(String name, SensorType type) {
        return new Sensor.Builder().setName(name).setSensorType(type).build();
    }

    @Test
    void saveToJSON_withValidKeyAndSensor_shouldPersistSensorAsJSON() {
        // 准备测试数据
        String key = "testKey";
        Sensor sensor = createSensor("sensor-1", SensorType.WINDOW);
        String gsonJson = gson.toJson(sensor);
        // 调用被测方法
        storage.saveToJSON(key, sensor);
        // 验证交互行为 确保 prefs.put 被调用
        verify(prefs).put(key, gsonJson);
    }

    @Test
    void put_withValidKeyAndSerializedSensor_shouldPutIntoPreferences(){
        // 准备测试数据
        String key = "testKey";
        Sensor sensor = createSensor("sensor-1", SensorType.WINDOW);
        String gsonJson = gson.toJson(sensor);

        storage.put(key,gsonJson);
        // 验证交互行为 确保 prefs.put 被调用
        verify(prefs).put(key, gsonJson);
    }

    @Test
    void load_withValidKeyAndSensor_shouldReturnDeserializedValue(){
        // 准备测试数据
        String key = "testKey";
        Sensor sensor = createSensor("sensor-1", SensorType.WINDOW);
        String gsonJson = gson.toJson(sensor);

        when(prefs.get(key, null)).thenReturn(gsonJson);
        Sensor load = storage.load(key, Sensor.class, null);
        assertThat(load).isEqualTo(sensor);
    }

    @Test
    void load_withValidKeyAndSensor_shouldReturnDefaultValue(){
        // 准备测试数据
        String key = "testKey";
        Sensor sensor = createSensor("sensor-1", SensorType.WINDOW);

        when(prefs.get(key, null)).thenReturn(null);
        Sensor load = storage.load(key, Sensor.class, sensor);
        assertThat(load).isEqualTo(sensor);
    }

//    @Test
//    void load_withValidKeyAndSensor_shouldReturnDefaultValueAndJsonSyntaxException(){
//        // 准备测试数据
//        String key = "testKey";
//        Sensor sensor = createSensor("sensor-1", SensorType.WINDOW);
//        String json= "{\"timeoutSeconds\":5,\"popularWordCount\":5,\"resultPath\":\"crawlResults.json\",}";
//        when(prefs.get(key, null)).thenReturn(json);
//        Sensor load = storage.load(key, Sensor.class, sengggsor);
//        assertThat(load).isEqualTo(sensor);
//    }

//    @Test
//    void load_withValidKeyAndSensor_shouldReturnDefaultValueAndJsonSyntaxException() {
//        // Arrange
//        String key = "testKey";
//        String invalidJson = "invalidJson";
//        String defaultValue = "default";
//        when(prefs.get(key, null)).thenReturn(invalidJson);
//        when(gson.fromJson(invalidJson, String.class)).thenThrow(JsonSyntaxException.class);
//
//        // Act
//        String result = storage.load(key, String.class, defaultValue);
//
//        // Assert
//        verify(prefs).get(key, null);       // 确保 prefs.get 被调用
//        verify(gson).fromJson(invalidJson, String.class); // 确保 gson.fromJson 被调用
//    }

    @Test
    void get_withValidKey_shouldReturnSensorJsonString(){
        String key = "testKey";
        Sensor sensor = createSensor("sensor-1", SensorType.WINDOW);
        String gsonJson = gson.toJson(sensor);
        when(prefs.get(key, null)).thenReturn(gsonJson);
        assertThat(storage.get(key,null)).isEqualTo(gsonJson);
    }
}
