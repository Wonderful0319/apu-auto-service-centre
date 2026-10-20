package asc.model;

public class Feedback {
    private String feedbackId;
    private String appointmentId;
    private String technicianId;
    private String content;
    private String feedbackDate;

    public Feedback(String feedbackId, String appointmentId, String technicianId, String content, String feedbackDate) {
        this.feedbackId = feedbackId;
        this.appointmentId = appointmentId;
        this.technicianId = technicianId;
        this.content = content;
        this.feedbackDate = feedbackDate;
    }

    public String getFeedbackId() { return feedbackId; }
    public String getAppointmentId() { return appointmentId; }
    public String getTechnicianId() { return technicianId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getFeedbackDate() { return feedbackDate; }
    public void setFeedbackDate(String date) { this.feedbackDate = date; }

    public String toFileString() {
        return feedbackId + "|" + appointmentId + "|" + technicianId + "|" + content + "|" + feedbackDate;
    }
}
