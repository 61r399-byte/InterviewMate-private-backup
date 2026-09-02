package com.example.interviewmate.util

object InterviewConstants {
    const val AllCategories = "All"

    const val CategoryAlgorithms = "Algorithms"
    const val CategorySystemDesign = "System Design"
    const val CategoryBehavioral = "Behavioral"
    const val CategoryProjectExperience = "Project Experience"

    const val RoundFirst = "First Round"
    const val RoundSecond = "Second Round"
    const val RoundThird = "Third Round"
    const val RoundFinal = "Final Round"

    const val ResultPassed = "Passed"
    const val ResultPending = "Pending"
    const val ResultFailed = "Failed"

    val Categories = listOf(
        CategoryAlgorithms,
        CategorySystemDesign,
        CategoryBehavioral,
        CategoryProjectExperience
    )
    val Rounds = listOf(RoundFirst, RoundSecond, RoundThird, RoundFinal)
    val Results = listOf(ResultPassed, ResultPending, ResultFailed)

    fun normalizeCategory(category: String): String {
        return when (category) {
            "\u7B97\u6CD5" -> CategoryAlgorithms
            "\u7CFB\u7EDF\u8BBE\u8BA1" -> CategorySystemDesign
            "\u884C\u4E3A\u9762\u8BD5" -> CategoryBehavioral
            "\u9879\u76EE\u7ECF\u9A8C" -> CategoryProjectExperience
            else -> category
        }
    }

    fun normalizeRound(round: String): String {
        return when (round) {
            "\u4E00\u9762" -> RoundFirst
            "\u4E8C\u9762" -> RoundSecond
            "\u4E09\u9762" -> RoundThird
            "\u7EC8\u9762" -> RoundFinal
            else -> round
        }
    }

    fun normalizeResult(result: String): String {
        return when (result) {
            "\u901A\u8FC7" -> ResultPassed
            "\u5F85\u5B9A" -> ResultPending
            "\u672A\u901A\u8FC7" -> ResultFailed
            else -> result
        }
    }
}
