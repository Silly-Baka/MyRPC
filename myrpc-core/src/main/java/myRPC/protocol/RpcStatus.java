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
    FAIL("",401);

    private String message;

    private Integer code;

    RpcStatus(String message, Integer code) {
        this.message = message;
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
