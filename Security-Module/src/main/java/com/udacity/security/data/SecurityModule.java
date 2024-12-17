package com.udacity.security.data;


import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.udacity.constant.enums.AlarmStatus;
import com.udacity.constant.enums.ArmingStatus;
import com.udacity.security.model.Sensor;
import com.udacity.security.service.SecurityService;
import com.udacity.security.service.StatusListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.prefs.Preferences;

import static com.udacity.constant.common.Constants.*;


/**
 * Guice dependency injection module
 *
 * 主要用于实现  Preferences -> Preferences.userNodeForPackage
 *             Gson ->  new Gson()
*              SecurityRepository -> PretendDatabaseSecurityRepositoryImpl
*              Storage -> PreferencesStorage
 *
 */
public class SecurityModule extends AbstractModule {
    private final Logger log = LoggerFactory.getLogger(SecurityModule.class);
    @Override
    protected void configure() {
        // 初始化 PreferencesStorage类中的 依赖
        // Bind singleton dependencies
        // Initialize dependencies in the PreferencesStorage class
        bind(Preferences.class).toInstance(Preferences.userNodeForPackage(PretendDatabaseSecurityRepositoryImpl.class));
        bind(Gson.class).toInstance(gsonBuilder());

        bind(SecurityRepository.class).to(PretendDatabaseSecurityRepositoryImpl.class).in(Singleton.class);
        bind(SecurityService.class).in(Singleton.class);

    }

    private Gson gsonBuilder(){
        return new GsonBuilder()
                .registerTypeAdapter(Sensor.class, new SensorInstanceCreator()) // 注册自定义的 InstanceCreator
                .create();
    }

    // 绑定 Storage 接口的实现
    // Bind the implementation of the Storage interface
    @Provides
    @Singleton
    Storage provideStorage(Preferences prefs, Gson gson) {
        return new PreferencesStorage(prefs,gson);
    }

    // 初始化 PretendDatabaseSecurityRepositoryImpl 类的依赖
    // 初始化 Set<Sensor> 数据
    // Initialize the dependencies of the PretendDatabaseSecurityRepositoryImpl class
    // Initialize Set<Sensor> data
    @Provides
    @Singleton
    Set<Sensor> sensors(Storage storage) {
        // TypeToken是为了解决 gson.fromJson(sensorString, type) 转换为Set<Sensor> 的类型擦除
        Type type = new TypeToken<Set<Sensor>>() {
        }.getType();
        return storage.load(SENSORS, type, new TreeSet<>());
    }


    // 初始化 alarmStatus 数据
    // Initialize alarmStatus data
    @Provides
    @Singleton
    AlarmStatus alarmStatus(Storage storage) {
        return AlarmStatus.valueOf(storage.get(ALARM_STATUS, AlarmStatus.NO_ALARM.toString()));
    }

    // 初始化 armingStatus 数据
    // Initialize armingStatus data
    @Provides
    @Singleton
    ArmingStatus armingStatus(Storage storage) {
        return ArmingStatus.valueOf(storage.get(ARMING_STATUS, ArmingStatus.DISARMED.toString()));
    }

    // 初始化 SecurityService 类中的 Set<StatusListener>
    // Initialize Set<StatusListener> in SecurityService class
    @Provides
    @Singleton
    Set<StatusListener> statusListeners() {
        return new HashSet<>();
    }
}
