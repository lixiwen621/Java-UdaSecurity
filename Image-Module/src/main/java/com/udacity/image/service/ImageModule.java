package com.udacity.image.service;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.udacity.constant.common.Constants;
import com.udacity.image.config.encryptedAwsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ImageModule extends AbstractModule {
    private final Logger log = LoggerFactory.getLogger(ImageModule.class);

    private final String control;
    public ImageModule(String control){
        this.control = control;
    }

    @Override
    protected void configure() {
        // 绑定 ImageService 接口的实现
        if (control.equals(Constants.AWS_CONTROL)) {
            configureAwsBindings();
        } else {
            bind(ImageService.class).to(FakeImageService.class).in(Singleton.class);
        }
    }

    private void configureAwsBindings() {
        bind(RekognitionClient.class).toInstance(createRekognitionClient());
        bind(ImageService.class).to(AwsImageService.class).in(Singleton.class);
    }

    private RekognitionClient createRekognitionClient() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (is == null) {
                throw new RuntimeException("AWS config.properties file not found");
            }
            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Error loading AWS properties", e);
        }
        String awsId = props.getProperty("aws.id");
        String awsSecret = props.getProperty("aws.secret");
        String awsRegion = props.getProperty("aws.region");
        try {
            String decryptKey = props.getProperty("decrypt.key");
            awsId = encryptedAwsConfig.decryptAws(decryptKey, awsId);
            awsSecret = encryptedAwsConfig.decryptAws(decryptKey, awsSecret);
        }catch (Exception e){
            throw new RuntimeException("decryptAws Fail");
        }

        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(awsId, awsSecret);
        return RekognitionClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .region(Region.of(awsRegion))
                .build();
    }

}
