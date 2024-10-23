package com.moore.tools.easynetty.common.constants;

/**
 * log定义
 *
 * @author ：imoore
 */
public class LogMessageConstant {
    public final static String W_MSG_NO_SEND = "The current message cannot be sent.({})";
    public final static String E_IDENTITY_ID_NOT_SET = "Identify id not be set at {}";

    public final static String E_SERVER_NOT_BE_CONFIGURED = "Server not be configured.";

    public final static String I_HEART_BEAT_READ_TIMEOUT = "Read timeout: No data received for a while.";
    public final static String I_HEART_BEAT_WRITE_TIMEOUT = "Write timeout: No data sent for a while.";
    public final static String I_ALL_IDLE_TIMEOUT = "All timeout: No read or write for a while.";
}
