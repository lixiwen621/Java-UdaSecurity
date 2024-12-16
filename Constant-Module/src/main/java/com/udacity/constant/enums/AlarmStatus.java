package com.udacity.constant.enums;


import java.awt.*;

/**
 * List of potential states the alarm can have. Also contains metadata about what
 * text and color is associated with the alarm.
 * 警报可能具有的潜在状态列表。还包含有关什么的元数据
 * 文本和颜色与警报相关。
 */
public enum AlarmStatus {

    NO_ALARM("Cool and Good", new Color(120,200,30)), //表示系统运行正常无报警
    PENDING_ALARM("I'm in Danger...", new Color(200,150,20)), // 待处理警报
    ALARM("Awooga!", new Color(250,80,50)); //系统处于报警状态

    private final String description;
    private final Color color;

    AlarmStatus(String description, Color color) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be null or empty");
        }
        if (color == null) {
            throw new IllegalArgumentException("Color cannot be null");
        }
        if (color.getRed() < 0 || color.getRed() > 255 ||
                color.getGreen() < 0 || color.getGreen() > 255 ||
                color.getBlue() < 0 || color.getBlue() > 255) {
            throw new IllegalArgumentException("AlarmStatus Invalid RGB color values");
        }
        this.description = description;
        this.color = color;
    }

    public String getDescription() {
        return description;
    }

    public Color getColor() {
        return color;
    }

    @Override
    public String toString() {
        return name();
    }

    public static void main(String[] args) {
        String noAlarmString = AlarmStatus.NO_ALARM.toString();
        System.out.println(noAlarmString);
    }
}
