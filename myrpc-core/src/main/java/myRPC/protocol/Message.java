package myRPC.protocol;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Date: 2022/7/8
 * Time: 15:21
 *
 * @Author SillyBaka
 * Description：消息默认实现类
 **/
@Data
public abstract class Message {
    /**
     * 序列号
     */
    protected int sequenceId;
    /**
     * 指令类型
     */
    protected int messageType;

    public static final int RPC_REQUEST_MESSAGE = 0;
    public static final int RPC_RESPONSE_MESSAGE = 1;

    private static final Map<Integer,Class<?>> MESSAGE_CLASSES = new HashMap<>();

    static {
        MESSAGE_CLASSES.put(RPC_REQUEST_MESSAGE, RpcRequestMessage.class);
        MESSAGE_CLASSES.put(RPC_RESPONSE_MESSAGE,RpcResponseMessage.class);
    }

    /**
     * 根据消息类型参数获取消息的类
     * @param messageType 消息类型参数
     * @return 消息的类
     */
    public static Class<?> getMessageClass(int messageType){
        if(!MESSAGE_CLASSES.containsKey(messageType)){
            throw new RuntimeException("不存在该类型的消息");
        }
        return MESSAGE_CLASSES.get(messageType);
    }

    /**
     * 获取当前消息类型
     * 由子类实现
     */
    public abstract int getMessageType();
}
