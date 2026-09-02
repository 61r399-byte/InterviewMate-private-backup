package com.example.interviewmate.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.interviewmate.data.model.InterviewEntity
import com.example.interviewmate.data.model.InterviewItemEntity
import com.example.interviewmate.data.model.QuestionEntity

@Database(
    entities = [
        InterviewEntity::class,
        InterviewItemEntity::class,
        QuestionEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun interviewDao(): InterviewDao
    abstract fun questionDao(): QuestionDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "interviewmate.db"
                ).addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS questions_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        category TEXT NOT NULL,
                        question TEXT NOT NULL,
                        answer_hint TEXT NOT NULL,
                        difficulty INTEGER NOT NULL,
                        company TEXT
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO questions_new (id, category, question, answer_hint, difficulty, company)
                    SELECT id, category, question, answer_hint, difficulty, company FROM questions
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE questions")
                db.execSQL("ALTER TABLE questions_new RENAME TO questions")
            }
        }
    }
}
