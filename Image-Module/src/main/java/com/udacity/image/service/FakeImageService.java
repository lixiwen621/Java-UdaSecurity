package com.udacity.image.service;

import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Service that tries to guess if an image displays a cat.
 * AwsImageService 的模拟类，主要模拟 AwsImageService 中的 imageContainsCat方法, 也可以用Mockito代替
 */
public class FakeImageService implements ImageService {
    private final Random r = new Random();

    public boolean imageContainsCat(BufferedImage image, float confidenceThreshhold) {
        return r.nextBoolean();
    }
}
