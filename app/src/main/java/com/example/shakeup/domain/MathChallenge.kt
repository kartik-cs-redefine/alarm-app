package com.example.shakeup.domain

import kotlin.random.Random

data class MathProblem(
    val question: String,
    val answer: Int
)

object MathChallengeGenerator {
    fun generate(random: Random = Random.Default): MathProblem {
        val a = random.nextInt(10, 50)
        val b = random.nextInt(10, 50)
        val isAddition = random.nextBoolean()
        val question = if (isAddition) "$a + $b" else "$a - $b"
        val answer = if (isAddition) a + b else a - b
        return MathProblem(question, answer)
    }
}
