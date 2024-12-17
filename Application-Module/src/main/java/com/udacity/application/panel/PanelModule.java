package com.udacity.application.panel;

import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.udacity.constant.common.Constants;
import com.udacity.constant.enums.ArmingStatus;
import com.udacity.constant.enums.SensorType;


import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class PanelModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(DisplayPanel.class).in(Singleton.class);
        bind(ImagePanel.class).in(Singleton.class);
        bind(ControlPanel.class).in(Singleton.class);
        bind(SensorPanel.class).in(Singleton.class);
    }

    // 注入 DisplayPanel类的 组件 开始
    // Inject the component of DisplayPanel class to start
    // DisplayPanel PanelLabel
    @Provides
    @Singleton
    @Named("displayPanelLabel")
    JLabel displayPanelLabel() {
        return new JLabel(Constants.DISPLAY_PANEL_LABEL);
    }

    // DisplayPanel SystemStatusLabel
    @Provides
    @Singleton
    @Named("displaySystemStatusLabel")
    JLabel displaySystemStatusLabel() {
        return new JLabel(Constants.DISPLAY_SYSTEM_STATUS_LABEL);
    }

    // DisplayPanel CurrentStatusLabel
    @Provides
    @Singleton
    @Named("displayCurrentStatusLabel")
    JLabel displayCurrentStatusLabel() {
        return new JLabel(Constants.DISPLAY_SYSTEM_STATUS_LABEL);
    }
    // 注入 DisplayPanel类的 组件 结束
    // End of injecting the component of DisplayPanel class


    // 注入 ImagePanel类的 组件 开始
    // Inject the component of ImagePanel class to start
    // ImagePanel  CameraHeader
    @Provides
    @Singleton
    @Named("imageCameraHeader")
    JLabel imageCameraHeader() {
        return new JLabel(Constants.IMAGE_CAMERA_HEADER);
    }

    // ImagePanel CameraLabel
    @Provides
    @Singleton
    @Named("imageCameraLabel")
    JLabel imageCameraLabel() {
        return new JLabel();
    }

    // // ImagePanel AddPictureButton
    @Provides
    @Singleton
    @Named("imageAddPictureButton")
    JButton imageAddPictureButton() {
        return new JButton(Constants.IMAGE_ADD_PICTURE_BUTTON);
    }

    // ImagePanel ScanPictureButton
    @Provides
    @Singleton
    @Named("imageScanPictureButton")
    JButton imageScanPictureButton() {
        return new JButton(Constants.IMAGE_SCAN_PICTURE_BUTTON);
    }
    // 注入 ImagePanel 类的 组件 结束
    // End of injecting the component of ImagePanel class


    // 注入 ControlPanel 类的 组件 开始
    // Inject the component of ControlPanel class to start
    // ControlPanel ButtonMap
    @Provides
    @Singleton
    @Named("controlButtonMap")
    Map<ArmingStatus, JButton> controlButtonMap() {
        return Arrays.stream(ArmingStatus.values())
                .collect(Collectors.toMap(status -> status, status -> new JButton(status.getDescription())));
    }

    // ControlPanel PanelLabel
    @Provides
    @Singleton
    @Named("controlPanelLabel")
    JLabel controlPanelLabel() {
        return new JLabel(Constants.CONTROL_PANEL_LABEL);
    }
    // 注入 ControlPanel 类的 组件 结束
    // End of injecting the component of ControlPanel class


    // 注入 SensorPanel 类的 组件 开始
    // Inject the component of SensorPanel class to start
    // SensorPanel ComponentMap
    @Provides
    @Singleton
    @Named("sensorComponentMap")
    Map<String, Component> sensorComponentMap() {
        return Maps.newHashMap();
    }

    // SensorPanel PanelLabel
    @Provides
    @Singleton
    @Named("sensorPanelLabel")
    JLabel sensorPanelLabel() {
        return new JLabel(Constants.SENSOR_PANEL_LABEL);
    }

    // SensorPanel newSensorName
    @Provides
    @Singleton
    @Named("newSensorName")
    JLabel newSensorName() {
        return new JLabel(Constants.SENSOR_NEW_SENSOR_NAME);
    }

    // SensorPanel newSensorType
    @Provides
    @Singleton
    @Named("newSensorType")
    JLabel newSensorType() {
        return new JLabel(Constants.SENSOR_NEW_SENSOR_TYPE);
    }

    // SensorPanel newSensorNameField
    @Provides
    @Singleton
    @Named("newSensorNameField")
    JTextField newSensorNameField() {
        return new JTextField();
    }

    // SensorPanel newSensorTypeDropdown
    @Provides
    @Singleton
    @Named("newSensorTypeDropdown")
    JComboBox newSensorTypeDropdown() {
        return new JComboBox(SensorType.values());
    }

    // SensorPanel addNewSensorButton
    @Provides
    @Singleton
    @Named("addNewSensorButton")
    JButton addNewSensorButton() {
        return new JButton(Constants.SENSOR_ADD_NEW_SENSOR_BUTTON);
    }

    @Provides
    @Singleton
    @Named("sensorListPanel")
    JPanel sensorListPanel() {
        return new JPanel();
    }

    @Provides
    @Singleton
    @Named("newSensorPanel")
    JPanel newSensorPanel() {
        return new JPanel();
    }
    // 注入 SensorPanel 类的 组件 结束
    // End of injecting the component of SensorPanel class
}
