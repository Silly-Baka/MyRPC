package myRPC.protocol;

import myRPC.protocol.Message;
import myRPC.protocol.RpcResponseMessage;
import myRPC.protocol.RpcStatus;

/**
 * Description：
 * <p>Date: 2022/11/23
 * <p>Time: 14:43
 *
 * @Author SillyBaka
 **/
public class RpcMessageUtil {
    public static <T> RpcResponseMessage success(T data, Integer sequenceId){
        RpcResponseMessage rpcResponseMessage = new RpcResponseMessage();
        rpcResponseMessage.setReturnValue(data);
        rpcResponseMessage.setStatus(RpcStatus.SUCCESS);
        rpcResponseMessage.setSequenceId(sequenceId);

        return rpcResponseMessage;
    }
    public static RpcResponseMessage fail(String message,Exception e,Integer sequenceId){
        RpcResponseMessage rpcResponseMessage = new RpcResponseMessage();
        rpcResponseMessage.setMessage(message);
        rpcResponseMessage.setStatus(RpcStatus.FAIL);
//        rpcResponseMessage.setExceptionValue(e);

        rpcResponseMessage.setSequenceId(sequenceId);

        return rpcResponseMessage;
    }
    public static RpcResponseMessage retry(RpcRequestMessage originMsg, String message){
        RpcResponseMessage rpcResponseMessage = new RpcResponseMessage();

        rpcResponseMessage.setMessage(message);
        rpcResponseMessage.setStatus(RpcStatus.RETRY);
        rpcResponseMessage.setSequenceId(originMsg.getSequenceId());

        originMsg.setTtl(originMsg.getTtl()-1);
        rpcResponseMessage.setReturnValue(originMsg);

        return rpcResponseMessage;
    }
}
