import myRPC.protocol.RpcRequestMessage;
import myRPC.protocol.Serializer;

/**
 * Date: 2022/7/8
 * Time: 16:37
 *
 * @Author SillyBaka
 * Description：
 **/
public class testSerilizer {
    public static void main(String[] args) {
        RpcRequestMessage requestMessage = new RpcRequestMessage("niubi", "666", new Class[]{testMessageCodec.class}, new Object[]{testMessageCodec.class});
        byte[] serialize = Serializer.Algorithm.Json.serialize(requestMessage);

        RpcRequestMessage message = Serializer.Algorithm.Json.deSerialize(RpcRequestMessage.class, serialize);
        System.out.println(message);
    }
}
