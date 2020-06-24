package utilities;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class CombinationGeneratorTest {
    @Test
    public void basic() {
        ArrayList<Integer> prevLabels = new ArrayList<>();
        prevLabels.add(5);
        prevLabels.add(2);
        prevLabels.add(7);
        prevLabels.add(1);

        ArrayList<String> combs= CombinationGenerator.getNewCombinations(prevLabels, 4);
        assertEquals(15, combs.size());

    }
}
