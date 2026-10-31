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
    val entropyBits: Double, // Raw character-pool estimate, not a crack-time guarantee.
    val feedback: List<String>
)

object PasswordStrengthCalculator {
    private val commonFragments = listOf(
        "password", "qwerty", "letmein", "welcome", "admin", "iloveyou",
        "123456", "123456789", "abc123"
    )

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

        for (character in password) {
            when {
                character.isLowerCase() -> hasLower = true
                character.isUpperCase() -> hasUpper = true
                character.isDigit() -> hasDigit = true
                else -> hasSymbol = true
            }
        }

        if (hasLower) poolSize += 26
        if (hasUpper) poolSize += 26
        if (hasDigit) poolSize += 10
        if (hasSymbol) poolSize += 32

        val rawEntropy = if (poolSize > 0) password.length * log2(poolSize.toDouble()) else 0.0
        val feedback = mutableListOf<String>()
        var adjustedEntropy = rawEntropy
        val normalized = password.lowercase()

        if (password.length < 8) {
            feedback.add("Too short (minimum 8 characters)")
        } else if (password.length < 12) {
            feedback.add("Consider increasing length to at least 14 characters")
        }

        val characterTypesCount = listOf(hasLower, hasUpper, hasDigit, hasSymbol).count { it }
        if (characterTypesCount < 3) {
            feedback.add("Mix uppercase, lowercase, numbers, and symbols")
        }

        if (Regex("(.)\\1{2,}").containsMatchIn(password)) {
            adjustedEntropy -= 20.0
            feedback.add("Avoid long runs of the same character")
        }

        if (containsSequentialRun(normalized)) {
            adjustedEntropy -= 15.0
            feedback.add("Avoid predictable keyboard or sequential patterns")
        }

        if (commonFragments.any(normalized::contains)) {
            adjustedEntropy = minOf(adjustedEntropy, 20.0)
            feedback.add("Avoid common words and predictable password fragments")
        }

        adjustedEntropy = adjustedEntropy.coerceAtLeast(0.0)

        val level = when {
            password.length < 6 || adjustedEntropy < 25 -> StrengthLevel.VERY_WEAK
            password.length < 9 || adjustedEntropy < 45 -> StrengthLevel.WEAK
            password.length < 12 || adjustedEntropy < 65 -> StrengthLevel.FAIR
            adjustedEntropy < 85 -> StrengthLevel.STRONG
            else -> StrengthLevel.VERY_STRONG
        }

        return StrengthResult(
            level = level,
            score = level.score,
            entropyBits = rawEntropy,
            feedback = feedback
        )
    }

    private fun containsSequentialRun(value: String): Boolean {
        if (value.length < 4) return false

        var ascending = 1
        var descending = 1
        for (index in 1 until value.length) {
            val difference = value[index].code - value[index - 1].code
            ascending = if (difference == 1) ascending + 1 else 1
            descending = if (difference == -1) descending + 1 else 1
            if (ascending >= 4 || descending >= 4) return true
        }
        return false
    }
}
