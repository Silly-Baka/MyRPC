package myRPC.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import myRPC.handler.RpcResponseHandler;
import myRPC.protocol.MessageCodec;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Date: 2022/7/7
 * Time: 12:21
 *
 * @Author SillyBaka
 * Description： Rpc客户端 目的在于创建一个与服务端连接的channel 便于远程调用
 **/
@Data
@Slf4j
public class RpcClient {
    /**
     * 维护当前客户端与服务器的连接对象channel
     * InetSocketAddress是目标服务器的地址
     * Channel是连接对象
     */
    private static final Map<InetSocketAddress,Channel> CHANNEL_MAP = new ConcurrentHashMap<>();
    /**
     * 锁 用于双重校验
     */
    private static final Object LOCK = new Object();
    /**
     * 获取当前客户端与目标地址的连接对象
     *
     * 该Channel对象可重用，避免重复连接
     * 使用双重校验锁 保证不出现重复连接
     */
    public static Channel getChannel(InetSocketAddress address){
        Channel channel = CHANNEL_MAP.get(address);
        if(channel != null){
            return channel;
        }else {
            synchronized (LOCK){
                channel = CHANNEL_MAP.get(address);
                if(channel == null){
                    channel = initChannel(address);
                }
                return channel;
            }
        }
    }
    /**
     * 初始化channel 将客户端连接上目标服务器
     * @param address 目标服务器的地址
     * @return 连接对象channel
     */
    private static Channel initChannel(InetSocketAddress address){

        NioEventLoopGroup worker = new NioEventLoopGroup();
        RpcResponseHandler rpcResponseHandler = new RpcResponseHandler();

        MessageCodec messageCodec = new MessageCodec();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(worker);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch){
                ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024,12,4,0,0));
                ch.pipeline().addLast(new LoggingHandler());
                ch.pipeline().addLast(messageCodec);
                ch.pipeline().addLast(rpcResponseHandler);
            }
        });
        try {
            Channel channel = bootstrap.connect(address).sync().channel();
            log.debug("客户端已连接上服务端{}", address);
            // 放入容器中
            CHANNEL_MAP.put(address,channel);
            // 添加监听器 若是channel被关闭则触发
            channel.closeFuture().addListener(future -> {
                worker.shutdownGracefully();
                log.debug("客户端已正常关闭...");
                // 从容器中注销
                CHANNEL_MAP.values().remove(channel);
            });
            return channel;
        } catch (InterruptedException e) {
            throw new RuntimeException("客户端启动失败！",e);
        }
    }
}
