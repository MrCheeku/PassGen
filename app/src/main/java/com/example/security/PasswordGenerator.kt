package com.example.security

import java.security.SecureRandom

object PasswordGenerator {
    private const val UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val LOWERCASE = "abcdefghijklmnopqrstuvwxyz"
    private const val NUMBERS = "0123456789"
    private const val SYMBOLS = "!@#$%^&*()-_=+[]{}|;:,.<>?"

    private val secureRandom = SecureRandom()

    data class GeneratorOptions(
        val length: Int = 16,
        val includeUppercase: Boolean = true,
        val includeLowercase: Boolean = true,
        val includeNumbers: Boolean = true,
        val includeSymbols: Boolean = true
    )

    fun generate(options: GeneratorOptions): String {
        require(options.length in 6..64) {
            "Password length must be between 6 and 64 characters."
        }

        val characterPool = StringBuilder()
        val requiredCharacters = mutableListOf<Char>()

        fun include(characters: String, enabled: Boolean) {
            if (enabled) {
                characterPool.append(characters)
                requiredCharacters += characters[secureRandom.nextInt(characters.length)]
            }
        }

        include(UPPERCASE, options.includeUppercase)
        include(LOWERCASE, options.includeLowercase)
        include(NUMBERS, options.includeNumbers)
        include(SYMBOLS, options.includeSymbols)

        if (characterPool.isEmpty()) {
            // An empty selection still produces a useful, secure password.
            characterPool.append(LOWERCASE).append(NUMBERS)
            requiredCharacters += LOWERCASE[secureRandom.nextInt(LOWERCASE.length)]
        }

        require(options.length >= requiredCharacters.size) {
            "Password length is too short for the selected character groups."
        }

        val pool = characterPool.toString()
        val result = CharArray(options.length)

        // Guarantee at least one character from every enabled group.
        requiredCharacters.forEachIndexed { index, character ->
            result[index] = character
        }

        for (index in requiredCharacters.size until result.size) {
            result[index] = pool[secureRandom.nextInt(pool.length)]
        }

        // Shuffle with SecureRandom instead of a separate non-cryptographic RNG.
        for (index in result.lastIndex downTo 1) {
            val swapIndex = secureRandom.nextInt(index + 1)
            val current = result[index]
            result[index] = result[swapIndex]
            result[swapIndex] = current
        }

        return String(result)
    }
}
