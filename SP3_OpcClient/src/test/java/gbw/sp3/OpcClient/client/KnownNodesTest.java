package gbw.sp3.OpcClient.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KnownNodesTest {

    @Test
    void parse() {

        assertEquals(0, KnownNodes.parseList(new String[0]).size());


    }
}