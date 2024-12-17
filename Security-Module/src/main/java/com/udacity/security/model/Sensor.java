package com.udacity.security.model;


import com.google.common.collect.ComparisonChain;
import com.udacity.constant.enums.SensorType;

import java.util.Objects;
import java.util.UUID;

/**
 * Sensor POJO. Needs to know how to sort itself for display purposes.
 */
public class Sensor implements Comparable<Sensor> {
    private final UUID sensorId; // 传感器唯一标识
    private final String name; // 传感器名称
    private Boolean active; // 表示传感器是否处于激活状态
    private final SensorType sensorType; // 传感器类型

    private Sensor(Builder builder) {
        this.name = builder.name;
        this.sensorType = builder.sensorType;
        this.sensorId = builder.sensorId;
        this.active = Boolean.FALSE;
    }

    // equals方法中只根据 sensorId来判断相等
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sensor sensor = (Sensor) o;
        return sensorId.equals(sensor.sensorId);
    }

    // 只根据 sensorId 来计算 hashCode
    @Override
    public int hashCode() {
        return Objects.hash(sensorId);
    }

    public String getName() {
        return name;
    }


    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public SensorType getSensorType() {
        return sensorType;
    }

    public UUID getSensorId() {
        return sensorId;
    }


    @Override
    public int compareTo(Sensor o) {
        // ComparisonChain 是 Guava 提供的一个工具类，简化了多属性的比较逻辑
        return ComparisonChain.start()
                .compare(this.name, o.name)
                .compare(this.sensorType.ordinal(), o.sensorType.ordinal()) // 按枚举的 enum常量的声明顺序
                .compare(this.sensorId, o.sensorId)
                .result();
    }

    public static class Builder{
        private final UUID sensorId; // 传感器唯一标识
        private String name; // 传感器名称
        private SensorType sensorType; // 传感器类型

        public Builder(){
            this.sensorId = UUID.randomUUID();
        }

        public Builder setName(String name){
            this.name = name;
            return this;
        }

        public Builder setSensorType(SensorType sensorType){
            this.sensorType = sensorType;
            return this;
        }

        public Sensor build(){
            if (sensorType == null){
                throw new IllegalStateException("Name and SensorType cannot be null");
            }
            return new Sensor(this);
        }
    }

}
