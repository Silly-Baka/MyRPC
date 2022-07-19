package myRPC.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.DefaultByteBufHolder;
import io.netty.channel.DefaultChannelPipeline;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;
import myRPC.utils.SequenceIdGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Date: 2022/7/8
 * Time: 16:01
 *
 * @Author SillyBaka
 * Description：
 **/
public class testMessageCodec {
    public static void main(String[] args) throws Exception {
        MessageCodec messageCodec = new MessageCodec();
        EmbeddedChannel channel = new EmbeddedChannel(
                new LengthFieldBasedFrameDecoder(1024,12,4,0,0),
                new LoggingHandler(),
                messageCodec);
        RpcRequestMessage requestMessage = new RpcRequestMessage("niubi", "666", new Class[]{testMessageCodec.class}, new Object[]{testMessageCodec.class});
        Integer sequenceId = SequenceIdGenerator.getSequenceId();
        requestMessage.setSequenceId(sequenceId);
//        channel.writeOutbound(requestMessage);
        List<Object> list = new ArrayList<>();
        messageCodec.encode(null,requestMessage,list);

        channel.writeInbound(list.get(0));
    }
}
