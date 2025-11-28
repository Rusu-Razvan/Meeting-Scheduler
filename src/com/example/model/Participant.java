package com.example.model;

/**
 * Represents a user’s participation in an appointment, including RSVP status and comment.
 */
public class Participant {
    private int appointmentId;
    private int userId;
    private String status;   // INVITED, ACCEPTED, DECLINED
    private String comment;

    public Participant() {
        // default constructor
    }

    public Participant(int appointmentId, int userId, String status, String comment) {
        this.appointmentId = appointmentId;
        this.userId = userId;
        this.status = status;
        this.comment = comment;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    /** 
     * Set the RSVP status. 
     * @param status one of "INVITED", "ACCEPTED", "DECLINED"
     */
    public void setStatus(String status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    /** 
     * Set the participant’s comment (can be empty or null). 
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "Participant{" +
               "appointmentId=" + appointmentId +
               ", userId=" + userId +
               ", status='" + status + '\'' +
               ", comment='" + comment + '\'' +
               '}';
    }
}
