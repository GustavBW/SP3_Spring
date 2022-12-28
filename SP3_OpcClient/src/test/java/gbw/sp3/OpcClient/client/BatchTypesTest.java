package gbw.sp3.OpcClient.client;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class BatchTypesTest {

    @BeforeAll
    static void setUp() {
        System.out.println("|||-----NOW TESTING: BatchTypes-----|||");
    }

    @AfterAll
    static void tearDown() {
        System.out.println("|||-----FINISHED TESTING: BatchTypes-----|||");
    }

    @Test
    void from() {
        System.out.println("\tBatchTypes.from()");
        BatchTypes[] types = BatchTypes.values();
        for(int i = 0; i < types.length; i++){
            assertEquals(BatchTypes.from(i), types[i]);
        }

        assertEquals(BatchTypes.from(-1), BatchTypes.UNKNOWN);
        assertEquals(BatchTypes.from(types.length +1), BatchTypes.UNKNOWN);
    }

    @Test
    void parse() {
        System.out.println("\tBatchTypes.parse()");
        String[] expectedParsableNames = new String[]{
                "pilsner", "wheat", "ipa", "stout", "ale", "alcohol free", "alcohol_free"
        };
        for(String name : expectedParsableNames){
            assertNotNull(BatchTypes.parse(name));
        }
        assertNull(BatchTypes.parse("this is not a type"));
        assertNull(BatchTypes.parse("this aint either"));
    }
}