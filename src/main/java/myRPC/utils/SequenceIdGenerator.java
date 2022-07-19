package myRPC.utils;

import java.util.BitSet;
import java.util.Random;

/**
 * Date: 2022/7/8
 * Time: 15:00
 *
 * @Author SillyBaka
 * Description：
 **/
public class SequenceIdGenerator {
    public static final BitSet RANDOM_MAP = new BitSet();
    private static final Random RANDOM = new Random();

    public static Integer getSequenceId(){
        int nextInt;
        do {
            nextInt = RANDOM.nextInt(Integer.MAX_VALUE);
        }while(RANDOM_MAP.get(nextInt));
        RANDOM_MAP.set(nextInt);

        return nextInt;
    }
}
