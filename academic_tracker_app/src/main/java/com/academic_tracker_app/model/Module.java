package com.academic_tracker_app.model;

public class Module {
    private int id;
    private String moduleName;
    private int credits;
    private double mark;

    public Module(String moduleName, int credits, double mark) {
        this.moduleName = moduleName;
        this.credits = credits;
        this.mark = mark;
    }

    /// I don't think one needs to get or set ID, but i'll keep it for now

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
