package com.udacity.constant.enums;


import java.awt.*;

/**
 * List of potential states the security system can use to describe how the system is armed.
 * Also contains metadata about what text and color is associated with the arming status.
 *  安全系统可用于描述系统如何布防的潜在状态列表。
 *  还包含有关与布防状态相关联的文本和颜色的元数据。
 */
public enum ArmingStatus {
    DISARMED("Disarmed", new Color(120,200,30)),// 安防系统未布防状态
    ARMED_HOME("Armed - At Home", new Color(190,180,50)),// 在家布防状态
    ARMED_AWAY("Armed - Away", new Color(170,30,150));//外出布防状态

    private final String description;
    private final Color color;

    ArmingStatus(String description, Color color) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be null or empty");
        }
        if (color == null) {
            throw new IllegalArgumentException("Color cannot be null");
        }
        if (color.getRed() < 0 || color.getRed() > 255 ||
                color.getGreen() < 0 || color.getGreen() > 255 ||
                color.getBlue() < 0 || color.getBlue() > 255) {
            throw new IllegalArgumentException("ArmingStatus Invalid RGB color values");
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
        for (ArmingStatus armingStatus : ArmingStatus.values()) {
            System.out.println(armingStatus);
        }
    }
}
