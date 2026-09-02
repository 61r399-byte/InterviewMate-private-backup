package com.example.interviewmate.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.interviewmate.data.model.QuestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions ORDER BY category, difficulty, id")
    fun observeQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun getQuestionCount(): Int

    @Query("DELETE FROM questions")
    suspend fun deleteAllQuestions()

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAllQuestions(questions: List<QuestionEntity>): List<Long>
}
