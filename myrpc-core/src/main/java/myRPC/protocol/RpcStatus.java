package myRPC.protocol;

/**
 * Date: 2022/7/7
 * Time: 12:59
 *
 * @Author SillyBaka
 * Description：Rpc响应码
 **/
public enum RpcStatus {
    /**
     * 成功响应码
     */
    SUCCESS("成功",200),
    /**
     * 失败响应码
     */
    FAIL("",401),
    /**
     * 重试状态码
     */
    RETRY("重试",300);

    private String message;

    private int code;

    RpcStatus(String message, int code) {
        this.message = message;
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
