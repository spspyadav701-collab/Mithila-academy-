package com.example.util

import java.security.MessageDigest

/**
 * SecurityHelper: Secure cryptographic hashing and credential protection.
 * Zero plaintext credentials stored in bytecode or binary assets.
 * All verification operates via irreversible SHA-256 digests.
 */
object SecurityHelper {
    // Irreversible SHA-256 cryptographic digest for authorized faculty administration
    private const val SECURE_AUTH_DIGEST = "3c7d8ee95b894f8d375ad166158f5eba8246858d84c4af4fff69498b2c69f56f"

    fun hashPin(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Cryptographically verifies input passcode against the secure digest.
     * Prevents timing attacks and reverse-engineering of plaintext credentials.
     */
    fun verifyAdminPasscode(inputPasscode: String): Boolean {
        val trimmed = inputPasscode.trim()
        if (trimmed.isEmpty()) return false
        val computedHash = hashPin(trimmed)
        return MessageDigest.isEqual(
            computedHash.toByteArray(Charsets.UTF_8),
            SECURE_AUTH_DIGEST.toByteArray(Charsets.UTF_8)
        )
    }

    fun verifyOwnerPin(inputPin: String, customHash: String? = null): Boolean {
        if (!customHash.isNullOrBlank()) {
            val computed = hashPin(inputPin.trim())
            return MessageDigest.isEqual(
                computed.toByteArray(Charsets.UTF_8),
                customHash.toByteArray(Charsets.UTF_8)
            )
        }
        return verifyAdminPasscode(inputPin)
    }
}



