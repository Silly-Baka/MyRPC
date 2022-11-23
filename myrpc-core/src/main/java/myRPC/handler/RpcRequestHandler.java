package myRPC.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import myRPC.protocol.RpcRequestMessage;
import myRPC.protocol.RpcResponseMessage;
import myRPC.registry.ServiceProvider;
import myRPC.protocol.RpcMessageUtil;

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

                String interfaceName = msg.getServiceName()+":"+msg.getMethodName();
                log.info("异步线程池正在处理请求:{} , 请求的接口为：{}",msg.getSequenceId(),interfaceName);

                boolean retry = msg.isRetry();
                long timeOut = msg.getTimeOut();
                int ttl = msg.getTtl();

                long startTime = System.currentTimeMillis();

                RpcResponseMessage responseMessage;
                Object result;
                try {
                    String serviceName = msg.getServiceName();
                    // 从本地注册表中获取服务接口
                    Object service = serviceProvider.getService(serviceName);
                    // 通过反射调用方法
                    Method method = service.getClass().getDeclaredMethod(msg.getMethodName(), msg.getParameterTypes());

                    result = method.invoke(service, msg.getParameters());

                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException e) {
                    // 判断是否允许重试 并且 是否还有重试次数
                    if(retry && ttl > 0){
                        // 返回重试响应
                        responseMessage = RpcMessageUtil.retry(msg,"重试");

                    }else{
                        // 返回失败响应
                        responseMessage = RpcMessageUtil.fail("执行出现异常",e,msg.getSequenceId());
                    }
                    ctx.writeAndFlush(responseMessage);
                    return;
                }
                // 判断处理完业务后是否已超时
                if(System.currentTimeMillis()-startTime > timeOut){
                    // 是否允许重试
                    if(retry && msg.getTtl() > 0){
                        // 允许则发送重试消息
                        responseMessage = RpcMessageUtil.retry(msg,"重试");
                    }else{
                    // 否则 返回失败消息
                        responseMessage = RpcMessageUtil.fail("超时失败",
                                new RuntimeException("接口: {"+interfaceName+"} 处理超时"),msg.getSequenceId());
                    }
                }else{
                    // 未超时则封装成响应信息 然后发回给客户端
                    responseMessage = RpcMessageUtil.success(result,msg.getSequenceId());
                }

                ctx.writeAndFlush(responseMessage);

                log.info("请求:{} 处理完毕，已返回结果给调用方",msg.getSequenceId());
            }
        });
    }
}
