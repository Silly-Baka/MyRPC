package myRPC.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;
import myRPC.protocol.RpcResponseMessage;
import myRPC.protocol.RpcStatus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Date: 2022/7/7
 * Time: 14:49
 *
 * @Author SillyBaka
 * Description：处理Rpc响应的处理器 用于客户端
 **/
@ChannelHandler.Sharable
public class RpcResponseHandler extends SimpleChannelInboundHandler<RpcResponseMessage> {
    /**
     * 用于线程通讯的集合 存放调用结果
     */
    public static Map<Integer, Promise<Object>> PROMISES = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponseMessage msg) throws Exception {
        // 由当前 nio线程 接收调用方法后的响应信息
        // 通过序列号找到用于存放结果的 Promise
        Integer sequenceId = msg.getSequenceId();
        Promise<Object> promise = PROMISES.get(sequenceId);
        // 通过Promise进行线程通信 传回给发起调用的线程
        if(promise != null){
            Object returnValue = msg.getReturnValue();
            Integer status = msg.getStatus();
            // 如果调用成功 就保存返回值
            if(status.equals(RpcStatus.SUCCESS.getCode())){
                promise.setSuccess(returnValue);
            }else {
                promise.setFailure(msg.getExceptionValue());
            }
        }
    }
}
