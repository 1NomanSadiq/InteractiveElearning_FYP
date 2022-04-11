package edu.vu.ielearning.adapters.models;

public class StudentRepliesModel {

    String replied_at;
    String reply;
    String student_id;

    public StudentRepliesModel() {
    }

    public StudentRepliesModel(String replied_at, String reply, String student_id) {
        this.replied_at = replied_at;
        this.reply = reply;
        this.student_id = student_id;
    }

    public String getReplied_at() {
        return replied_at;
    }

    public void setReplied_at(String replied_at) {
        this.replied_at = replied_at;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getStudent_id() {
        return student_id;
    }

    public void setStudent_id(String student_id) {
        this.student_id = student_id;
    }
}
