package dataStructures;

import org.junit.Test;

public class ConnectedComponentTest {

    @Test
    public void basic() {
        ConnectedComponent connectedComponent = new ConnectedComponent(1, 11, 2, 11, 21);
        System.out.println(connectedComponent);
    }
}
