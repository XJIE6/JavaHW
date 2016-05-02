package sp;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;
import static sp.SecondPartTasks.*;

public class SecondPartTasksTest {

    @Test
    public void testFindQuotes() {
        List<String> files = Arrays.asList(
                "src/test/resources/file1.txt",
                "src/test/resources/file2.txt",
                "src/test/resources/file3.txt"
        );
        TreeSet<String> test1 = new TreeSet<>(Arrays.asList(
                "this is the first lane of file1",
                "first lane",
                "not first lane",
                "not first and second lane"
        ));
        TreeSet<String> test2 = new TreeSet<>(Arrays.asList(
                "second lane",
                "not second lane",
                "not first and second lane"
        ));
        TreeSet<String> test3 = new TreeSet<>();

        assertEquals(test1, new TreeSet<String>(findQuotes(files, "first")));
        assertEquals(test2, new TreeSet<String>(findQuotes(files, "second lane")));
        assertEquals(test3, new TreeSet<String>(findQuotes(files, "nothing")));
    }

    @Test
    public void testPiDividedBy4() {
        assertEquals(Math.PI / 4, piDividedBy4(), 0.01);
    }

    @Test
    public void testFindPrinter() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("AuthorOne", Arrays.asList("I'm author one and I hate writing.", "Seriously!"));
        map.put("AuthorTwo", Arrays.asList("I'm author two and I like writing a bit more then previous", "But I'm still hate it."));
        map.put("PacMan", Arrays.asList("Waka-waka-waka-waka-waka-waka-waka-waka-waka-waka-waka-waka-waka-waka-waka-waka-waka-waka-waka-waka-waka-waka-waka-waka-waka-waka-waka-waka-waka-waka"));

        assertEquals("PacMan", findPrinter(map));
    }

    @Test
    public void testCalculateGlobalOrder() {
        Map<String, Integer> authorOne = new HashMap<>();
        authorOne.put("pen", 1);
        authorOne.put("paper", 150);

        Map<String, Integer> authorTwo = new HashMap<>();
        authorTwo.put("pen", 2);
        authorTwo.put("paper", 300);
        authorTwo.put("pencil", 1);

        Map<String, Integer> pacMan = new HashMap<>();
        pacMan.put("pencil", 1);
        pacMan.put("cherry", 100500);

        Map<String, Integer> result = new HashMap<>();
        result.put("pen", 3);
        result.put("paper", 450);
        result.put("pencil", 2);
        result.put("cherry", 100500);

        assertEquals(result, calculateGlobalOrder(Arrays.asList(authorOne, authorTwo, pacMan)));
    }
}