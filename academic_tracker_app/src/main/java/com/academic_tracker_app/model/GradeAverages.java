package com.academic_tracker_app.model;

import java.util.Collection;

public final class GradeAverages {
    private GradeAverages() { }

    public record Summary(int moduleCount, long credits, double weightedTotal) {
        public double average() { return credits > 0 ? weightedTotal / credits : 0; }
    }

    public static Summary summarize(Collection<Module> modules, boolean finalYearOnly) {
        int count = 0;
        long credits = 0;
        double weightedTotal = 0;
        for (Module module : modules) {
            if (finalYearOnly && !module.isFinalYear()) continue;
            count++;
            credits += module.getCredits();
            weightedTotal += module.getCredits() * module.getMark();
        }
        return new Summary(count, credits, weightedTotal);
    }
}
