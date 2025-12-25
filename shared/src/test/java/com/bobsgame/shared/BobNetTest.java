package com.bobsgame.shared;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.bobsgame.net.BobNet;

class BobNetTest {

    @Test
    void testConstants() {
        assertNotNull(BobNet.releaseServerAddress);
        assertTrue(BobNet.serverTCPPort > 0);
        assertTrue(BobNet.STUNServerUDPPort > 0);
        assertTrue(BobNet.INDEXServerTCPPort > 0);
    }
}
