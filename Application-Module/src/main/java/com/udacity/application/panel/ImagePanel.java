package com.udacity.application.panel;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.udacity.constant.common.Constants;
import com.udacity.security.service.SecurityService;
import com.udacity.security.service.StatusListener;
import net.miginfocom.swing.MigLayout;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

/** Panel containing the 'camera' output. Allows users to 'refresh' the camera
 * 包含‘camera’输出的面板。允许用户“刷新”相机
 * by uploading their own picture, and 'scan' the picture, sending it for image analysis
 * 上传自己的照片，并“扫描”照片，发送给图像分析
 * ImagePanel(图像面板)
 * 目的：在检测到事件（如检测到猫）时，动态更新界面显示
 * 主要功能：
 * 	1.	显示当前摄像头图像。
 * 	2.	允许用户上传图片作为当前摄像头图像。
 * 	3.	提供按钮扫描图像，并将图像发送至 SecurityService 进行分析。
 *
 */

public class ImagePanel extends JPanel implements StatusListener {
    private final SecurityService securityService;
    private final JLabel cameraHeader;
    private final JLabel cameraLabel;
    private final JButton addPictureButton;
    private final JButton scanPictureButton;
    private BufferedImage currentCameraImage;

    @Inject
    ImagePanel(
            SecurityService securityService,
            @Named("imageCameraHeader") JLabel cameraHeader,
            @Named("imageCameraLabel") JLabel cameraLabel,
            @Named("imageAddPictureButton") JButton addPictureButton,
            @Named("imageScanPictureButton") JButton scanPictureButton) {
        this.securityService = Objects.requireNonNull(securityService,"SecurityService must not be null");
        this.cameraHeader = Objects.requireNonNull(cameraHeader,"imageCameraHeader must not be null");
        this.cameraLabel = Objects.requireNonNull(cameraLabel,"imageCameraLabel must not be null");
        this.addPictureButton = Objects.requireNonNull(addPictureButton,"imageAddPictureButton must not be null");
        this.scanPictureButton = Objects.requireNonNull(scanPictureButton,"imageScanPictureButton must not be null");
    }

    /**
     * Build style
     */
    public void builder(){
        setLayout(new MigLayout()); // 设置布局管理器
        securityService.addStatusListener(this);
        cameraHeader.setFont(StyleService.HEADING_FONT);
        //create a label for the camera image
        // 用于显示当前摄像头图像，设置了固定尺寸和边框
        cameraLabel.setBackground(Color.WHITE);
        cameraLabel.setPreferredSize(new Dimension(Constants.IMAGE_WIDTH, Constants.IMAGE_HEIGHT));
        cameraLabel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        //button allowing users to select a file to be the current camera image
        // 添加一个按钮，允许用户选择一个文件作为当前摄像头图像(上传图片功能)
        // 功能描述：
        //	•	使用 JFileChooser 打开文件选择对话框，让用户选择图片文件。
        //	•	读取用户选择的图片，并使用 ImageIO.read 加载图片数据到 BufferedImage。
        //	•	使用 ImageIcon 和 getScaledInstance 方法对图片进行缩放，确保其适配面板大小。
        //	•	捕获可能的异常（如文件格式不正确），并通过对话框通知用户。
        addPictureButton.addActionListener(e -> {
            JFileChooser chooser = getJFileChooser();
            // 用户未选择文件则返回
            if(chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            try {
                currentCameraImage = ImageIO.read(chooser.getSelectedFile());
                Image tmp = new ImageIcon(currentCameraImage).getImage();
                cameraLabel.setIcon(new ImageIcon(tmp.getScaledInstance(Constants.IMAGE_WIDTH, Constants.IMAGE_HEIGHT, Image.SCALE_SMOOTH)));
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(null, "Invalid image selected.");
            }
            // 调用 repaint 方法强制面板重新绘制
            // 更新 cameraLabel 的内容后，需要通知界面刷新。
            repaint();
        });

        //button that sends the image to the image service
        // 图像扫描功能
        scanPictureButton.addActionListener(e -> {
            securityService.processImage(currentCameraImage);
        });

        add(cameraHeader, "span 3, wrap");
        add(cameraLabel, "span 3, wrap");
        add(addPictureButton);
        add(scanPictureButton);
    }

    private JFileChooser getJFileChooser() {
        JFileChooser chooser = new JFileChooser();
        // 将文件选择器的默认目录设置为当前目录
        chooser.setCurrentDirectory(new File("."));
        chooser.setDialogTitle("Select Picture or Navigate Directories");
        // 限制文件选择器仅允许选择文件，不允许选择目录
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        // 添加文件过滤器，仅显示支持的图片格式
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Supported Image Files (jpg, png, bmp, gif, wbmp)",
                "jpg", "jpeg", "png", "bmp", "gif", "wbmp"
        ));
        return chooser;
    }


    @Override
    public void catDetected(boolean catDetected) {
        securityService.setCatDetected(catDetected);
        if(catDetected) {
            cameraHeader.setText("DANGER - CAT DETECTED");
        } else {
            cameraHeader.setText("Camera Feed - No Cats Detected");
        }
    }

}
