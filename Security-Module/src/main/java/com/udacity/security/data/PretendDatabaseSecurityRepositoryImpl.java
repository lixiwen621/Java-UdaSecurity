package com.udacity.security.data;

import com.google.inject.Inject;
import com.udacity.constant.common.Constants;
import com.udacity.constant.enums.AlarmStatus;
import com.udacity.constant.enums.ArmingStatus;
import com.udacity.security.model.Sensor;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Fake repository implementation for demo purposes. Stores state information in local
 * memory and writes it to user preferences between app loads. This implementation is
 * intentionally a little hard to use in unit tests, so watch out!
 * 用于演示目的的假存储库实现。将状态信息存储在本地内存并将其写入应用程序加载之间的用户首选项。这个实现是
 * 故意在单元测试中使用起来有点困难，所以要小心！
 *
 */
public class PretendDatabaseSecurityRepositoryImpl implements SecurityRepository{
    private final Set<Sensor> sensors;
    private AlarmStatus alarmStatus;
    private ArmingStatus armingStatus;
    // Storage implemented in PreferencesStorage
    private final Storage storage;

    /**
     * 通过依赖注入的方式来实现
     * Implemented through dependency injection
     * @param sensors
     * @param alarmStatus
     * @param armingStatus
     * @param storage
     */
    @Inject
    public PretendDatabaseSecurityRepositoryImpl(
            Set<Sensor> sensors,
            AlarmStatus alarmStatus,
            ArmingStatus armingStatus,
            Storage storage ) {
        // 初始化数据
        this.sensors = Objects.requireNonNull(sensors, "Sensors must not be null");
        this.alarmStatus = Objects.requireNonNull(alarmStatus, "AlarmStatus must not be null");
        this.armingStatus = Objects.requireNonNull(armingStatus, "ArmingStatus must not be null");
        this.storage = Objects.requireNonNull(storage, "Storage must not be null");
    }
    private void saveSensorsToPreFs(){
        storage.saveToJSON(Constants.SENSORS, sensors);
    }

    private void saveAlarmStatus() {
        storage.put(Constants.ALARM_STATUS, alarmStatus.toString());
    }

    private void saveArmingStatus() {
        storage.put(Constants.ARMING_STATUS, armingStatus.toString());
    }

    @Override
    public void addSensor(Sensor sensor) {
        Objects.requireNonNull(sensor, "sensor must not be null");
        sensors.add(sensor);
        saveSensorsToPreFs();
    }

    @Override
    public void removeSensor(Sensor sensor) {
        Objects.requireNonNull(sensor, "sensor must not be null");
        sensors.remove(sensor);
        saveSensorsToPreFs();
    }

    @Override
    public void updateSensor(Sensor sensor) {
        Objects.requireNonNull(sensor, "sensor must not be null");
        // remove sensorId equals
        // remove sensorId相等的
        sensors.removeIf(existingSensor -> existingSensor.getSensorId().equals(sensor.getSensorId()));
        sensors.add(sensor);
        saveSensorsToPreFs();
    }

    @Override
    public void reloadSensorsAll(Set<Sensor> sensorSet) {
        Objects.requireNonNull(sensorSet, "sensorSet must not be null");
        sensors.clear();
        sensors.addAll(sensorSet);
        saveSensorsToPreFs();
    }

    @Override
    public void setAlarmStatus(AlarmStatus alarmStatus) {
        Objects.requireNonNull(alarmStatus, "alarmStatus must not be null");
        this.alarmStatus = alarmStatus;
        saveAlarmStatus();
    }

    @Override
    public void setArmingStatus(ArmingStatus armingStatus) {
        Objects.requireNonNull(alarmStatus, "alarmStatus must not be null");
        this.armingStatus = armingStatus;
        saveArmingStatus();
    }

    @Override
    public Set<Sensor> getSensors() {
        return Collections.unmodifiableSet(sensors);
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
