package neu.lab.tool.test;


import org.junit.Test;

import java.util.Arrays;

public class StringTest {

    @Test
    public void testSplit() {
        System.out.println(Arrays.asList("1@".split("@")));
    }
}
