package com.udacity.application.panel;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.udacity.constant.enums.AlarmStatus;
import com.udacity.security.service.SecurityService;
import com.udacity.security.service.StatusListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.Objects;

/**
 * Displays the current status of the system. Implements the StatusListener
 *  显示系统当前状态。实现状态监听器
 * interface so that it can be notified whenever the status changes.
 * 接口，以便每当状态发生变化时都能得到通知。
 *
 * DisplayPanel(显示面板)
 *
 * 构造一个新的DisplayPanel实例来显示当前的安全系统状态。
 * 面板包括标题、系统状态标签和显示当前告警状态的标签。
 * 面板将自己注册为StatusListener，并提供SecurityService，当告警状态发生变化时 以便它可以被通知
 *  securityService用于监控告警状态的securityService实例
 */
public class DisplayPanel extends JPanel implements StatusListener {
    private final SecurityService securityService;
    private final JLabel panelLabel;
    private final JLabel systemStatusLabel;
    private final JLabel currentStatusLabel;

    @Inject
    DisplayPanel(
            SecurityService securityService,
            @Named("displayPanelLabel") JLabel panelLabel,
            @Named("displaySystemStatusLabel") JLabel systemStatusLabel,
            @Named("displayCurrentStatusLabel") JLabel currentStatusLabel) {
        this.securityService = Objects.requireNonNull(securityService,"SecurityService must not be null");
        this.panelLabel = Objects.requireNonNull(panelLabel,"displayPanelLabel must not be null");
        this.systemStatusLabel = Objects.requireNonNull(systemStatusLabel,"displaySystemStatusLabel must not be null");
        this.currentStatusLabel = Objects.requireNonNull(currentStatusLabel,"displayCurrentStatusLabel must not be null");
    }

    /**
     *  Build style
     */
    public void builder(){
        setLayout(new MigLayout());  // 设置布局管理器
        securityService.addStatusListener(this);
        panelLabel.setFont(StyleService.HEADING_FONT); // 设置标题的字体格式
        notify(securityService.getAlarmStatus());
        // span 2：表示当前组件占据两列,也就是横跨2列 , wrap：表示组件会自动换行
        add(panelLabel, "span 2, wrap");
        add(systemStatusLabel);
        add(currentStatusLabel, "wrap");
    }

    @Override
    public void notify(AlarmStatus alarmStatus) {
        currentStatusLabel.setText(alarmStatus.getDescription());
        currentStatusLabel.setBackground(alarmStatus.getColor());
        currentStatusLabel.setOpaque(true);
    }

}
