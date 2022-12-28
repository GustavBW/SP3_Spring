package gbw.sp3.OpcClient.client;

import gbw.sp3.OpcClient.util.ArrayUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class KnownNodesTest {

    @BeforeAll
    static void startUp()
    {
        System.out.println("|||-----NOW TESTING: KnownNodes-----|||");
    }

    @AfterAll
    static void tearDown()
    {
        System.out.println("|||-----FINISHED TESTING: KnownNodes-----|||");
    }

    @Test
    void getValidNames() {
        System.out.println("\tKnownNodes.getValidNames()");
        String[] validNames = KnownNodes.getValidNames();
        //Specifically testing that the object is the same since its a static instance.
        assertEquals(validNames, KnownNodes.getValidNames());

        assertNotNull(KnownNodes.getValidNames());
        assertTrue(KnownNodes.getValidNames().length > 1);

        for(String nodeName: validNames){
            assertFalse(nodeName.isEmpty());
            assertFalse(nodeName.isBlank());
            assertTrue(nodeName.length() > 3);
        }
    }

    @Test
    void testParse() {
        System.out.println("\tKnownNodes.parse()");
        String[] validNames = KnownNodes.getValidNames();
        for(String name: validNames){
            assertNotNull(KnownNodes.parse(name));
            assertEquals(name, Objects.requireNonNull(KnownNodes.parse(name)).displayName);
        }
        assertNull(KnownNodes.parse("This is not a node name"));
        assertNull(KnownNodes.parse("This aint either"));
    }

    @Test
    void parseList() {
        System.out.println("\tKnownNodes.parseList()");
        List<String> validNames = Arrays.stream(KnownNodes.getValidNames()).toList();
        for(int i = 0; i < validNames.size(); i++){
            List<String> sublist = validNames.subList(0,i);
            assertNotNull(sublist);
            assertEquals(sublist.size(), i);
        }

        String[] excludingDuplicates = new String[]{"SetCommand","SetCommand", "SetSpeed", "Barley"};
        List<KnownNodes> parsed = KnownNodes.parseList(excludingDuplicates);
        assertEquals(parsed.size(), excludingDuplicates.length - 1);
        for(KnownNodes node : parsed){
            assertNotNull(node);
            assertTrue(ArrayUtil.contains(excludingDuplicates,node.displayName));
        }
    }
}