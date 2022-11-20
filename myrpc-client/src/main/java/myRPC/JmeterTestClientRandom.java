package myRPC;

import myRPC.client.RpcClientProxyFactory;
import myRPC.service.HelloService;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

/**
 * Description：
 * <p>Date: 2022/11/15
 * <p>Time: 20:47
 *
 * @Author SillyBaka
 **/
public class JmeterTestClientRandom implements JavaSamplerClient {

    private HelloService helloService;

    @Override
    public void setupTest(JavaSamplerContext context) {
        RpcClientProxyFactory rpcClientProxyFactory = new RpcClientProxyFactory();
        helloService = rpcClientProxyFactory.getServiceProxy(HelloService.class);

        System.out.println("测试准备开始");
    }

    @Override
    public SampleResult runTest(JavaSamplerContext context) {

        SampleResult sampleResult = new SampleResult();

        sampleResult.sampleStart();
        try {
            String result = helloService.hello("测试TPS");
            sampleResult.setResponseData(result,"utf-8");
            sampleResult.setDataType(SampleResult.TEXT);
            sampleResult.setSuccessful(true);

        } catch (RuntimeException e){
            sampleResult.setSuccessful(false);
        }finally {
            sampleResult.sampleEnd();
        }

        return sampleResult;
    }

    @Override
    public void teardownTest(JavaSamplerContext context) {
        System.out.println("测试结束");
    }

    @Override
    public Arguments getDefaultParameters() {
        Arguments arguments = new Arguments();
        return arguments;
    }
}
