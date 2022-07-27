package joey.mqtt.broker.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import joey.mqtt.broker.core.MqttMaster;
import joey.mqtt.broker.util.NettyUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * mqtt协议事件处理handler
 *
 * @author Joey
 * @date 2019/7/18
 */
@Slf4j
@ChannelHandler.Sharable
public class MqttMainHandler extends SimpleChannelInboundHandler<MqttMessage> {
    private final MqttMaster master;

    public MqttMainHandler(MqttMaster master) {
        this.master = master;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MqttMessage msg) throws Exception {
        NettyUtils.checkMessage(msg);

        MqttMessageType messageType = msg.fixedHeader().messageType();
        try {
            //协议事件参考：https://juejin.im/post/5bcc4e1ce51d457a0e17c908#heading-55
            switch (messageType) {
                case CONNECT:
                    master.connect(ctx, (MqttConnectMessage) msg);
                    break;

                case PUBLISH:
                    master.publish(ctx, (MqttPublishMessage) msg);
                    break;

                case PUBACK:
                    master.pubAck(ctx, (MqttPubAckMessage) msg);
                    break;

                case PUBREC:
                    master.pubRec(ctx, msg);
                    break;

                case PUBREL:
                    master.pubRel(ctx, msg);
                    break;

                case PUBCOMP:
                    master.pubComp(ctx, msg);
                    break;

                case SUBSCRIBE:
                    master.subscribe(ctx, (MqttSubscribeMessage) msg);
                    break;

                case UNSUBSCRIBE:
                    master.unsubscribe(ctx, (MqttUnsubscribeMessage) msg);
                    break;

                case PINGREQ:
                    master.pingReq(ctx, msg);
                    break;

                case DISCONNECT:
                    master.disconnect(ctx, msg);
                    break;

                default:
                    log.error("Unsupported messageType:{}", messageType);
                    break;
            }
        } catch (Throwable ex) {
            log.error("MqttMainHandler handle message exception: " + ex.getCause(), ex);
            ctx.fireExceptionCaught(ex);
            ctx.close();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.debug("MqttMainHandler-channelInactive clientId={},userName={}", NettyUtils.clientId(ctx.channel()), NettyUtils.userName(ctx.channel()));
        master.lostConnection(ctx);
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.warn("MqttMainHandler-exceptionCaught clientId={},userName={}", NettyUtils.clientId(ctx.channel()), NettyUtils.userName(ctx.channel()), cause);
        ctx.close();
    }

    /**
     * 处理心跳超时
     * 参考：https://github.com/moquette-io/moquette/blob/master/broker/src/main/java/io/moquette/broker/MoquetteIdleTimeoutHandler.java
     *
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;

            if (idleStateEvent.state() == IdleState.ALL_IDLE) {
                log.warn("MqttMainHandler-idleStateEvent clientId={},userName={}", NettyUtils.clientId(ctx.channel()), NettyUtils.userName(ctx.channel()));
                //fire a channelInactive to trigger publish of Will
                ctx.fireChannelInactive();
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
