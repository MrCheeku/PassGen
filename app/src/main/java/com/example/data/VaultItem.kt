package com.example.data

import com.example.security.PasswordStrengthCalculator
import com.example.security.StrengthLevel
import com.example.security.StrengthResult

data class VaultItem(
    val id: Long = 0,
    val title: String,
    val username: String,
    val password: String,
    val website: String = "",
    val notes: String = "",
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val strength: StrengthResult
        get() = PasswordStrengthCalculator.calculate(password)

    val isWeak: Boolean
        get() = strength.level == StrengthLevel.VERY_WEAK || strength.level == StrengthLevel.WEAK

    val isShort: Boolean
        get() = password.length < 10

    val isMissingWebsite: Boolean
        get() = website.isBlank()

    val isOld: Boolean
        get() {
            val ninetyDaysMs = 90L * 24 * 60 * 60 * 1000L
            return (System.currentTimeMillis() - updatedAt) > ninetyDaysMs
        }
}
