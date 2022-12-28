package gbw.sp3.OpcClient.client;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductionStateTest {

    @BeforeAll
    static void startUp()
    {
        System.out.println("|||-----NOW TESTING: ProductionState-----|||");
    }

    @AfterAll
    static void tearDown()
    {
        System.out.println("|||-----FINISHED TESTING: ProductionState-----|||");
    }

    @Test
    void from() {
        ProductionState[] values = ProductionState.values();
        for(int i = 0; i < values.length; i++){
            assertNotNull(values[i]);
            assertEquals(values[i],ProductionState.from(i));
        }

        assertEquals( ProductionState.INVALID_STATE,ProductionState.from(values.length +1));
        assertEquals(ProductionState.INVALID_STATE, ProductionState.from(-1));
    }
}