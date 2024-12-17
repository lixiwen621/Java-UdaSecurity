package com.udacity.security.service;

import com.udacity.constant.enums.AlarmStatus;
import com.udacity.constant.enums.ArmingStatus;
import com.udacity.constant.enums.SensorType;
import com.udacity.image.service.FakeImageService;
import com.udacity.image.service.ImageService;
import com.udacity.security.data.FakeSecurityRepository;
import com.udacity.security.model.Sensor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;


public class SecurityServiceIntegrationTest {
    private SecurityService securityService;
    private FakeSecurityRepository fakeRepository;
    private ImageService fakeImageService; // 使用 mock 的 ImageService
    private Set<StatusListener> statusListeners;

    @BeforeEach
    void setUp() {
        // 初始化 FakeSecurityRepository
        fakeRepository = new FakeSecurityRepository();

        // 初始化 Fake ImageService
        fakeImageService = new FakeImageService();

        // 初始化 StatusListeners 集合
        statusListeners = new HashSet<>();

        // 初始化 SecurityService
        securityService = new SecurityService(fakeRepository, fakeImageService, statusListeners);
    }

    private Sensor createSensor(String name, SensorType type) {
        return new Sensor.Builder().setName(name).setSensorType(type).build();
    }

    @ParameterizedTest
    @EnumSource(ArmingStatus.class)
    void setAlarmStatus_givenStatus_updatesArmingStatusInRepository(ArmingStatus armingStatus) {
        securityService.setCatDetected(true);
        securityService.setArmingStatus(armingStatus);
        if (armingStatus == ArmingStatus.DISARMED){
            assertThat(fakeRepository.getAlarmStatus()).isEqualTo(AlarmStatus.NO_ALARM);
        } else if (armingStatus == ArmingStatus.ARMED_HOME) {
            assertThat(fakeRepository.getAlarmStatus()).isEqualTo(AlarmStatus.ALARM);
        }
        assertThat(fakeRepository.getArmingStatus()).isEqualTo(armingStatus);
        statusListeners.forEach(sl -> verify(sl).notify(AlarmStatus.NO_ALARM));
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

        fakeRepository.setSensors(sensorSet);
        securityService.setArmingStatus(armingStatus);
        Set<Sensor> sensors = fakeRepository.getSensors();
        sensors.forEach(sensor -> assertThat(sensor.getActive()).isEqualTo(false));
        statusListeners.forEach(sl -> verify(sl).sensorStatusChanged());
    }

    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_AWAY", "DISARMED"})
    void setArmingStatus_whenAsArmedAwayOrDisarmedInSystemAndCatDetected_GivenChangeArmedHomeShouldAlarm(ArmingStatus armingStatus) {
        Sensor sensorSet1 = createSensor("sensorSet-1", SensorType.WINDOW);
        Sensor sensorSet2 = createSensor("sensorSet-2", SensorType.WINDOW);
        Set<Sensor> sensorSet = new TreeSet<>();
        sensorSet.add(sensorSet1);
        sensorSet.add(sensorSet2);

        fakeRepository.setArmingStatus(armingStatus);
        fakeRepository.setSensors(sensorSet);
        securityService.setCatDetected(true);
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        assertThat(fakeRepository.getAlarmStatus()).isEqualTo(AlarmStatus.ALARM);
    }

    @Test
    void addStatusListener_addsListenerToSet(){
        StatusListener mockListener = mock(StatusListener.class);
        securityService.addStatusListener(mockListener);
        Set<StatusListener> listeners = securityService.getStatusListeners();

        assertThat(listeners).contains(mockListener);
    }

    @ParameterizedTest
    @EnumSource(AlarmStatus.class)
    void setAlarmStatus_givenStatus_updatesAlarmStatusAndNotifyInRepository(AlarmStatus alarmStatus){
        securityService.setAlarmStatus(alarmStatus);
        assertThat(fakeRepository.getAlarmStatus()).isEqualTo(alarmStatus);
        statusListeners.forEach(sl -> verify(sl).notify(alarmStatus));
    }

    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_AWAY", "ARMED_HOME"})
    void changeSensorActivationStatus_whenBothSensorsAreActivatedAsAlarms_shouldSensorActivationChangesDoNotChangeAlarms(ArmingStatus armingStatus) throws NoSuchFieldException, IllegalAccessException {
        Sensor sensorSet1 = createSensor("sensorSet-1", SensorType.WINDOW);
        Sensor sensorSet2 = createSensor("sensorSet-2", SensorType.WINDOW);
        Set<Sensor> sensorSet = new HashSet<>();
        sensorSet.add(sensorSet1);
        sensorSet.add(sensorSet2);

        fakeRepository.setArmingStatus(armingStatus);
        fakeRepository.setSensors(sensorSet);
        fakeRepository.setAlarmStatus(AlarmStatus.NO_ALARM);
        securityService.changeSensorActivationStatus(sensorSet1, true);
        // AlarmStatus.PENDING_ALARM
        assertThat(fakeRepository.getAlarmStatus()).isEqualTo(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensorSet2, true);
        // AlarmStatus.ALARM
        assertThat(fakeRepository.getAlarmStatus()).isEqualTo(AlarmStatus.ALARM);
        securityService.changeSensorActivationStatus(sensorSet2, false);
        securityService.changeSensorActivationStatus(sensorSet1, false);
        // AlarmStatus.ALARM
        assertThat(fakeRepository.getAlarmStatus()).isEqualTo(AlarmStatus.ALARM);
    }

    @ParameterizedTest
    @EnumSource(value = AlarmStatus.class, names = {"NO_ALARM", "PENDING_ALARM"})
    void changeSensorActivationStatus_whenSensorBecomesActiveAndGivenNoAlarm_shouldHandleSensorActivatedAndChangeAlarmStatus(AlarmStatus alarmStatus){
        fakeRepository.setAlarmStatus(alarmStatus);
        fakeRepository.setArmingStatus(ArmingStatus.ARMED_HOME);
        Sensor sensor = createSensor("sensorSet-1", SensorType.WINDOW);
        securityService.changeSensorActivationStatus(sensor, true);
        if (alarmStatus == AlarmStatus.NO_ALARM){
            assertThat(fakeRepository.getAlarmStatus()).isEqualTo(AlarmStatus.PENDING_ALARM);
        }else if (alarmStatus == AlarmStatus.PENDING_ALARM){
            assertThat(fakeRepository.getAlarmStatus()).isEqualTo(AlarmStatus.ALARM);
        }
        fakeRepository.getSensors().forEach(s -> assertThat(s.getActive()).isEqualTo(true));
    }

    @ParameterizedTest
    @EnumSource(value = AlarmStatus.class, names = {"NO_ALARM", "PENDING_ALARM"})
    void changeSensorActivationStatus_whenSensorBecomesActiveAndWithArmingStatusDisarmed_shouldReturnImmediately(AlarmStatus alarmStatus){
        fakeRepository.setAlarmStatus(alarmStatus);
        Sensor sensor = createSensor("sensorSet-1", SensorType.WINDOW);
        securityService.changeSensorActivationStatus(sensor, true);
        fakeRepository.getSensors().forEach(s -> assertThat(s.getActive()).isEqualTo(true));
    }

    @Test
    void changeSensorActivationStatus_whenSensorIsReactivatedInPendingAlarm_shouldHandleSensorActivatedAndChangeAlarmStatus() {
        fakeRepository.setAlarmStatus(AlarmStatus.PENDING_ALARM);
        Sensor sensor = createSensor("sensorSet-1", SensorType.WINDOW);
        sensor.setActive(true);
        securityService.changeSensorActivationStatus(sensor, true);
        fakeRepository.getSensors().forEach(s -> assertThat(s.getActive()).isEqualTo(true));
    }

    @Test
    void changeSensorActivationStatus_whenSensorBecomesInactiveAndAlarmStatusIsPendingAndRemainingSensorsInactive_shouldAlarmStatusIsNoAlarm() {
        Sensor sensor = createSensor("sensor1", SensorType.WINDOW);
        sensor.setActive(true);
        Sensor sensorSet1 = createSensor("sensorSet-1", SensorType.WINDOW);
        Sensor sensorSet2 = createSensor("sensorSet-2", SensorType.WINDOW);

        Set<Sensor> sensorSet = new HashSet<>();
        sensorSet.add(sensorSet1);
        sensorSet.add(sensorSet2);

        fakeRepository.setSensors(sensorSet);
        fakeRepository.setAlarmStatus(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor, false);

        assertThat(fakeRepository.getAlarmStatus()).isEqualTo(AlarmStatus.NO_ALARM);
        fakeRepository.getSensors().forEach(s -> assertThat(s.getActive()).isEqualTo(false));
    }

    @Test
    void processImage_whenRandomCatDetectedAndArmedHomeAndSensorsInactive_shouldChangeAlarm() {
        BufferedImage mockImage = null;
        fakeRepository.setArmingStatus(ArmingStatus.ARMED_HOME);
        Sensor sensorSet1 = createSensor("sensorSet-1", SensorType.WINDOW);
        Sensor sensorSet2 = createSensor("sensorSet-2", SensorType.WINDOW);
        Set<Sensor> sensorSet = new HashSet<>();
        sensorSet.add(sensorSet1);
        sensorSet.add(sensorSet2);
        fakeRepository.setSensors(sensorSet);
        securityService.processImage(mockImage);

        assertThat(fakeRepository.getAlarmStatus()).isAnyOf(AlarmStatus.ALARM,AlarmStatus.NO_ALARM);
        for (StatusListener listener : statusListeners){
            verify(listener).notify(AlarmStatus.ALARM);
            verify(listener).catDetected(true);
        }
    }

    @ParameterizedTest
    @EnumSource(AlarmStatus.class)
    void getAlarmStatus_shouldReturnAlarmStatus(AlarmStatus alarmStatus){
        fakeRepository.setAlarmStatus(alarmStatus);
        assertThat(securityService.getAlarmStatus()).isEqualTo(alarmStatus);
    }

    @Test
    void addSensor_givenSensor_shouldAddSensor(){
        Sensor sensorSet1 = createSensor("sensorSet-1", SensorType.WINDOW);
        securityService.addSensor(sensorSet1);
        assertThat(fakeRepository.getSensors()).contains(sensorSet1);
    }

    @Test
    void removeSensor_givenSensor_shouldRemoveSensor(){
        Sensor sensorSet1 = createSensor("sensorSet-1", SensorType.WINDOW);
        Set<Sensor> sensorSet = new HashSet<>();
        sensorSet.add(sensorSet1);
        fakeRepository.setSensors(sensorSet);
        securityService.removeSensor(sensorSet1);
        assertThat(fakeRepository.getSensors()).isEmpty();
    }

}
