package com.udacity.image.service;


import java.awt.image.BufferedImage;

/**
 *  Image Interface
 */
public interface ImageService {
    boolean imageContainsCat(BufferedImage image, float confidenceThreshold);
}
