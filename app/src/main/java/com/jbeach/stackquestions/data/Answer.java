package com.jbeach.stackquestions.data;

public class Answer {

    private String id;
    private String questionId;
    private String body;
    private String answeredBy;
    private String answeredAt;
    private int score;
    private boolean isAccepted;
    private boolean userDidVote;

    public Answer(String id, String body, String answeredBy, String answeredAt, boolean isAccepted, int score){
        this.id = id;
        this.body = body;
        this.answeredBy = answeredBy;
        this.answeredAt = answeredAt;
        this.isAccepted = isAccepted;
        this.score = score;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getAnsweredBy() {
        return answeredBy;
    }

    public void setAnsweredBy(String answeredBy) {
        this.answeredBy = answeredBy;
    }

    public String getAnsweredAt() {
        return answeredAt;
    }

    public void setAnsweredAt(String answeredAt) {
        this.answeredAt = answeredAt;
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    public void setAccepted(boolean accepted) {
        isAccepted = accepted;
    }

    public boolean isUserDidVote() {
        return userDidVote;
    }

    public void setUserDidVote(boolean userDidVote) {
        this.userDidVote = userDidVote;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }


}
