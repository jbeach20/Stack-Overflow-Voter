package com.jbeach.stackquestions.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.jbeach.stackquestions.data.Question;
import com.jbeach.stackquestions.data.QuestionsDao;

@Database(entities = {Question.class}, version = 2, exportSchema = false)
public abstract class VotingDatabase extends RoomDatabase {
    public abstract QuestionsDao questionsDao();

    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE `questions` ADD `correct_answer_id` varchar(225)");
        }
    };

}
