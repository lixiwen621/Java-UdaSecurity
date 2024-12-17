package com.udacity.application.panel;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.udacity.constant.enums.ArmingStatus;
import com.udacity.security.service.SecurityService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * JPanel containing the buttons to manipulate arming status of the system.
 * JPanel 包含用于操作系统布防状态的按钮
 * ControlPanel(控制面板): 继承自 JPanel，是一个 Swing 容器类，用于放置其他组件
 *
 * 向buttonMap中的每个按钮添加一个操作侦听器，它将安全服务的武装状态设置为相应的ArmingStatus，并重新为按钮上色以反映当前状态。
 * 当点击按钮时，将执行以下操作：
 * 1、安全服务的武装状态设置为与所单击的按钮相关联的ArmingStatus。
 * 2、被单击按钮的背景颜色被设置为与其ArmingStatus相关联的颜色。
 * 3、所有其他按钮的背景色设置为null，恢复其默认外观。
 *
 */
public class ControlPanel extends JPanel {
    private final SecurityService securityService;
    // buttonMap: 存储一个 ArmingStatus 枚举值到对应 JButton 的映射，便于通过状态操作按钮
    private final Map<ArmingStatus, JButton> buttonMap;
    private final JLabel panelLabel;

    @Inject
    ControlPanel(
            SecurityService securityService,
            @Named("controlButtonMap") Map<ArmingStatus, JButton> buttonMap,
            @Named("controlPanelLabel") JLabel panelLabel) {
        this.securityService = Objects.requireNonNull(securityService,"SecurityService must not be null");
        this.buttonMap = Objects.requireNonNull(buttonMap,"Map<ArmingStatus, JButton> buttonMap must not be null");
        this.panelLabel = Objects.requireNonNull(panelLabel,"JLabel panelLabel must not be null");
    }

    /**
     * Build style
     */
    public void builder(){
        // 使用 MigLayout 设置布局，MigLayout 提供了灵活的布局管理功能
        setLayout(new MigLayout());
        // 通过 StyleService.HEADING_FONT 设置标题字体样式
        panelLabel.setFont(StyleService.HEADING_FONT);
        // 布局约束: "span 3, wrap" 表示该组件横跨 3 列并自动换行
        add(panelLabel, "span 3, wrap");

        //add an action listener to each button that applies its arming status and recolors all the buttons
        // 添加事件监听 遍历按钮映射: 为每个按钮添加点击事件
        // 状态更新: 点击按钮时，通过 securityService 将系统布防状态设置为对应的 ArmingStatus
        buttonMap.forEach((k, v) -> {
            v.addActionListener(e -> {
                //恢复上一个状态的按钮颜色
                // Restore the button color of the previous state
                JButton previousButton = buttonMap.get(securityService.getArmingStatus());
                if (!Objects.isNull(previousButton)){
                    previousButton.setBackground(null); // 恢复默认背景颜色
                    previousButton.setOpaque(false); //消除背景
                }
                // 状态更新: 将系统布防状态设置为当前按钮的 ArmingStatus
                securityService.setArmingStatus(k);

                // 设置当前状态的按钮颜色
                // Set the button color for the current state
                JButton currentButton = buttonMap.get(k);
                // 只有被点击的按钮的背景设置为 status.getColor()，其他按钮恢复默认
                currentButton.setBackground(k.getColor());
                // 移除按钮边框, 可以让背景色跟按钮整体颜色一致
                // Remove the button border to make the background color consistent
                // with the overall color of the button
                currentButton.setBorderPainted(false);
                // 确保按钮完全不透明
                currentButton.setOpaque(true);
            });
        });

        //map order above is arbitrary, so loop again in order to add buttons in enum-order
        // 添加按钮到面板 按照枚举顺序: 遍历 ArmingStatus 枚举的值，按顺序将按钮添加到面板
        Arrays.stream(ArmingStatus.values()).forEach(status -> add(buttonMap.get(status)));
        // 重置当前状态按钮颜色
        ArmingStatus currentStatus = securityService.getArmingStatus();
        // 如果当前状态为 ArmingStatus.DISARMED, 重置所有传感器激活状态
        if (currentStatus == ArmingStatus.DISARMED){
            securityService.resetSensorToInactive();
        }
        JButton currentButton = buttonMap.get(currentStatus);
        currentButton.setBackground(currentStatus.getColor());
        currentButton.setBorderPainted(false);
        currentButton.setOpaque(true);
    }
}
