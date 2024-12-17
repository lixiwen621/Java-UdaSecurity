package com.udacity.security.service;


import com.google.inject.Inject;
import com.udacity.constant.enums.AlarmStatus;
import com.udacity.constant.enums.ArmingStatus;
import com.udacity.image.service.ImageService;
import com.udacity.security.data.SecurityRepository;
import com.udacity.security.model.Sensor;


import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service that receives information about changes to the security system. Responsible for
 * forwarding updates to the repository and making any decisions about changing the system state.
 * This is the class that should contain most of the business logic for our system, and it is the
 * class you will be writing unit tests for.
 * 接收有关安全系统更改的信息的服务。负责将更新转发到存储库并做出有关更改系统状态的任何决定。
 * 这个类应该包含我们系统的大部分业务逻辑，它是您将为其编写单元测试的课程。
 */
public class SecurityService {

    private final ImageService imageService;
    // 数据存储层，负责持久化存储和读取系统状态（如警报状态、布防状态、传感器信息等）
    private final SecurityRepository securityRepository;
    // 状态监听器的集合，用于在警报状态或猫检测结果改变时通知外部组件
    private final Set<StatusListener> statusListeners;
    // Is a cat detected
    private boolean catDetected=false;

    @Inject
    public SecurityService(SecurityRepository securityRepository, ImageService imageService, Set<StatusListener> statusListeners) {
        this.securityRepository = Objects.requireNonNull(securityRepository,"SecurityRepository must not be null");
        this.imageService = Objects.requireNonNull(imageService,"ImageService must not be null");
        this.statusListeners = Objects.requireNonNull(statusListeners,"Set<StatusListener> must not be null");;
    }

    /**
     * Sets the current arming status for the system. Changing the arming status
     * 设置系统当前布防状态。更改布防状态
     * may update both the alarm status.
     * 可更新两者的警报状态。
     * @param armingStatus
     */
    public void setArmingStatus(ArmingStatus armingStatus) {
        // 如果状态变为撤防（DISARMED），直接将警报状态设为无警报（NO_ALARM）
        if(armingStatus == ArmingStatus.DISARMED) {
            setAlarmStatus(AlarmStatus.NO_ALARM); // 撤防时取消警报
            setCatDetected(false);
        }else if (armingStatus == ArmingStatus.ARMED_HOME){
            if (isCatDetected()){
                // 如果图片有猫, 直接警报
                // If there is a cat in the picture, AlarmStatus.ALARM
                setAlarmStatus(AlarmStatus.ALARM);
            }
        }
        if (getArmingStatus() == ArmingStatus.DISARMED){
            //将所有的Sensor传感器设置为 未激活状态(Inactive)
            // 并通知监听者, 传感器的状态已经更改
            // Set all Sensor sensors to Inactive
            // And notify the StatusListener that the status of the sensor has changed
            resetSensorToInactive();
        }
        securityRepository.setArmingStatus(armingStatus);
    }

    /**
     * Internal method that handles alarm status changes based on whether
     *  根据是否处理报警状态变化的内部方法
     * the camera currently shows a cat.
     *  相机当前显示一只猫
     * @param cat True if a cat is detected, otherwise false.
     *             如果检测到猫则为 true，否则为 false。
     */
    private void catDetected(Boolean cat) {
        Objects.requireNonNull(cat, "cat must not be null");
        // 布防状态下检测到猫，触发警报
        if(cat && getArmingStatus() == ArmingStatus.ARMED_HOME) {
            setAlarmStatus(AlarmStatus.ALARM);
        } else if (!cat && isEverySensorInactive()) {
            // 无猫 且 没有传感器激活
            // No cats and no all sensors activated
            setAlarmStatus(AlarmStatus.NO_ALARM);
        }
        // 通知监听器猫检测结果
        statusListeners.forEach(sl -> sl.catDetected(cat));
    }

    /**
     * Register the StatusListener for alarm system updates from within the SecurityService.
     * 在 SecurityService 中注册 StatusListener 以获取警报系统更新。
     * @param statusListener
     */
    public void addStatusListener(StatusListener statusListener) {
        Objects.requireNonNull(statusListener, "statusListener must not be null");
        // 添加监听器
        statusListeners.add(statusListener);
    }

    // 添加 getter 方法
    public Set<StatusListener> getStatusListeners() {
        return Collections.unmodifiableSet(statusListeners);
    }

    /**
     * Change the alarm status of the system and notify DisplayPanel listeners.
     * 改变系统的报警状态并通知DisplayPanel 的监听者。
     * @param status
     */
    public void setAlarmStatus(AlarmStatus status) {
        // 更新存储库中的警报状态
        securityRepository.setAlarmStatus(status);
        // notify listeners
        statusListeners.forEach(sl -> sl.notify(status));
    }

    /**
     * Internal method for updating the alarm status when a sensor has been activated.
     *  当传感器被激活时更新警报状态的内部方法。
     */
    private void handleSensorActivated() {
        if(securityRepository.getArmingStatus() == ArmingStatus.DISARMED) {
            return; //no problem if the system is disarmed
        }
        switch(securityRepository.getAlarmStatus()) {
            case NO_ALARM -> setAlarmStatus(AlarmStatus.PENDING_ALARM);
            case PENDING_ALARM -> setAlarmStatus(AlarmStatus.ALARM);
            default -> throw new IllegalStateException("Unexpected alarm status: " + securityRepository.getAlarmStatus());
        }
    }

    /**
     * Internal method for updating the alarm status when a sensor has been deactivated
     * 当传感器停用时更新警报状态的内部方法
     */
    private void handleSensorDeactivated() {
        if (securityRepository.getAlarmStatus() == AlarmStatus.PENDING_ALARM) {
            setAlarmStatus(AlarmStatus.NO_ALARM);
        }
    }

    /**
     * Change the activation status for the specified sensor and update alarm status if necessary.
     * 更改指定传感器的激活状态，并在必要时更新警报状态。
     * @param sensor
     * @param active
     */
    public void changeSensorActivationStatus(Sensor sensor, Boolean active) {
        // 如果当前状态是 ALARM，传感器状态仍需更新，但警报状态不改变
        // If the current status is ALARM, the sensor status still needs to be updated,
        // but the alarm status does not change
        if (securityRepository.getAlarmStatus() == AlarmStatus.ALARM) {
            sensor.setActive(active);
            securityRepository.updateSensor(sensor);
            return;
        }
        // 判断传感器状态的变化情况
        // Determine changes in sensor status
        if(!sensor.getActive() && active) {
            // 如果传感器从未激活变为激活，调用 handleSensorActivated() 更新警报状态
            // Sensor changes from inactive to active
            handleSensorActivated();
        } else if (sensor.getActive() && active) {
            // 如果同一个传感器 再次激活
            // If the same sensor is activated again
            // AlarmStatus is PENDING_ALARM
            if (securityRepository.getAlarmStatus() == AlarmStatus.PENDING_ALARM){
                handleSensorActivated();
            }
        } else if (sensor.getActive() && !active) {
            // 传感器从激活变为未激活
            // Sensor changes from active to inactive
            // All sensors are inactive execute handleSensorDeactivated
            if (isEveryOtherSensorInactive(sensor)){
                handleSensorDeactivated();
            }
        } else {
            // 其他情况不处理
            // Other situations will not be processed
            return;
        }
        sensor.setActive(active);
        securityRepository.updateSensor(sensor);
    }

    /**
     * Send an image to the SecurityService for processing. The securityService will use its provided
     *  将图像发送到SecurityService进行处理。 securityService 将使用其提供的
     * ImageService to analyze the image for cats and update the alarm status accordingly.
     *  ImageService 分析猫的图像并相应更新警报状态
     * @param currentCameraImage
     */
    public void processImage(BufferedImage currentCameraImage) {
        catDetected(imageService.imageContainsCat(currentCameraImage, 70.0f));
    }

    public AlarmStatus getAlarmStatus() {
        return securityRepository.getAlarmStatus();
    }

    public ArmingStatus getArmingStatus() {
        return securityRepository.getArmingStatus();
    }

    public Set<Sensor> getSensors() {
        return securityRepository.getSensors();
    }

    public void addSensor(Sensor sensor) {
        securityRepository.addSensor(sensor);
    }

    public void removeSensor(Sensor sensor) {
        securityRepository.removeSensor(sensor);
    }

    /**
     *  所有传感器都 未激活
     *  All sensors are inactive
     *  @return All sensors are inactive return true
     */
    private boolean isEverySensorInactive(){
        return getSensors().stream().noneMatch(Sensor::getActive);
    }

    /**
     * 先过滤掉传入的 sensor, 然后剩下的传感器 都未激活 返回true
     * Filter parameter sensors first, then return true if the remaining sensors are inactive
     * @param sensor
     * @return
     */
    private boolean isEveryOtherSensorInactive(Sensor sensor){
         return getSensors().stream()
                 .filter(s -> !s.getSensorId().equals(sensor.getSensorId()))
                 .noneMatch(Sensor::getActive);
    }

    /**
     * All sensors are set to inactive
     */
    public void resetSensorToInactive(){
        Set<Sensor> sensorSet = getSensors().stream()
                .peek(sensor -> sensor.setActive(false)).collect(Collectors.toSet());
        securityRepository.reloadSensorsAll(sensorSet);

        // 通知监听者, 传感器的状态已经更改
        statusListeners.forEach(StatusListener::sensorStatusChanged);
    }

    public boolean isCatDetected() {
        return catDetected;
    }

    public void setCatDetected(boolean catDetected) {
        this.catDetected = catDetected;
    }
}
