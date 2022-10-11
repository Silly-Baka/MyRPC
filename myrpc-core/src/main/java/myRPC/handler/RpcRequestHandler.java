package myRPC.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import myRPC.protocol.RpcRequestMessage;
import myRPC.protocol.RpcResponseMessage;
import myRPC.registry.ServiceProvider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Date: 2022/7/7
 * Time: 14:49
 *
 * @Author SillyBaka
 *
 * Description：处理Rpc请求的处理器 用于服务端
 **/
@Slf4j
public class RpcRequestHandler extends SimpleChannelInboundHandler<RpcRequestMessage> {

    private final ServiceProvider serviceProvider;

    private static final EventExecutorGroup EXECUTOR_GROUP = new DefaultEventExecutorGroup(16);

    public RpcRequestHandler(ServiceProvider serviceProvider){
        this.serviceProvider = serviceProvider;
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequestMessage msg) throws Exception {
        //todo 将任务提交给异步线程池
        EXECUTOR_GROUP.submit(new Runnable() {
            @Override
            public void run() {

                log.info("异步线程池正在处理请求:{} , 请求的接口为：{}",msg.getSequenceId(),msg.getInterfaceName()+":"+msg.getMethodName());

                RpcResponseMessage responseMessage;
                Object result;
                try {
                    String interfaceName = msg.getInterfaceName();
                    // 从本地注册表中获取服务接口
                    Object service = serviceProvider.getService(interfaceName);
                    // 通过反射调用方法
                    Method method = service.getClass().getDeclaredMethod(msg.getMethodName(), msg.getParameterTypes());

                    result = method.invoke(service, msg.getParameters());

                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException e) {
                    responseMessage = RpcResponseMessage.fail("服务端调用方法时出现异常",e);
                    ctx.writeAndFlush(responseMessage);
                    return;
                }
                // 封装成响应信息 然后发回给客户端
                responseMessage = RpcResponseMessage.success(result,msg.getSequenceId());
                ctx.writeAndFlush(responseMessage);

                log.info("请求:{} 处理完毕，已返回结果给调用方",msg.getSequenceId());
            }
        });
    }
}
