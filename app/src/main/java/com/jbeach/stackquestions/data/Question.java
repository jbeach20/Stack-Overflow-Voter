package com.jbeach.stackquestions.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "questions")
public class Question {

    @PrimaryKey(autoGenerate = true)
    private int uid;

    @ColumnInfo(name = "id")
    private int id;
    @ColumnInfo(name = "title")
    private String title;
    @ColumnInfo(name = "asked_by")
    private String askedBy;
    @ColumnInfo(name = "asked_at")
    private String askedAt;
    @ColumnInfo(name = "answer_count")
    private int answerCount;
    @ColumnInfo(name = "voted_answer_id")
    private String votedAnswerId;
    @ColumnInfo(name = "correct_answer_id")
    private String correctAnswerId;
    @ColumnInfo(name = "user_was_right")
    private boolean userWasRight;
    @ColumnInfo(name = "created_at")
    private String createdAt;


    public Question(int id, String title, String askedBy, String askedAt, int answerCount){
        this.id = id;
        this.title = title;
        this.askedAt = askedAt;
        this.askedBy = askedBy;
        this.answerCount = answerCount;
    }

//    public Question(int id){
//        this.id = id;
//        this.title = title;
//        this.askedAt = askedAt;
//        this.askedBy = askedBy;
//        this.answerCount = answerCount;
//    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAskedBy() {
        return askedBy;
    }

    public void setAskedBy(String askedBy) {
        this.askedBy = askedBy;
    }

    public String getAskedAt() {
        return askedAt;
    }

    public void setAskedAt(String askedAt) {
        this.askedAt = askedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAnswerCount() {
        return answerCount;
    }

    public void setAnswerCount(int answerCount) {
        this.answerCount = answerCount;
    }
    public String getVotedAnswerId() {
        return votedAnswerId;
    }

    public void setVotedAnswerId(String votedAnswerId) {
        this.votedAnswerId = votedAnswerId;
    }

    public boolean isUserWasRight() {
        return userWasRight;
    }

    public void setUserWasRight(boolean userWasRight) {
        this.userWasRight = userWasRight;
    }
    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getCorrectAnswerId() {
        return correctAnswerId;
    }

    public void setCorrectAnswerId(String correctAnswerId) {
        this.correctAnswerId = correctAnswerId;
    }

}
