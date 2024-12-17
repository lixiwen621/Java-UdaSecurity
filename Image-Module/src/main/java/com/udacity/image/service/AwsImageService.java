package com.udacity.image.service;


import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsResponse;
import software.amazon.awssdk.services.rekognition.model.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Image Recognition Service that can identify cats. Requires aws credentials to be entered in config.properties to work.
 * 可以识别猫的图像识别服务, 主要是扫描图片服务(Scanning image service), 需要aws凭证才能工作
 * Steps to make work (optional):
 * 1. Log into AWS and navigate to the AWS console
 *    登录 AWS 并导航至 AWS 控制台
 * 2. Search for IAM then click on Users in the IAM nav bar
 *    搜索 IAM，然后单击 IAM 导航栏中的用户
 * 3. Click Add User. Enter a user name and select Programmatic access
 *    单击添加用户。输入用户名并选择编程访问
 * 4. Next to Permissions. Select 'Attach existing policies directly' and attack 'AmazonRekognitionFullAccess'
 *    在权限旁边。选择“直接附加现有策略”并攻击“AmazonRekognitionFullAccess”
 * 5. Next through the remaining screens. Copy the 'Access key ID' and 'Secret access key' for this user.
 *    接下来浏览剩余的屏幕。复制该用户的“访问密钥 ID”和“秘密访问密钥”
 * 6. Create a config.properties file in the src/main/resources dir containing the keys referenced in this class
 *    在 src/main/resources 目录中创建一个 config.properties 文件，其中包含此类中引用的键
 *      aws.id=[your access key id]
 *      aws.secret=[your Secret access key]
 *      aws.region=[an aws region of choice. For example: us-east-2]
 */
public class AwsImageService implements ImageService {

    private final Logger log = LoggerFactory.getLogger(AwsImageService.class);

    //aws recommendation is to maintain only a single instance of client objects
    // AWS Rekognition 客户端，按 AWS 的建议使用单例模式，避免重复创建客户端实例
    private final RekognitionClient rekognitionClient;

    @Inject
    public AwsImageService(RekognitionClient rekognitionClient) {
        Objects.requireNonNull(rekognitionClient, "rekognitionClient must not be null");
        this.rekognitionClient = rekognitionClient;
    }

    /**
     * Returns true if the provided image contains a cat.
     *  如果提供的图像包含猫，则返回 true
     * @param image Image to scan  要扫描的图像
     * @param confidenceThreshhold Minimum threshhold to consider for cat. For example, 90.0f would require 90% confidence minimum
*                                  标签的最小置信度（百分比）
     * @return
     */
    public boolean imageContainsCat(BufferedImage image, float confidenceThreshhold) {
        Image awsImage = null;
        // 使用 ImageIO 将 BufferedImage 写入 ByteArrayOutputStream
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ImageIO.write(image, "jpg", os);
            // 将图像字节数组转换为 AWS 的 Image 对象
            awsImage = Image.builder().bytes(SdkBytes.fromByteArray(os.toByteArray())).build();
        } catch (IOException ioe) {
            log.error("Error building image byte array", ioe);
            return false;
        }
        // 指定图像和置信度阈值
        DetectLabelsRequest detectLabelsRequest = DetectLabelsRequest.builder().image(awsImage).minConfidence(confidenceThreshhold).build();
        // 调用 AWS Rekognition
        DetectLabelsResponse response = rekognitionClient.detectLabels(detectLabelsRequest);
        logLabelsForFun(response);
        return response.labels().stream()
                .anyMatch(l -> l.name().equalsIgnoreCase("cat"));
    }

    public boolean imageContainsLabel(BufferedImage image, String label,float confidenceThreshhold) {
        Image awsImage = null;
        // 使用 ImageIO 将 BufferedImage 写入 ByteArrayOutputStream
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ImageIO.write(image, "jpg", os);
            // 将图像字节数组转换为 AWS 的 Image 对象
            awsImage = Image.builder().bytes(SdkBytes.fromByteArray(os.toByteArray())).build();
        } catch (IOException ioe) {
            log.error("Error building image byte array", ioe);
            return false;
        }
        // 指定图像和置信度阈值
        DetectLabelsRequest detectLabelsRequest = DetectLabelsRequest.builder()
                .image(awsImage)
                .minConfidence(confidenceThreshhold)
                .build();

        // 调用 AWS Rekognition
        DetectLabelsResponse response = rekognitionClient.detectLabels(detectLabelsRequest);
        logLabelsForFun(response);
        return response.labels().stream()
                .anyMatch(l -> l.name().toLowerCase()
                .contains(label.toLowerCase()));
    }

    // 将检测到的标签名和置信度拼接成字符串，输出到日志
    // 例如：Cat(95.5%), Dog(90.2%)
    private void logLabelsForFun(DetectLabelsResponse response) {
        log.info(response.labels().stream()
                .map(label -> String.format("%s(%.1f%%)", label.name(), label.confidence()))
                .collect(Collectors.joining(", ")));
    }
}
