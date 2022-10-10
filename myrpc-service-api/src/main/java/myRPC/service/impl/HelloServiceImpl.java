package myRPC.service.impl;

import myRPC.annotation.Service;
import myRPC.service.HelloService;

/**
 * Date: 2022/7/7
 * Time: 18:26
 *
 * @Author SillyBaka
 * Description：
 **/
@Service
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String name) {
        return "你好"+name+"远程调用成功！！";
    }
}
