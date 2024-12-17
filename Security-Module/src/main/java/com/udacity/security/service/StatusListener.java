package com.udacity.security.service;


import com.udacity.constant.enums.AlarmStatus;

/**
 * Identifies a component that should be notified whenever the system status changes
 * 标识当系统状态发生变化时应该通知的组件
 */
public interface StatusListener {
    /**
     * Notifies the listener that the system status has changed.
     * 通知监听者系统状态发生变化
     *
     * @param alarmStatus
     */
    default void notify(AlarmStatus alarmStatus) {

    }

    /**
     * Notifies the listener that a cat has been detected.
     * 通知系统是否检测到了猫
     *
     * @param catDetected true if a cat has been detected, false otherwise
     *                    boolean catDetected：true 表示检测到猫，false 表示未检测到
     */
    default void catDetected(boolean catDetected) {

    }

    /**
     * Notifies the listener that a sensor status has changed.
     * 通知监听者传感器状态已更改。
     */
    default void sensorStatusChanged() {

    }
}
