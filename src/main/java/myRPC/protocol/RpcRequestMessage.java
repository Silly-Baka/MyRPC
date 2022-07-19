package myRPC.protocol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String interfaceName;
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


    @Override
    public int getMessageType() {
        return RPC_REQUEST_MESSAGE;
    }
}
