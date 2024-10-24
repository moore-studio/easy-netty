package com.moore.tools.easynetty.common.constants;

/**
 * log定义
 *
 * @author ：imoore
 */
public class LogMessageConstant {
    public final static String W_MSG_NO_SEND = "The current message cannot be sent.({})";
    public final static String E_IDENTITY_ID_NOT_SET = "Identify id not be set at {}";

    public final static String E_SERVER_NOT_BE_CONFIGURED = "Server not be configured.({})";

    public final static String I_HEART_BEAT_READ_TIMEOUT = "Read timeout: No data received for a while.";
    public final static String I_HEART_BEAT_WRITE_TIMEOUT = "Write timeout: No data sent for a while.";
    public final static String I_ALL_IDLE_TIMEOUT = "All timeout: No read or write for a while.";
    //*******************************Receiver******************************************************//

    public final static String E_THROW_ERROR = "An exception occurred while {}:{}.";
    //********************************************NETTY********************************************//

    public final static String E_CHANNEL_IS_INACTIVE = "{} channel is inactive.({})";
    public final static String I_NETTY_START = "{} started on ({}:{})";
    public final static String I_NETTY_STOP = "{} stopped.";
    public final static String E_SENDER_NO_IMPLEMENT = "ISender not be implemented,please bind first.";

    public final static String W_NETTY_NO_INSTANCE = "{} non instance.";

    public final static String W_RECONNECTED_MSG = "Unable to connect to the server, try to connect,retries:{} / {}";
    public final static String I_RECONNECTED_SERVICE_SHUTDOWN = "Reconnected service shutdown.";

}
