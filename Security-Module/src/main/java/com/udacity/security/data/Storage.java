package com.udacity.security.data;

import java.lang.reflect.Type;

/**
 *  创建一个抽象存储接口，以解耦 Preferences 的具体实现。如果未来需要更换存储方式，比如数据库或文件存储，只需更换实现类。
 *  Create an abstract storage interface to decouple the concrete implementation of Preferences.
 *  If you need to change the storage method in the future, such as database or file storage,
 *  you only need to change the implementation class.
 */
public interface Storage {
    /**
     * 存储数据
     *  Store data as json
     * @param key  Data association key
     * @param value Data
     * @param <T>
     */
    <T> void saveToJSON(String key,T value);

    /**
     *  put data
     * @param key String
     * @param value String
     */
    void put(String key,String value);

    /**
     * 初始化数据
     * initialization data
     * @param key Data association key
     * @param typeOfT   data type
     * @param defaultValueObject If the initialization data is null, set the default data object
     * @return
     * @param <T>
     */
    <T> T load(String key, Type typeOfT, T defaultValueObject);

    /**
     * 从数据库中获取数据, 获取的数据为null, 会给个默认值
     * @param key
     * @param defaultValue
     * @return
     */
    String get(String key, String defaultValue);

}
