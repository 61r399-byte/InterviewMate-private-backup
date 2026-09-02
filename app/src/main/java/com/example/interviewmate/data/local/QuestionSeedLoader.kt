package com.example.interviewmate.data.local

import android.content.Context
import android.util.Log
import com.example.interviewmate.data.model.QuestionEntity
import com.example.interviewmate.util.InterviewConstants
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull

interface QuestionSeedSource {
    fun loadQuestions(): List<QuestionEntity>
}

class QuestionSeedLoader(
    private val context: Context
) : QuestionSeedSource {
    override fun loadQuestions(): List<QuestionEntity> {
        return try {
            val rawJson = context.assets.open("questions.json").bufferedReader().use { it.readText() }
            parseQuestions(rawJson)
        } catch (exception: Exception) {
            Log.e(Tag, "Unable to load questions.json", exception)
            emptyList()
        }
    }

    companion object {
        private const val Tag = "QuestionSeedLoader"

        fun parseQuestions(rawJson: String): List<QuestionEntity> {
            val jsonArray = try {
                val root = Json.parseToJsonElement(rawJson)
                if (root !is JsonArray) {
                    Log.e(Tag, "questions.json root must be an array")
                    return emptyList()
                }
                root
            } catch (exception: SerializationException) {
                Log.e(Tag, "questions.json is not valid JSON", exception)
                return emptyList()
            }

            return buildList {
                jsonArray.forEachIndexed { index, element ->
                    if (element !is JsonObject) {
                        Log.w(Tag, "Skipped invalid question at index $index: item must be an object")
                        return@forEachIndexed
                    }

                    val question = try {
                        parseQuestion(element, index)
                    } catch (exception: Exception) {
                        Log.w(Tag, "Skipped invalid question at index $index", exception)
                        null
                    }
                    if (question != null) {
                        add(question)
                    }
                }
            }
        }

        private fun parseQuestion(obj: JsonObject, index: Int): QuestionEntity? {
            val category = obj.trimmedStringOrNull("category")
            val question = obj.trimmedStringOrNull("question")
            val answerHint = obj.trimmedStringOrNull("answerHint")
            val difficulty = obj.intOrNull("difficulty")
            val company = obj.companyOrNull(index) ?: return null

            if (category.isNullOrBlank()) {
                Log.w(Tag, "Skipped invalid question at index $index: category is blank")
                return null
            }
            if (category !in InterviewConstants.Categories) {
                Log.w(Tag, "Skipped invalid question at index $index: unknown category $category")
                return null
            }
            if (question.isNullOrBlank()) {
                Log.w(Tag, "Skipped invalid question at index $index: question is blank")
                return null
            }
            if (answerHint.isNullOrBlank()) {
                Log.w(Tag, "Skipped invalid question at index $index: answerHint is blank")
                return null
            }
            if (difficulty == null || difficulty !in 1..5) {
                Log.w(Tag, "Skipped invalid question at index $index: difficulty must be 1..5")
                return null
            }

            return QuestionEntity(
                category = category,
                question = question,
                answerHint = answerHint,
                difficulty = difficulty,
                company = company.value
            )
        }

        private fun JsonObject.trimmedStringOrNull(name: String): String? {
            val value = this[name] as? JsonPrimitive ?: return null
            if (!value.isString) return null
            return value.content.trim()
        }

        private fun JsonObject.intOrNull(name: String): Int? {
            val value = this[name] as? JsonPrimitive ?: return null
            if (value.isString) return null
            return value.intOrNull
        }

        private fun JsonObject.companyOrNull(index: Int): NullableCompany? {
            val name = "company"
            val value = this[name] ?: return NullableCompany(null)
            if (value is JsonNull) return NullableCompany(null)

            val primitive = value as? JsonPrimitive
            if (primitive == null || !primitive.isString) {
                Log.w(Tag, "Skipped invalid question at index $index: company must be a string or null")
                return null
            }
            return NullableCompany(primitive.content.trim().ifBlank { null })
        }

        private data class NullableCompany(val value: String?)
    }
}
