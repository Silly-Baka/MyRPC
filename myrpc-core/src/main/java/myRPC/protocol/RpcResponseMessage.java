package myRPC.protocol;

import lombok.Data;

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
     * 响应码
     */
    private Integer status;
    /**
     * 响应消息
     */
    private String message;
    /**
     * 响应数据
     */
    private Object returnValue;
    /**
     * 异常信息
     */
    private Exception exceptionValue;

    public static <T> RpcResponseMessage success(T data,Integer sequenceId){
        RpcResponseMessage rpcResponseMessage = new RpcResponseMessage();
        rpcResponseMessage.setReturnValue(data);
        rpcResponseMessage.setStatus(RpcStatus.SUCCESS.getCode());
        rpcResponseMessage.setSequenceId(sequenceId);

        return rpcResponseMessage;
    }
    public static RpcResponseMessage fail(String message,Exception e){
        RpcResponseMessage rpcResponseMessage = new RpcResponseMessage();
        rpcResponseMessage.setMessage(message);
        rpcResponseMessage.setStatus(RpcStatus.FAIL.getCode());
        rpcResponseMessage.setExceptionValue(e);

        return rpcResponseMessage;
    }

    @Override
    public int getMessageType() {
        return RPC_RESPONSE_MESSAGE;
    }
}
