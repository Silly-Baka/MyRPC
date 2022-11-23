package myRPC.protocol;

import lombok.Data;
import org.ietf.jgss.Oid;

import java.io.Serializable;

/**
 * Date: 2022/7/7
 * Time: 12:21
 *
 * @Author SillyBaka
 * Description： Rpc响应对象
 **/
@Data
public class RpcResponseMessage extends Message implements Serializable {
    /**
     * 响应状态
     */
    private RpcStatus status;
    /**
     * 响应消息
     */
    private String message;
    /**
     * 响应数据
     */
    private Object returnValue;
//    /**
//     * 异常信息
//     */
//    private Exception exceptionValue;

    @Override
    public int getMessageType() {
        return RPC_RESPONSE_MESSAGE;
    }
}
