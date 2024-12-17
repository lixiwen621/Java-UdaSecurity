package com.udacity.security.data;

import com.udacity.constant.enums.AlarmStatus;
import com.udacity.constant.enums.ArmingStatus;
import com.udacity.security.model.Sensor;

import java.util.Set;

/**
 * Interface showing the methods our security repository will need to support
 * 显示我们的安全存储库需要支持的方法的界面
 */
public interface SecurityRepository {
    // 添加传感器
    void addSensor(Sensor sensor);
    // 移除指定的传感器
    void removeSensor(Sensor sensor);
    // 更新传感器的状态或配置
    void updateSensor(Sensor sensor);
    // 先清空整个Sensor集合 再添加 sensorSet集合
    void reloadSensorsAll(Set<Sensor> sensorSet);
    // 设置当前系统的警报状态（如无警报、待警报、已警报）
    void setAlarmStatus(AlarmStatus alarmStatus);
    // 设置系统的布防模式（如未布防、在家布防、外出布防）
    void setArmingStatus(ArmingStatus armingStatus);
    // 获取系统中所有已注册的传感器
    Set<Sensor> getSensors();
    // 获取当前的警报状态
    AlarmStatus getAlarmStatus();
    // 获取当前系统的布防状态
    ArmingStatus getArmingStatus();


}
