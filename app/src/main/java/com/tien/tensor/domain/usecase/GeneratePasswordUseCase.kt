package com.tien.tensor.domain.usecase

import java.security.SecureRandom
import java.util.Collections

/**
 * Generates cryptographically random passwords guaranteeing at least one
 * character from each class (upper, lower, digit, symbol).
 */
class GeneratePasswordUseCase {

    private val random = SecureRandom()

    operator fun invoke(length: Int = 16): String {
        val safeLength = length.coerceIn(8, 64)
        val all = CHARACTER_SETS.joinToString("")
        val chars = MutableList(safeLength) { all[random.nextInt(all.length)] }
        // Overwrite the first N slots to guarantee one char per class, then shuffle.
        CHARACTER_SETS.forEachIndexed { i, set -> chars[i] = set[random.nextInt(set.length)] }
        Collections.shuffle(chars, random)
        return chars.joinToString("")
    }

    private companion object {
        val CHARACTER_SETS = listOf(
            "ABCDEFGHJKLMNPQRSTUVWXYZ",
            "abcdefghijkmnopqrstuvwxyz",
            "23456789",
            "!@#\$%&*-_=+?"
        )
    }
}
