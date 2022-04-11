package edu.vu.ielearning.adapters.models;

public class DiscussionBoardQuestionModel {

    String student_id;
    String question;
    String question_subject;
    String asked_at;
    String answer;

    public DiscussionBoardQuestionModel() {
    }

    public DiscussionBoardQuestionModel(String student_id, String question, String asked_at, String answer, String question_subject) {
        this.student_id = student_id;
        this.question = question;
        this.asked_at = asked_at;
        this.answer = answer;
        this.question_subject = question_subject;
    }

    public String getStudent_id() {
        return student_id;
    }

    public void setStudent_id(String student_id) {
        this.student_id = student_id;
    }

    public String getQuestion_subject() {
        return question_subject;
    }

    public void setQuestion_subject(String question_subject) {
        this.question_subject = question_subject;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAsked_at() {
        return asked_at;
    }

    public void setAsked_at(String asked_at) {
        this.asked_at = asked_at;
    }


    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

}
