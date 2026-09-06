package com.example.security

import androidx.compose.ui.graphics.Color
import kotlin.math.log2

enum class StrengthLevel(val label: String, val color: Color, val score: Int) {
    VERY_WEAK("Very Weak", Color(0xFFF2B8B5), 0),
    WEAK("Weak", Color(0xFFF2B8B5), 1),
    FAIR("Fair", Color(0xFFF59E0B), 2),
    STRONG("Strong", Color(0xFF006493), 3),
    VERY_STRONG("Very Strong", Color(0xFFD0E4FF), 4)
}

data class StrengthResult(
    val level: StrengthLevel,
    val score: Int, // 0 to 4
    val entropyBits: Double,
    val feedback: List<String>
)

object PasswordStrengthCalculator {
    fun calculate(password: String): StrengthResult {
        if (password.isEmpty()) {
            return StrengthResult(
                level = StrengthLevel.VERY_WEAK,
                score = 0,
                entropyBits = 0.0,
                feedback = listOf("Password is empty")
            )
        }

        var poolSize = 0
        var hasLower = false
        var hasUpper = false
        var hasDigit = false
        var hasSymbol = false

        for (c in password) {
            when {
                c.isLowerCase() -> hasLower = true
                c.isUpperCase() -> hasUpper = true
                c.isDigit() -> hasDigit = true
                else -> hasSymbol = true
            }
        }

        if (hasLower) poolSize += 26
        if (hasUpper) poolSize += 26
        if (hasDigit) poolSize += 10
        if (hasSymbol) poolSize += 32

        val entropy = if (poolSize > 0) password.length * log2(poolSize.toDouble()) else 0.0
        val feedback = mutableListOf<String>()

        if (password.length < 8) {
            feedback.add("Too short (minimum 8 characters)")
        } else if (password.length < 12) {
            feedback.add("Consider increasing length to at least 14 characters")
        }

        val charTypesCount = listOf(hasLower, hasUpper, hasDigit, hasSymbol).count { it }
        if (charTypesCount < 3) {
            feedback.add("Mix uppercase, lowercase, numbers, and symbols")
        }

        // Determine level based on entropy and length
        val level = when {
            password.length < 6 || entropy < 25 -> StrengthLevel.VERY_WEAK
            password.length < 9 || entropy < 45 -> StrengthLevel.WEAK
            password.length < 12 || entropy < 65 -> StrengthLevel.FAIR
            entropy < 85 -> StrengthLevel.STRONG
            else -> StrengthLevel.VERY_STRONG
        }

        return StrengthResult(
            level = level,
            score = level.score,
            entropyBits = entropy,
            feedback = feedback
        )
    }
}
