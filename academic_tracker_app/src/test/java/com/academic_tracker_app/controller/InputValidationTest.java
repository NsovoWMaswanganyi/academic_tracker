package com.academic_tracker_app.controller;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InputValidationTest {
    @Test void acceptsTrimmedValuesAndBoundaryMarks() {
        var module = MainController.readInput(" Statistics ", " 12 ", " 95.5 ");
        assertEquals("Statistics", module.getModuleName());
        assertEquals(12, module.getCredits());
        assertEquals(95.5, module.getMark());
        assertEquals(0, MainController.readInput("A", "1", "0").getMark());
        assertEquals(100, MainController.readInput("A", "1", "100").getMark());
    }

    @Test void rejectsInvalidNamesCreditsAndMarks() {
        assertThrows(IllegalArgumentException.class, () -> MainController.readInput(" ", "12", "50"));
        for (String credits : new String[]{"0", "-12", "1.5", "", "2147483648"})
            assertThrows(IllegalArgumentException.class, () -> MainController.readInput("A", credits, "50"));
        for (String mark : new String[]{"-1", "101", "NaN", "Infinity", "", "abc"})
            assertThrows(IllegalArgumentException.class, () -> MainController.readInput("A", "12", mark));
    }
}
