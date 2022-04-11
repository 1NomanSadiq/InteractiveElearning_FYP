package edu.vu.ielearning.adapters.models;

public class NotesModel {
    String noteTitle;
    String noteContent;
    String date;
    int color;

    public NotesModel() {
    }

    public NotesModel(String noteTitle, String noteContent, String date, int color) {
        this.noteTitle = noteTitle;
        this.noteContent = noteContent;
        this.date = date;
        this.color = color;
    }

    public String getNoteTitle() {
        return noteTitle;
    }

    public void setNoteTitle(String noteTitle) {
        this.noteTitle = noteTitle;
    }

    public String getNoteContent() {
        return noteContent;
    }

    public void setNoteContent(String noteContent) {
        this.noteContent = noteContent;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}