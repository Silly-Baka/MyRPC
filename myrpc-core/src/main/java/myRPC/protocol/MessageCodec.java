package myRPC.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import myRPC.config.RpcConfig;

import java.util.List;

/**
 * Date: 2022/7/8
 * Time: 15:17
 * @Author SillyBaka
 *
 * Description： 自定义的编解码器
 * 自定义协议为： 4字节的魔数+1字节版本控制号+1字节的指令类型+1字节的序列化算法+4字节的请求序列号+1字节无意义+4字节的数据内容实际长度+数据内容
 *  *  +---------------+-----------字节的版本控制号+----------------------+-------------------------------------------------+
 *  |  Magic Number | Control Num | Order Type | Serializer Type| SequenceId | None Sense | Data Length |
 *  |    4 bytes    |   1 byte    |   1 byte   |    1 bytes     |  4 bytes   |  1 byte    |   4 bytes   |
 *  +---------------------------------------------------+---------------+-----------------+-------------+
 *  |                                           Data Bytes                                              |
 *  |                                      Length: ${Data Length}                                       |
 *   +--------------------------------------------------------------------------------------------------+
 **/
@ChannelHandler.Sharable
public class MessageCodec extends MessageToMessageCodec<ByteBuf,Message> {
    /**
     * 魔数
     */
    private static final byte[] MAGIC_NUM = new byte[]{'b','a','k','a'};
    /**
     * 编码
     */
    @Override
    public void encode(ChannelHandlerContext ctx, Message msg, List<Object> out){
        ByteBuf byteBuf = ctx.alloc().buffer();
        // 魔数 写死 不用写在配置文件中
        byteBuf.writeBytes(MAGIC_NUM);
        // 版本控制号  由配置文件指定
        byteBuf.writeByte(1);
        // 指令类型
        byteBuf.writeByte(msg.getMessageType());
        // 序列化算法类型  由配置文件指定
        Serializer.Algorithm algorithm = RpcConfig.getSerializerAlgorithm();
        byteBuf.writeByte(algorithm.ordinal());
        // 指令序列号
        byteBuf.writeInt(msg.getSequenceId());
        // 无意义位
        byteBuf.writeByte(0);
        // 数据长度 先序列化
        byte[] bytes = algorithm.serialize(msg);
        byteBuf.writeInt(bytes.length);
        // 实际数据内容
        byteBuf.writeBytes(bytes);

        out.add(byteBuf);
    }
    /**
     * 解码
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
        // 魔数
        int magicNum = msg.readInt();
        // 版本控制号
        byte version = msg.readByte();
        // 指令类型
        byte messageType = msg.readByte();
        // 序列化算法类型
        byte serializeType = msg.readByte();
        // 指令序列号
        int sequenceId = msg.readInt();
        // 无意义位
        byte none = msg.readByte();
        // 数据长度
        int messageLength = msg.readInt();
        // 获取实际数据内容
        byte[] bytes = new byte[messageLength];
        msg.readBytes(bytes,0,messageLength);
        // 将消息反序列化
        Serializer.Algorithm algorithm = Serializer.Algorithm.values()[serializeType];
        Object message = algorithm.deSerialize(Message.getMessageClass(messageType), bytes);

        out.add(message);
    }
}
