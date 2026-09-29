package com.academic_tracker_app.model;

public class Module {
    private int id;
    private String moduleName;
    private int credits;
    private double mark;
    private boolean finalYear;

    public Module(String moduleName, int credits, double mark) {
        this.moduleName = moduleName;
        this.credits = credits;
        this.mark = mark;
    }

    // Database identity ensures updates and deletes target exactly one row.

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public double getMark() {
        return mark;
    }

    public void setMark(double mark) {
        this.mark = mark;
    }

    public boolean isFinalYear() { return finalYear; }

    public void setFinalYear(boolean finalYear) { this.finalYear = finalYear; }

    @Override
    public String toString() {
        return "Module{" +
                "id=" + id +
                ", moduleName='" + moduleName + '\'' +
                ", credits=" + credits +
                ", mark=" + mark +
                '}';
    }
}
