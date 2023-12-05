package com.moore.tools.easynetty.service.dm.server;

import com.alibaba.fastjson2.JSON;
import com.moore.tools.easynetty.entities.ChannelEntity;
import com.moore.tools.easynetty.entities.NettyEntity;
import com.moore.tools.easynetty.entities.ReplyConfirm;
import com.moore.tools.easynetty.enums.CommandSendType;
import com.moore.tools.easynetty.service.security.IChannelHandler;
import com.moore.tools.easynetty.service.security.conditions.IOperation;
import com.moore.tools.easynetty.service.dm.condition.impl.DefaultOperation;
import com.moore.tools.easynetty.service.dm.condition.impl.ServerSecretOperation;
import com.moore.tools.easynetty.service.dm.condition.impl.ReplyConfirmOperation;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * @author ：imoore
 * @date ：created in 2023/12/2 17:55
 * @description：
 * @version: v
 */
@Slf4j
public class ServerChannelImpl implements IChannelHandler {
    Map<String, IOperation> condition = null;
    /**
     * TODO:应答确认，后续使用数据库
     */
    public static List<ReplyConfirm> replyConfirms = new ArrayList<>(10);

    public ServerChannelImpl() {
        condition = new HashMap<>(CommandSendType.values().length);
        condition.put(CommandSendType.SECRET_KEY.getCommand(), new ServerSecretOperation());
        condition.put(CommandSendType.REPLY.getCommand(), new ReplyConfirmOperation());
        condition.put(CommandSendType.REPLY_SECRET.getCommand(), new ReplyConfirmOperation());
    }

    /**
     * 存储所有连接的Channel
     */
    private static final ChannelGroup CHANNELS = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static final List<ChannelEntity> CHANNEL_ENTITIES = new ArrayList<>();


    @Override
    public void active(Channel channel) {
        CHANNELS.add(channel);
        CHANNEL_ENTITIES.add(new ChannelEntity().build(channel.id()));
        log.info("client connected:" + channel.remoteAddress().toString());
    }

    @Override
    public void inactive(Channel channel) {
        CHANNELS.remove(channel);
        CHANNEL_ENTITIES.removeIf(item -> Objects.equals(item.getChannelId(), channel.id()));
        log.info("Client disconnected:" + channel.remoteAddress().toString());
    }

    @Override
    public <R extends NettyEntity> void received(Channel channel, R entity) {
        log.info("received:{}", JSON.toJSONString(entity));
        CHANNEL_ENTITIES.stream().filter(en -> Objects.equals(channel.id(), en.getChannelId())).findFirst().ifPresent(channelEntity -> {
            IOperation operation = Optional.ofNullable(condition.get(entity.getCommand().getCommand())).orElse(new DefaultOperation());
            ReplyConfirm replyConfirm = replyConfirms.stream().filter(r -> entity.getTaskId().equalsIgnoreCase(r.getSequence())).findFirst().orElse(null);
            operation.execute(replyConfirm, channel, channelEntity, entity);
        });
    }

    @Override
    public void exceptionCaught(Channel channel, Throwable throwable) {

    }
}
