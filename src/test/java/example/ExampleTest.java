package example;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(BrokenRunner.class)
public class ExampleTest {

    @Test
    public void testJoin() {
        assertEquals("Joined string didn't match", "1 2 3", Example.join("1", "2", "3"));
    }

}
