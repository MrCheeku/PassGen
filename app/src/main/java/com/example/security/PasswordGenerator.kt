package com.example.security

import java.security.SecureRandom

object PasswordGenerator {
    private val UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private val LOWERCASE = "abcdefghijklmnopqrstuvwxyz"
    private val NUMBERS = "0123456789"
    private val SYMBOLS = "!@#$%^&*()-_=+[]{}|;:,.<>?"

    private val secureRandom = SecureRandom()

    data class GeneratorOptions(
        val length: Int = 16,
        val includeUppercase: Boolean = true,
        val includeLowercase: Boolean = true,
        val includeNumbers: Boolean = true,
        val includeSymbols: Boolean = true
    )

    fun generate(options: GeneratorOptions): String {
        val length = options.length.coerceIn(6, 64)
        val charPool = StringBuilder()

        val mandatoryChars = mutableListOf<Char>()

        if (options.includeUppercase) {
            charPool.append(UPPERCASE)
            mandatoryChars.add(UPPERCASE[secureRandom.nextInt(UPPERCASE.length)])
        }
        if (options.includeLowercase) {
            charPool.append(LOWERCASE)
            mandatoryChars.add(LOWERCASE[secureRandom.nextInt(LOWERCASE.length)])
        }
        if (options.includeNumbers) {
            charPool.append(NUMBERS)
            mandatoryChars.add(NUMBERS[secureRandom.nextInt(NUMBERS.length)])
        }
        if (options.includeSymbols) {
            charPool.append(SYMBOLS)
            mandatoryChars.add(SYMBOLS[secureRandom.nextInt(SYMBOLS.length)])
        }

        // Fallback if user somehow deselected all
        if (charPool.isEmpty()) {
            charPool.append(LOWERCASE).append(NUMBERS)
            mandatoryChars.add(LOWERCASE[secureRandom.nextInt(LOWERCASE.length)])
        }

        val pool = charPool.toString()
        val result = CharArray(length)

        // Fill mandatory characters to guarantee each selected set is represented
        val shuffledIndices = (0 until length).shuffled(kotlin.random.Random(secureRandom.nextLong()))
        for (i in mandatoryChars.indices) {
            if (i < length) {
                result[shuffledIndices[i]] = mandatoryChars[i]
            }
        }

        for (i in mandatoryChars.size until length) {
            result[shuffledIndices[i]] = pool[secureRandom.nextInt(pool.length)]
        }

        return String(result)
    }
}
