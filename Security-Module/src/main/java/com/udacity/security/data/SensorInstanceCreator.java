package com.udacity.security.data;

import com.google.gson.InstanceCreator;
import com.udacity.constant.enums.SensorType;
import com.udacity.security.model.Sensor;

import java.lang.reflect.Type;

/**
 *  Gson 通过 Unsafe 实例化对象, 在多模块环境下会受到限制,
 */
class SensorInstanceCreator implements InstanceCreator<Sensor> {
    @Override
    public Sensor createInstance(Type type) {
        return new Sensor.Builder().setName("test1").setSensorType(SensorType.DOOR).build();
    }
}