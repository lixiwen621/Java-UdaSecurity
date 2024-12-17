package com.udacity.image.config;

import com.udacity.constant.utils.EncryptionUtil;

public class encryptedAwsConfig {
    public static String decryptAws(String key,String encryptedAwsString) throws Exception {
        return EncryptionUtil.decrypt(encryptedAwsString, key);
    }
}
