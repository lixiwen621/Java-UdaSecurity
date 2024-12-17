package com.udacity.security.data;

import com.udacity.constant.common.Constants;
import com.udacity.constant.enums.AlarmStatus;
import com.udacity.constant.enums.ArmingStatus;
import com.udacity.constant.enums.SensorType;
import com.udacity.security.model.Sensor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.TreeSet;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

//初始化 @Mock 和 @InjectMocks 注解的字段或者其他测试相关的注解
@ExtendWith(MockitoExtension.class)
class PretendDatabaseSecurityRepositoryImplTest {

    // Mock 通常用于模拟行为，而非数据存储
    @Mock
    private Storage storage;
    // 这些存储相关的可以不用Mock
    private Set<Sensor> sensors;
    private PretendDatabaseSecurityRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        sensors = new TreeSet<>();
        AlarmStatus alarmStatus = AlarmStatus.NO_ALARM;
        ArmingStatus armingStatus = ArmingStatus.DISARMED;

        repository = new PretendDatabaseSecurityRepositoryImpl(sensors, alarmStatus, armingStatus, storage);
    }

    private Sensor createSensor(String name, SensorType type) {
        return new Sensor.Builder().setName(name).setSensorType(type).build();
    }

    @Test
    void addSensor_withSensor_shouldAddSensorAndSaveToStorage() {
        Sensor sensor = createSensor("sensor-1", SensorType.WINDOW);
        repository.addSensor(sensor);
        // contains 集合包含一个 对象
        assertThat(sensors).contains(sensor);
        // Mockito 的 verify 方法，检查在测试过程中，storage 的 saveToJSON 方法是否被调用
        // 第一个参数是否等于 Constants.SENSORS  第二个参数是否等于当前的 sensors 集合
        verify(storage).saveToJSON(eq(Constants.SENSORS), eq(sensors));
    }

    @Test
    void removeSensor_givenSensor_shouldRemoveSensorAndSaveToStorage() {
        Sensor sensor = createSensor("sensor-1", SensorType.WINDOW);
        sensors.add(sensor);

        repository.removeSensor(sensor);
        // doesNotContain 集合没有包含这个对象
        assertThat(sensors).doesNotContain(sensor);
        verify(storage).saveToJSON(eq(Constants.SENSORS), eq(sensors));
    }

    @Test
    void updateSensor_withUpdateSensor_shouldUpdateSensorAndSaveSensorsToPreFs() throws NoSuchFieldException, IllegalAccessException {
        // Arrange
        Sensor existingSensor = createSensor("Old Sensor", SensorType.DOOR);
        sensors.add(existingSensor);

        Sensor updatedSensor = createSensor("Updated Sensor", SensorType.DOOR);
        // 使用反射设置 updatedSensor 的 sensorId 与 existingSensor 一致
        Field sensorIdField = Sensor.class.getDeclaredField("sensorId");
        sensorIdField.setAccessible(true);
        sensorIdField.set(updatedSensor, existingSensor.getSensorId());

        // Act: 调用要测试的方法
        repository.updateSensor(updatedSensor);

        // Assert: 验证行为和状态
        // 验证集合中传感器已经被更新
        //  containsExactly 说明 完全相同且顺序一致
        assertThat(sensors).containsExactly(updatedSensor); // Google Truth 的断言
        // 验证存储操作被调用且参数正确
        verify(storage).saveToJSON(eq(Constants.SENSORS), eq(sensors));
    }

    @Test
    void reloadSensorsAll_withSensorSets_shouldReloadSensorsAllAndSaveSensorsToPreFs(){
        Sensor sensor = createSensor("sensor-1", SensorType.WINDOW);
        sensors.add(sensor);

        Sensor sensor1 = createSensor("sensorSets-1", SensorType.WINDOW);
        Sensor sensor2 = createSensor("sensorSets-2", SensorType.WINDOW);
        Set<Sensor> sensorSets = new TreeSet<>();
        sensorSets.add(sensor1);
        sensorSets.add(sensor2);

        repository.reloadSensorsAll(sensorSets);
        // containsExactlyElementsIn 用来跟集合进行比较是否一致(包括数量、内容和顺序)
        assertThat(sensors).containsExactlyElementsIn(sensorSets);
        verify(storage).saveToJSON(eq(Constants.SENSORS), eq(sensors));
    }

    @ParameterizedTest
    @EnumSource(AlarmStatus.class)
    void setAlarmStatus_givenAllAlarmStatus_shouldUpdateAlarmStatusAndSaveToStorage(AlarmStatus newStatus) {
        repository.setAlarmStatus(newStatus);
        // isEqualTo 对象跟对象之间 判断是否相等
        assertThat(repository.getAlarmStatus()).isEqualTo(newStatus);
        verify(storage).put(eq(Constants.ALARM_STATUS), eq(newStatus.toString()));
    }

    @ParameterizedTest
    @EnumSource(ArmingStatus.class)
    void setArmingStatus_givenAllArmingStatus_shouldUpdateArmingStatusAndSaveToStorage(ArmingStatus armingStatus) {
        repository.setArmingStatus(armingStatus);
        // isEqualTo 对象跟对象之间 判断是否相等
        assertThat(repository.getArmingStatus()).isEqualTo(armingStatus);
        verify(storage).put(eq(Constants.ARMING_STATUS), eq(armingStatus.toString()));
    }

    @Test
    void getSensors_withSensor_shouldReturnUnmodifiableSet(){
        Sensor sensor = createSensor("sensor-1", SensorType.WINDOW);
        sensors.add(sensor);
        Set<Sensor> returnedSensors = repository.getSensors();
        assertThat(repository.getSensors()).containsExactlyElementsIn(sensors);
        // Check immutability 检查不可变性  assertThrows 用于测试代码是否抛出了预期的异常
        assertThrows(UnsupportedOperationException.class,
                () -> returnedSensors.add(new Sensor.Builder().setName("sensor-1").setSensorType(SensorType.WINDOW).build()));

    }


}