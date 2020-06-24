package utilities;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class StopWatchTest {
    @Test
    public void basic() throws InterruptedException {
        StopWatch s = new StopWatch();
        s.start();
        //code you want to time goes here
        Thread.sleep(1);
        s.stop();
        assertTrue(s.getElapsedTime()>0);
    }
}
