package com.tien.tensor.domain.usecase

import java.security.MessageDigest

class HashTextUseCase {

    operator fun invoke(text: String, algorithm: String = "SHA-256"): String {
        if (text.isEmpty()) return ""
        val digest = MessageDigest.getInstance(algorithm).digest(text.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}
