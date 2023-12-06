package com.moore.tools.easynetty.uitils;

import java.lang.reflect.Constructor;
import java.util.Objects;

/**
 * @author ：imoore
 * @date ：created in 2023/12/4 20:29
 * @description：共通工具
 * @version: v
 */
public class CommonUtils {

    /**
     * 实例
     *
     * @param clazz
     * @param <E>
     * @return
     */
    public static <E> E tryNewInstance(Class<E> clazz, Class<?>[] constructorParamTypes, Object... parameters) {
        try {
            if (Objects.isNull(clazz)) {
                return null;
            }

            if (parameters.length > 0 && constructorParamTypes != null && constructorParamTypes.length > 0) {
                Constructor<E> constructor = clazz.getConstructor(constructorParamTypes);
                return constructor.newInstance(parameters);
            } else {
                return clazz.getDeclaredConstructor().newInstance();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

}
