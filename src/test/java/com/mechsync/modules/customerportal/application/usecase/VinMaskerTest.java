package com.mechsync.modules.customerportal.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class VinMaskerTest {

    @Test
    void keepsOnlyLastFourCharactersVisible() {
        assertEquals("*************1234", VinMasker.mask("1HGCM82633A001234"));
    }

    @Test
    void masksShortVinCompletely() {
        assertEquals("****", VinMasker.mask("1234"));
    }

    @Test
    void nullOrBlankVinRemainsAbsent() {
        assertNull(VinMasker.mask(null));
        assertNull(VinMasker.mask("   "));
    }
}
