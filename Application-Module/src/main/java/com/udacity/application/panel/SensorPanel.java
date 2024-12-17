package com.udacity.application.panel;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import com.udacity.constant.common.Constants;
import com.udacity.constant.enums.SensorType;
import com.udacity.security.model.Sensor;
import com.udacity.security.service.SecurityService;
import com.udacity.security.service.StatusListener;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Panel that allows users to add sensors to their system. Sensors may be
 * manually set to "active" and "inactive" to test the system.
 * 允许用户向其系统添加传感器的面板。传感器可能是
 * 手动设置为“活动”和“非活动”以测试系统。
 * 功能概述
 * SensorPanel(传感器面板) 是一个用于显示和管理传感器的用户界面组件：
 * 	1.	用户可以手动添加传感器，并指定传感器名称和类型。
 * 	2.	展示当前传感器列表，包括名称、类型、状态（激活或未激活）。
 * 	3.	提供按钮切换传感器状态或删除传感器。
 * 	4.	当传感器数量超过 4 时，限制用户操作，并提示需要升级会员。
 */
public class SensorPanel extends JPanel implements StatusListener {
    private final Logger log = LoggerFactory.getLogger(SensorPanel.class);
    private final SecurityService securityService;
    // Map used to store container components
    // 用来存储容器组件的Map
    private final Map<String, Component> componentMap;
    private final JLabel panelLabel;
    private final JLabel newSensorName;
    private final JLabel newSensorType;
    private final JTextField newSensorNameField;
    // 下拉框，用于选择传感器类型
    private final JComboBox newSensorTypeDropdown;
    // 按钮，用于添加新传感器
    private final JButton addNewSensorButton;

    private final JPanel sensorListPanel;
    private final JPanel newSensorPanel;

    @Inject
    SensorPanel(
            SecurityService securityService,
            @Named("sensorComponentMap") Map<String, Component> componentMap,
            @Named("sensorPanelLabel") JLabel panelLabel,
            @Named("newSensorName") JLabel newSensorName,
            @Named("newSensorType") JLabel newSensorType,
            @Named("newSensorNameField") JTextField newSensorNameField,
            @Named("newSensorTypeDropdown") JComboBox newSensorTypeDropdown,
            @Named("addNewSensorButton") JButton addNewSensorButton,
            @Named("sensorListPanel") JPanel sensorListPanel,
            @Named("newSensorPanel") JPanel newSensorPanel) {
        this.securityService = Objects.requireNonNull(securityService,"SecurityService must not be null");
        this.componentMap = Objects.requireNonNull(componentMap,"sensorComponentMap must not be null");
        this.panelLabel = Objects.requireNonNull(panelLabel,"sensorPanelLabel must not be null");
        this.newSensorName = Objects.requireNonNull(newSensorName,"newSensorName must not be null");
        this.newSensorType = Objects.requireNonNull(newSensorType,"newSensorType must not be null");
        this.newSensorNameField = Objects.requireNonNull(newSensorNameField,"newSensorNameField must not be null");
        this.newSensorTypeDropdown = Objects.requireNonNull(newSensorTypeDropdown,"newSensorTypeDropdown must not be null");
        this.addNewSensorButton = Objects.requireNonNull(addNewSensorButton,"addNewSensorButton must not be null");
        this.sensorListPanel = Objects.requireNonNull(sensorListPanel,"sensorListPanel must not be null");
        this.newSensorPanel = Objects.requireNonNull(newSensorPanel,"newSensorPanel must not be null");
    }

    /**
     * Build style
     */
    public void builder(){
        setLayout(new MigLayout());
        securityService.addStatusListener(this);
        // 显示标题“Sensor Management”
        panelLabel.setFont(StyleService.HEADING_FONT);

        // 动态面板，用于展示当前的传感器列表
        sensorListPanel.setLayout(new MigLayout());

        // 按钮事件 点击“Add New Sensor”按钮时，触发 addSensor 方法，将新传感器添加至系统。
        addNewSensorButton.addActionListener(e -> {
            Sensor sensor = new Sensor.Builder().setName(newSensorNameField.getText())
                    .setSensorType(SensorType.valueOf(Objects.requireNonNull(newSensorTypeDropdown.getSelectedItem()).toString()))
                    .build();
            addSensor(sensor,sensorListPanel);
        });
        // Sensor Management 下的表单面板，允许用户输入传感器信息并添加新传感器
        buildAddSensorPanel(newSensorPanel);
        // 先初始化 sensorListPanel
        // Initialize sensorListPanel first
        // I have replaced the previous updateSensorList
        // because each click operation needs to be processed through a loop,
        // which is a bit performance consuming.
        initSensorList(sensorListPanel);
        // Sensor Management标题
        add(panelLabel, "wrap");
        // Sensor Management面板的表单
        add(newSensorPanel, "span");
        // 传感器列表面板, 展示当前的传感器列表
        add(sensorListPanel, "span");
    }

    /**
     * Builds the panel with the form for adding a new sensor
     * 使用添加新传感器的表单构建面板
     */
    private void buildAddSensorPanel(JPanel p) {
        p.setLayout(new MigLayout());
        p.add(newSensorName);
        p.add(newSensorNameField, "width 50:100:200");
        p.add(newSensorType);
        p.add(newSensorTypeDropdown, "wrap");
        p.add(addNewSensorButton, "span 3");
    }

    /**
     * Used to initialize the sensor list. Sensors will display in the order that they are created.
     *  方法用于初始化传感器列表。
     * @param p The Panel to populate with the current list of sensor
     */
    private void initSensorList(JPanel p){
        securityService.getSensors().stream().sorted().forEach(s -> {
            initSensor(s,p);
        });
    }

    /**
     * To initialize the sensor settings,
     * you mainly need to set a unique identification name for the JButton JLabel component.
     *  初始化传感器的设置, 主要需要给 JButton JLabel组件设置唯一标识名称
     * @param s Sensor
     * @param p The Panel to populate with the current list of sensors
     */
    private void initSensor(Sensor s, JPanel p){
        JLabel sensorLabel = new JLabel(String.format("%s(%s): %s", s.getName(),  s.getSensorType().toString(),(s.getActive() ? "Active" : "Inactive")));
        JButton sensorToggleButton = new JButton((s.getActive() ? "Deactivate" : "Activate"));
        JButton sensorRemoveButton = new JButton("Remove Sensor");
        String sensorId = s.getSensorId().toString();

        // 添加 JLabel JButton 组件的uuid+name, 然后交给componentMap管理
        // Add the uuid+name of the JLabel JButton component, and then hand it over to componentMap for management
        addComponentToMap(sensorId+ Constants.LABEL,sensorLabel);
        addComponentToMap(sensorId+ Constants.TOGGLE,sensorToggleButton);
        addComponentToMap(sensorId+ Constants.REMOVE,sensorRemoveButton);

        sensorToggleButton.addActionListener(e -> setSensorActivity(s, !s.getActive()) );
        sensorRemoveButton.addActionListener(e -> removeSensor(s,p));

        //hard code some sizes, tsk tsk
        p.add(sensorLabel, "width 300:300:300");
        p.add(sensorToggleButton, "width 100:100:100");
        p.add(sensorRemoveButton, "wrap");
    }

    /**
     * Add Component to Map;
     *  添加Component到Map中；
     * @param name uuid + Component name
     * @param component
     * @param <T>
     */
    private <T extends Component> void addComponentToMap(String name, T component){
        componentMap.put(name, component);
    }

    private  <T extends Component> T getComponentByName(String name, Class<T> type) {
        Component component = componentMap.get(name);
        if (type.isInstance(component)) {
            return type.cast(component);
        }
        return null;
    }

    /**
     * setSensorActivity
     * 通过 sensorId 从MapComponent 来获取sensorToggleButton,然后就可以更新状态信息
     * Get sensorToggleButton from MapComponent through sensorId, and then update the status information
     * @param sensor
     * @param isActive
     */
    private synchronized void setSensorActivity(Sensor sensor, Boolean isActive) {
        securityService.changeSensorActivationStatus(sensor, isActive);
        // 更新 sensorToggleButton 的内容
        updateSensorToggleButtonText(sensor);
        // 重新绘制内容
        repaint();
    }

    private void updateSensorToggleButtonText(Sensor sensor){
        // 获取Sensor的uuid, 并根据sensorId来更新对应的组件
        String sensorId = sensor.getSensorId().toString();
        JButton sensorToggleButton = getComponentByName(sensorId+ Constants.TOGGLE, JButton.class);
        JLabel sensorLabel = getComponentByName(sensorId+ Constants.LABEL, JLabel.class);
        if (Objects.isNull(sensorToggleButton) || Objects.isNull(sensorLabel)){
            // SensorPanel类中Map的sensorToggleButton为null
            log.error("The sensorToggleButton or sensorLabel of the Map in the SensorPanel class is null");
            return;
        }
        sensorToggleButton.setText((sensor.getActive() ? "Deactivate" : "Activate"));
        sensorLabel.setText(String.format("%s(%s): %s",sensor.getName(),sensor.getSensorType().toString(),(sensor.getActive() ? "Active" : "Inactive")));
    }

    private void addSensor(Sensor sensor,JPanel p){
        if(securityService.getSensors().size() < 4) {
            securityService.addSensor(sensor);
            initSensor(sensor,p);
        } else {
            JOptionPane.showMessageDialog(null, "To add more than 4 sensors, please subscribe to our Premium Membership!");
        }
        // 重新绘制内容
        repaint();
        // 重新布局
        revalidate();
    }

    /**
     *
     * @param sensor
     * @param p
     */
    private void removeSensor(Sensor sensor,JPanel p) {
        securityService.removeSensor(sensor);
        String sensorId = sensor.getSensorId().toString();
        JLabel sensorLabel = getComponentByName(sensorId+ Constants.LABEL, JLabel.class);
        JButton sensorToggleButton = getComponentByName(sensorId+ Constants.TOGGLE, JButton.class);
        JButton sensorRemoveButton = getComponentByName(sensorId+ Constants.REMOVE, JButton.class);
        p.remove(sensorLabel);
        p.remove(sensorToggleButton);
        p.remove(sensorRemoveButton);

        repaint();
        revalidate();
    }


    @Override
    public void sensorStatusChanged() {
        Set<Sensor> sensors = securityService.getSensors();
        sensors.forEach(this::updateSensorToggleButtonText);
        repaint();
    }
}
