package com.academic_tracker_app.model;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class GradeAveragesTest {
    @Test void weightsByCreditsAndIncludesOnlyFinalYearModulesRegardlessOfOrder() {
        Module finance = new Module("Financial Management", 24, 80);
        finance.setFinalYear(true);
        Module project = new Module("Project", 6, 40);
        project.setFinalYear(true);
        Module earlier = new Module("Earlier year", 30, 100);
        var modules = new ArrayList<>(List.of(finance, earlier, project));
        var finalYear = GradeAverages.summarize(modules, true);
        assertEquals(2, finalYear.moduleCount());
        assertEquals(30, finalYear.credits());
        assertEquals(72, finalYear.average(), 0.00001);
        assertEquals(86, GradeAverages.summarize(modules, false).average(), 0.00001);
        Collections.reverse(modules);
        assertEquals(finalYear, GradeAverages.summarize(modules, true));
        project.setMark(100);
        assertEquals(84, GradeAverages.summarize(modules, true).average(), 0.00001);
    }

    @Test void emptySelectionIsNotANonFiniteAverage() {
        var summary = GradeAverages.summarize(List.of(new Module("Earlier", 12, 80)), true);
        assertEquals(0, summary.moduleCount());
        assertEquals(0, summary.credits());
        assertEquals(0, summary.average());
    }
}
