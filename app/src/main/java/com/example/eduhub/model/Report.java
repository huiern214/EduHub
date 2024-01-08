package com.example.eduhub.model;

import com.google.firebase.Timestamp;

public class Report {
    String report_id, reportDetails, report_type, resource_id, user_id;
    Timestamp report_timestamp;

    public Report(String report_id, String reportDetails, Timestamp report_timestamp, String resource_id, String user_id) {
        this.report_id = report_id;
        this.reportDetails = reportDetails;
        this.report_timestamp = report_timestamp;
        this.report_type = "notes";
        this.resource_id = resource_id;
        this.user_id = user_id;
    }

    public Report(){

    }

    public String getReport_id(){
        return report_id;
    }

    public void setReport_id(String reportId){
        this.report_id = reportId;
    }

    public String getReportDetails() {
        return reportDetails;
    }

    public void setReportDetails(String reportDetails) {
        this.reportDetails = reportDetails;
    }

    public Timestamp getReport_timestamp() {
        return report_timestamp;
    }

    public void setReport_timestamp(Timestamp report_timestamp) {
        this.report_timestamp = report_timestamp;
    }

    public String getReport_type() {
        return report_type;
    }

    public void setReport_type(String report_type) {
        this.report_type = report_type;
    }

    public String getResource_id() {
        return resource_id;
    }

    public void setResource_id(String resource_id) {
        this.resource_id = resource_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}