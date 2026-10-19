package com.example.shakeup.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MathChallengeTest {

    @Test
    fun mathProblemGeneration_computesCorrectAnswer() {
        // Deterministic random seed
        val problem = MathChallengeGenerator.generate(Random(42))
        // Verify question format and correctness
        val parts = problem.question.split(" ")
        assertEquals(3, parts.size)
        val a = parts[0].toInt()
        val op = parts[1]
        val b = parts[2].toInt()

        val expectedAnswer = if (op == "+") a + b else a - b
        assertEquals(expectedAnswer, problem.answer)
    }
}
