package com.udacity.application.panel;

import com.google.inject.Inject;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.Objects;

/**
 * This is the primary JFrame for the application that contains all the top-level JPanels.
 * 这是包含所有顶级 JPanel 的应用程序的主要 JFrame。
 *
 * We're not using any dependency injection framework, so this class also handles constructing
 * 我们没有使用任何依赖注入框架，所以这个类也处理构造
 * all our dependencies and providing them to other classes as necessary.
 * 我们所有的依赖项并根据需要将它们提供给其他类
 */
public class CatpointGui extends JFrame {
    private final DisplayPanel displayPanel;
    private final ImagePanel imagePanel;
    private final ControlPanel controlPanel;
    private final SensorPanel sensorPanel;

    @Inject
    CatpointGui(DisplayPanel displayPanel,
                ImagePanel imagePanel,
                ControlPanel controlPanel,
                SensorPanel sensorPanel) {
        this.displayPanel = Objects.requireNonNull(displayPanel,"DisplayPanel must not be null");
        this.imagePanel = Objects.requireNonNull(imagePanel,"ImagePanel must not be null");
        this.controlPanel = Objects.requireNonNull(controlPanel,"ControlPanel must not be null");
        this.sensorPanel = Objects.requireNonNull(sensorPanel,"SensorPanel must not be null");
    }

    /**
     * Build style
     */
    public void builder(){
        // build displayPanel imagePanel controlPanel sensorPanel
        displayPanel.builder();
        imagePanel.builder();
        controlPanel.builder();
        sensorPanel.builder();
        setLocation(100, 100); // 设置窗口初始位置
        setSize(600, 850); // 设置窗口大小
        setTitle("Very Secure App"); // 设置窗口标题
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 设置关闭操作

        JPanel mainPanel = new JPanel(); // 创建主面板
        mainPanel.setLayout(new MigLayout()); // 使用 MigLayout 布局管理器
        mainPanel.add(displayPanel, "wrap"); // 添加显示面板，"wrap" 表示下一项换行
        mainPanel.add(imagePanel, "wrap"); // 添加图像面板
        mainPanel.add(controlPanel, "wrap"); // 添加控制面板
        mainPanel.add(sensorPanel);  // 添加传感器面板

        getContentPane().add(mainPanel); // 将主面板添加到窗口内容区域
    }
}
