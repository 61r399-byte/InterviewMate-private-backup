package com.example.interviewmate.data.local

import android.content.Context

interface QuestionSeedVersionStore {
    fun getImportedVersion(): Int

    fun setImportedVersion(version: Int)
}

class SharedPreferencesQuestionSeedVersionStore(
    context: Context
) : QuestionSeedVersionStore {
    private val preferences = context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)

    override fun getImportedVersion(): Int {
        return preferences.getInt(KeyImportedQuestionSeedVersion, 0)
    }

    override fun setImportedVersion(version: Int) {
        preferences.edit()
            .putInt(KeyImportedQuestionSeedVersion, version)
            .apply()
    }

    private companion object {
        const val PreferencesName = "question_seed_preferences"
        const val KeyImportedQuestionSeedVersion = "imported_question_seed_version"
    }
}
