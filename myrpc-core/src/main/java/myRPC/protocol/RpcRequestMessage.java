package myRPC.protocol;

import lombok.*;

import java.io.Serializable;

/**
 * Date: 2022/7/7
 * Time: 12:20
 *
 * @Author SillyBaka
 * Description： Rpc请求对象
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RpcRequestMessage extends Message implements Serializable {
    /**
     * 接口名字
     */
    private String serviceName;
    /**
     * 调用的方法名
     */
    private String methodName;
    /**
     * 参数类型
     */
    private Class<?>[] parameterTypes;
    /**
     * 参数列表
     */
    private Object[] parameters;
    /**
     * 是否允许重试 默认不允许
     * 只有读接口允许重试
     * 写接口不能允许重试
     */
    protected boolean isRetry = true;
    /**
     * 超时时间 默认1000ms
     */
    protected long timeOut = 1000;
    /**
     * 重试次数 默认3次
     */
    protected int ttl = 3;


    @Override
    public int getMessageType() {
        return RPC_REQUEST_MESSAGE;
    }

}
