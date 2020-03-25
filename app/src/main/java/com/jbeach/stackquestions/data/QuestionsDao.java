package com.jbeach.stackquestions.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.jbeach.stackquestions.data.Question;

import java.util.List;

@Dao
public interface QuestionsDao {

    @Query("SELECT * FROM questions")
    List<Question> getAll();


    @Query("SELECT count(user_was_right) FROM questions where user_was_right == 1")
    int getSumCorrect();


    @Query("SELECT count(user_was_right) FROM questions where user_was_right == 0")
    int getSumIncorrect();


    @Query("SELECT * FROM questions where id == (:qId)")
    Question getQuestion(String qId);

    @Insert
    void insertAll(Question... reviews);

    @Insert
    void insert(Question review);

    @Delete
    void delete(Question review);

}
