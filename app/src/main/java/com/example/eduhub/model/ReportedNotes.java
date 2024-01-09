package com.example.eduhub.model;

public class ReportedNotes {
    String notes_id;
    int total_report_cases;
    int pending_report_cases;

    public ReportedNotes(String notes_id, int total_report_cases, int pending_report_cases) {
        this.notes_id = notes_id;
        this.total_report_cases = total_report_cases;
        this.pending_report_cases = pending_report_cases;
    }

    public String getNotes_id() {
        return notes_id;
    }

    public void setNotes_id(String notes_id) {
        this.notes_id = notes_id;
    }

    public int getTotal_report_cases() {
        return total_report_cases;
    }

    public void setTotal_report_cases(int total_report_cases) {
        this.total_report_cases = total_report_cases;
    }

    public int getPending_report_cases() {
        return pending_report_cases;
    }

    public void setPending_report_cases(int pending_report_cases) {
        this.pending_report_cases = pending_report_cases;
    }
}
