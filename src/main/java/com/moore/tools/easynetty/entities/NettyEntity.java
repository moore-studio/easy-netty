package com.moore.tools.easynetty.entities;

import com.moore.tools.easynetty.enums.CommandSendType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ：imoore
 * @date ：created in 2023/12/1 20:00
 * @description：client实体
 * @version: v1.0
 */
@Data
public class NettyEntity{
    private String clientId;
    private String formAddress;
    private String toAddress;
    private String taskId;
    private Command command;
    private String data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Command {
        private String sequence;
        private String command;
        private String type;
        private Long expired;
        private boolean encrypt;

        public Command(CommandSendType commandSendType) {
            setCommand(commandSendType.getCommand());
            setExpired(System.currentTimeMillis() + (commandSendType.getExpire() * 1000));
            setType(commandSendType.getType());
            setSequence(commandSendType.getSequence());
            setEncrypt(commandSendType.getEncrypt());
        }
    }


}
