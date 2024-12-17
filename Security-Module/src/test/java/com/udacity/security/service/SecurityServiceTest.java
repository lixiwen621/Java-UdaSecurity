package com.udacity.security.service;

import com.udacity.constant.enums.AlarmStatus;
import com.udacity.constant.enums.ArmingStatus;
import com.udacity.constant.enums.SensorType;
import com.udacity.image.service.ImageService;
import com.udacity.security.data.SecurityRepository;
import com.udacity.security.model.Sensor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {
    @Mock
    private ImageService imageService;
    @Mock
    private SecurityRepository securityRepository;
    @Mock
    private Sensor sensor;
    private Set<StatusListener> statusListeners;
    private SecurityService securityService;

    @BeforeEach
    void setUp(){
        statusListeners = new HashSet<>();
        securityService = new SecurityService(securityRepository,imageService,statusListeners);
        //securityService.addStatusListener(statusListener); // 添加监听器
    }

    @ParameterizedTest
    @EnumSource(ArmingStatus.class)
    void setAlarmStatus_givenStatus_updatesArmingStatusInRepository(ArmingStatus armingStatus) {
        securityService.setCatDetected(true);
        securityService.setArmingStatus(armingStatus);
        verify(securityRepository).setArmingStatus(armingStatus);
        if (armingStatus == ArmingStatus.DISARMED) {
            verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
        } else if (armingStatus == ArmingStatus.ARMED_HOME){
            verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
        }
    }

    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_AWAY", "ARMED_HOME"})
    void setArmingStatus_givenStatus_resetsSensorsAndNotifySensorStatusChangedIfPreviouslyDisarmed(ArmingStatus armingStatus) {
        Sensor sensorSet1 = createSensor("sensorSet-1", SensorType.WINDOW);
        Sensor sensorSet2 = createSensor("sensorSet-2", SensorType.WINDOW);
        sensorSet1.setActive(true);
        sensorSet2.setActive(true);
        Set<Sensor> sensorSet = new TreeSet<>();
        sensorSet.add(sensorSet1);
        sensorSet.add(sensorSet2);

        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);
        when(securityRepository.getSensors()).thenReturn(sensorSet);
        securityService.setArmingStatus(armingStatus);
        verify(securityRepository).reloadSensorsAll(sensorSet);
        verify(securityRepository).setArmingStatus(armingStatus);
    }


    @Test
    void addStatusListener_addsListenerToSet(){
        StatusListener mockListener = Mockito.mock(StatusListener.class);
        securityService.addStatusListener(mockListener);
        Set<StatusListener> listeners = securityService.getStatusListeners();

        assertThat(listeners).contains(mockListener);
    }

    @ParameterizedTest
    @EnumSource(AlarmStatus.class)
    void setAlarmStatus_givenStatus_updatesAlarmStatusAndNotifyInRepository(AlarmStatus alarmStatus){
        securityService.setAlarmStatus(alarmStatus);
        verify(securityRepository).setAlarmStatus(alarmStatus);
    }

    @Test
    void changeSensorActivationStatus_whenAlarmStatusIsAlarm_shouldNoChangeAlarmOnlyUpdateSensor() {
        // 模拟当前状态是 ALARM
        // The current state of the mock is ALARM
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        securityService.changeSensorActivationStatus(sensor, true);
        // 验证传感器状态被更新
        verify(sensor).setActive(true);
        verify(securityRepository).updateSensor(sensor);
        // 验证其他方法未被调用
        verify(securityRepository, never()).setAlarmStatus(any());
    }

    @ParameterizedTest
    @EnumSource(value = AlarmStatus.class, names = {"NO_ALARM", "PENDING_ALARM"})
    void changeSensorActivationStatus_whenSensorBecomesActiveAndGivenNoAlarm_shouldHandleSensorActivatedAndChangeAlarmStatus(AlarmStatus alarmStatus){
        // 模拟当前状态不是 ALARM
        // The current state of the mock is not ALARM
        when(securityRepository.getAlarmStatus()).thenReturn(alarmStatus);
        when(sensor.getActive()).thenReturn(false);

        securityService.changeSensorActivationStatus(sensor, true);

        verify(sensor).setActive(true);
        verify(securityRepository).updateSensor(sensor);
        // 验证 至少调用一次
        verify(securityRepository, atLeastOnce()).setAlarmStatus(any());
    }


    @ParameterizedTest
    @EnumSource(value = AlarmStatus.class, names = {"NO_ALARM", "PENDING_ALARM"})
    void changeSensorActivationStatus_whenSensorBecomesActiveAndWithArmingStatusDisarmed_shouldReturnImmediately(AlarmStatus alarmStatus){
        // 模拟当前  ArmingStatus为 Disarmed
        // Mock the current ArmingStatus to Disarmed
        when(securityRepository.getAlarmStatus()).thenReturn(alarmStatus);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);
        when(sensor.getActive()).thenReturn(false);
        securityService.changeSensorActivationStatus(sensor, true);

        verify(sensor).setActive(true);
        verify(securityRepository).updateSensor(sensor);
    }

    @Test
    void changeSensorActivationStatus_whenSensorIsReactivatedInPendingAlarm_shouldHandleSensorActivatedAndChangeAlarmStatus() {
        // 模拟当前状态为 PENDING_ALARM
        // Mock the current AlarmStatus to PENDING_ALARM
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        when(sensor.getActive()).thenReturn(true);

        // 执行被测方法
        securityService.changeSensorActivationStatus(sensor, true);

        verify(sensor).setActive(true);
        verify(securityRepository).updateSensor(sensor);
        // 验证 至少调用一次
        verify(securityRepository, atLeastOnce()).setAlarmStatus(any());
    }

    @Test
    void changeSensorActivationStatus_whenSensorBecomesInactiveAndAlarmStatusIsPendingAndRemainingSensorsInactive_shouldAlarmStatusIsNoAlarm() {
        // 模拟传感器从激活变为未激活，且其他传感器都未激活
        Sensor sensor = createSensor("sensor1", SensorType.WINDOW);
        sensor.setActive(true);
        Sensor sensorSet1 = createSensor("sensorSet-1", SensorType.WINDOW);
        Sensor sensorSet2 = createSensor("sensorSet-2", SensorType.WINDOW);

        Set<Sensor> sensorSet = new HashSet<>();
        sensorSet.add(sensorSet1);
        sensorSet.add(sensorSet2);
        when(securityRepository.getSensors()).thenReturn(sensorSet);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        // 执行被测方法
        securityService.changeSensorActivationStatus(sensor, false);

        verify(securityRepository).updateSensor(sensor);
        verify(securityRepository, atLeastOnce()).setAlarmStatus(any());
    }

    private Sensor createSensor(String name, SensorType type) {
        return new Sensor.Builder().setName(name).setSensorType(type).build();
    }

    @Test
    void changeSensorActivationStatus_whenNoStateChange_shouldNotProcessFurther() {
        // 模拟传感器状态未改变
        when(sensor.getActive()).thenReturn(false);

        // 执行被测方法
        securityService.changeSensorActivationStatus(sensor, false);

        // 验证状态未更新，也未调用其他方法
        verify(sensor, never()).setActive(anyBoolean());
        verify(securityRepository, never()).updateSensor(sensor);
        verify(securityRepository, never()).setAlarmStatus(any());
    }

    @Test
    void processImage_whenCatDetectedAndArmedHome_shouldTriggerAlarm() {
        // 模拟检测到猫
        BufferedImage mockImage = Mockito.mock(BufferedImage.class);
        when(imageService.imageContainsCat(mockImage, 70.0f)).thenReturn(true);
        // 模拟布防状态为 ARMED_HOME
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        // 执行方法
        securityService.processImage(mockImage);

        // 验证警报状态被设置为 ALARM
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
        for (StatusListener listener : statusListeners){
            verify(listener).notify(AlarmStatus.ALARM);
            verify(listener).catDetected(true);
        }
    }

    @Test
    void processImage_whenNoCatAndSensorsInactive_shouldCancelAlarm() {
        // 模拟未检测到猫
        BufferedImage mockImage = Mockito.mock(BufferedImage.class);
        when(imageService.imageContainsCat(mockImage, 70.0f)).thenReturn(false);
        Sensor sensorSet1 = createSensor("sensorSet-1", SensorType.WINDOW);
        Sensor sensorSet2 = createSensor("sensorSet-2", SensorType.WINDOW);
        Set<Sensor> sensorSet = new HashSet<>();
        sensorSet.add(sensorSet1);
        sensorSet.add(sensorSet2);
        // 模拟传感器均未激活
        when(securityRepository.getSensors()).thenReturn(sensorSet);

        // 执行方法
        securityService.processImage(mockImage);

        // 验证警报状态被设置为 NO_ALARM
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);

        // 验证监听器接收了检测结果
        for (StatusListener listener : statusListeners){
            verify(listener).notify(AlarmStatus.NO_ALARM);
            verify(listener).catDetected(false);
        }
    }

    @Test
    void processImage_whenNoCatAndSensorsActive_shouldNotChangeAlarmStatus() {
        // 模拟未检测到猫
        BufferedImage mockImage = Mockito.mock(BufferedImage.class);
        when(imageService.imageContainsCat(mockImage, 70.0f)).thenReturn(false);
        // 模拟有传感器激活
        Sensor sensorSet1 = createSensor("sensorSet-1", SensorType.WINDOW);
        Sensor sensorSet2 = createSensor("sensorSet-2", SensorType.WINDOW);
        sensorSet2.setActive(true);
        Set<Sensor> sensorSet = new HashSet<>();
        sensorSet.add(sensorSet1);
        sensorSet.add(sensorSet2);
        // 模拟传感器均未激活
        when(securityRepository.getSensors()).thenReturn(sensorSet);

        // 执行方法
        securityService.processImage(mockImage);

        // 验证警报状态没有改变
        verify(securityRepository, never()).setAlarmStatus(any());

        // 验证监听器接收了检测结果
        for (StatusListener listener : statusListeners){
            verify(listener).catDetected(false);
        }
    }

    @Test
    void processImage_whenCatDetectedAndNotArmedHome_shouldNotTriggerAlarm() {
        // 模拟检测到猫
        BufferedImage mockImage = Mockito.mock(BufferedImage.class);
        when(imageService.imageContainsCat(mockImage, 70.0f)).thenReturn(true);
        // 模拟布防状态为 DISARMED
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);

        // 执行方法
        securityService.processImage(mockImage);

        // 验证警报状态没有改变
        verify(securityRepository, never()).setAlarmStatus(any());

        // 验证监听器接收了检测结果
        for (StatusListener listener : statusListeners){
            verify(listener).catDetected(false);
        }
    }

    @ParameterizedTest
    @EnumSource(AlarmStatus.class)
    void getAlarmStatus_shouldReturnAlarmStatus(AlarmStatus alarmStatus){
        when(securityRepository.getAlarmStatus()).thenReturn(alarmStatus);
        assertThat(securityService.getAlarmStatus()).isEqualTo(alarmStatus);
    }

    @Test
    void addSensor_givenSensor_shouldAddSensor(){
        Sensor sensorSet1 = createSensor("sensorSet-1", SensorType.WINDOW);
        securityService.addSensor(sensorSet1);
        verify(securityRepository).addSensor(sensorSet1);
    }

    @Test
    void removeSensor_givenSensor_shouldRemoveSensor(){
        Sensor sensorSet1 = createSensor("sensorSet-1", SensorType.WINDOW);
        securityService.removeSensor(sensorSet1);
        verify(securityRepository).removeSensor(sensorSet1);
    }
}
