package myRPC.service.impl;

import myRPC.annotation.Service;
import myRPC.service.RetryService;

/**
 * Description：
 * <p>Date: 2022/11/23
 * <p>Time: 14:36
 *
 * @Author SillyBaka
 **/
@Service
public class RetryServiceImpl implements RetryService {
    @Override
    public String retry() {
        System.out.println("开始测试重试");
//        int i = 10/0;
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("下一次重试");

        return "你已经超时了";
    }
}
