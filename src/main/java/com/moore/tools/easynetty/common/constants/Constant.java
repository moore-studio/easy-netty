package com.moore.tools.easynetty.common.constants;

import io.netty.util.AttributeKey;

/**
 * @author ：imoore
 * @date ：created in 2023/11/25 0:07
 * @description：
 * @version: v
 */
public class Constant {
    public static final Integer INVALID_THREADS = 0;

    public static final String CLIENT_IDENTIFY_ID = "0000000000000000000000000";
    public static final String SERVER_IDENTITY_ID = "1111111111111111111111111";
    public static final AttributeKey<String> ATTR_IDENTIFY_ID = AttributeKey.valueOf("identifyId");
}
