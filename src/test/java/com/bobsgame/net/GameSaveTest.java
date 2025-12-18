package com.bobsgame.net;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameSaveTest {

    @Test
    void testGetCountryCodeFromCountryString() {
        assertEquals("US", GameSave.getCountryCodeFromCountryString("United States"));
        assertEquals("CA", GameSave.getCountryCodeFromCountryString("Canada"));
        assertEquals("GB", GameSave.getCountryCodeFromCountryString("United Kingdom"));
        assertEquals("JP", GameSave.getCountryCodeFromCountryString("Japan"));
        // Test a default case
        assertEquals("US", GameSave.getCountryCodeFromCountryString("Non Existent Country"));
    }

    @Test
    void testGetCountryStringFromCode() {
        assertEquals("United States", GameSave.getCountryStringFromCode("US"));
        assertEquals("Canada", GameSave.getCountryStringFromCode("CA"));
        assertEquals("United Kingdom", GameSave.getCountryStringFromCode("GB"));
        assertEquals("Japan", GameSave.getCountryStringFromCode("JP"));
        // Test a default case
        assertEquals("United States", GameSave.getCountryStringFromCode("XX"));
    }
}
