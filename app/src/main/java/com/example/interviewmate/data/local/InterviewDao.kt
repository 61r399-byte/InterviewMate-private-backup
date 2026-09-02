package com.example.interviewmate.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.interviewmate.data.model.InterviewEntity
import com.example.interviewmate.data.model.InterviewItemEntity
import com.example.interviewmate.data.model.InterviewWithItems
import kotlinx.coroutines.flow.Flow

@Dao
abstract class InterviewDao {
    @Transaction
    @Query("SELECT * FROM interviews ORDER BY date DESC, created_at DESC")
    abstract fun observeInterviews(): Flow<List<InterviewWithItems>>

    @Transaction
    @Query(
        """
        SELECT * FROM interviews
        WHERE company LIKE '%' || :query || '%'
            OR position LIKE '%' || :query || '%'
        ORDER BY date DESC, created_at DESC
        """
    )
    abstract fun searchInterviews(query: String): Flow<List<InterviewWithItems>>

    @Transaction
    @Query("SELECT * FROM interviews WHERE id = :id")
    abstract fun observeInterview(id: Long): Flow<InterviewWithItems?>

    @Transaction
    open suspend fun saveInterviewWithItems(
        interview: InterviewEntity,
        items: List<InterviewItemEntity>
    ): Long {
        val interviewId = if (interview.id == 0L) {
            insertInterview(interview)
        } else {
            updateInterview(interview)
            interview.id
        }
        deleteItemsForInterview(interviewId)
        if (items.isNotEmpty()) {
            insertItems(items.map { it.copy(interviewId = interviewId) })
        }
        return interviewId
    }

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insertInterview(interview: InterviewEntity): Long

    @Update
    abstract suspend fun updateInterview(interview: InterviewEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertItems(items: List<InterviewItemEntity>)

    @Query("DELETE FROM interview_items WHERE interview_id = :interviewId")
    abstract suspend fun deleteItemsForInterview(interviewId: Long)

    @Query("DELETE FROM interviews WHERE id = :id")
    abstract suspend fun deleteInterviewById(id: Long)

    @Delete
    abstract suspend fun deleteInterview(interview: InterviewEntity)
}
