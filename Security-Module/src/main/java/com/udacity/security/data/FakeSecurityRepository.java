package com.udacity.security.data;

import com.udacity.constant.enums.AlarmStatus;
import com.udacity.constant.enums.ArmingStatus;
import com.udacity.security.model.Sensor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class FakeSecurityRepository implements SecurityRepository {
    private Set<Sensor> sensors = new HashSet<>();
    private AlarmStatus alarmStatus = AlarmStatus.NO_ALARM;
    private ArmingStatus armingStatus = ArmingStatus.DISARMED;

    @Override
    public void addSensor(Sensor sensor) {
        Objects.requireNonNull(sensor, "Sensor must not be null");
        sensors.add(sensor);
    }

    @Override
    public void removeSensor(Sensor sensor) {
        Objects.requireNonNull(sensor, "Sensor must not be null");
        sensors.remove(sensor);
    }

    @Override
    public void updateSensor(Sensor sensor) {
        Objects.requireNonNull(sensor, "Sensor must not be null");
        sensors.removeIf(existingSensor -> existingSensor.getSensorId().equals(sensor.getSensorId()));
        sensors.add(sensor);
    }

    @Override
    public void reloadSensorsAll(Set<Sensor> sensorSet) {
        Objects.requireNonNull(sensorSet, "Sensor set must not be null");
        sensors.clear();
        sensors.addAll(sensorSet);
    }

    @Override
    public void setAlarmStatus(AlarmStatus alarmStatus) {
        Objects.requireNonNull(alarmStatus, "AlarmStatus must not be null");
        this.alarmStatus = alarmStatus;
    }

    @Override
    public void setArmingStatus(ArmingStatus armingStatus) {
        Objects.requireNonNull(armingStatus, "ArmingStatus must not be null");
        this.armingStatus = armingStatus;
    }

    @Override
    public Set<Sensor> getSensors() {
        return Collections.unmodifiableSet(sensors);
    }
    // Just for unit testing
    public void setSensors(Set<Sensor> sensors){
        this.sensors = sensors;
    }

    @Override
    public AlarmStatus getAlarmStatus() {
        return alarmStatus;
    }

    @Override
    public ArmingStatus getArmingStatus() {
        return armingStatus;
    }
}