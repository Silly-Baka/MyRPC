package myRPC.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import myRPC.handler.RpcRequestHandler;
import myRPC.protocol.MessageCodec;
import myRPC.registry.impl.DefaultServiceProvider;
import myRPC.registry.impl.NacosServiceRegistry;

import java.net.InetSocketAddress;

/**
 * Date: 2022/7/7
 * Time: 12:22
 *
 * @Author SillyBaka
 * Description： Rpc服务端
 **/
@Slf4j
@NoArgsConstructor
public class RpcServer extends AbstractServer{
    private String host;
    private Integer port;

    public RpcServer(String host,Integer port){
        this.host = host;
        this.port = port;
        serviceProvider = new DefaultServiceProvider();
        serviceRegistry = new NacosServiceRegistry();
        scanServices(host,port);
    }
    @Override
    public void start(){
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();

        MessageCodec messageCodec = new MessageCodec();
        try {
            ChannelFuture channelFuture = new ServerBootstrap()
                    .group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch){
                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024,12,4,0,0));
                            ch.pipeline().addLast(new LoggingHandler());
                            ch.pipeline().addLast(messageCodec);
                            ch.pipeline().addLast(new RpcRequestHandler(serviceProvider));
                        }
                    })
                    .bind(new InetSocketAddress(host,port));
            log.debug("服务端已启动...");
            channelFuture.sync();
            Channel channel = channelFuture.channel();

            channel.closeFuture().addListener(future -> {
                log.debug("服务端已关闭...");
                NacosServiceRegistry.clearAllService(serviceProvider,new InetSocketAddress(host,port));
                boss.shutdownGracefully();
                worker.shutdownGracefully();
            });
        } catch (InterruptedException e) {
            log.debug("服务端发生异常..",e);
        }
    }
}
