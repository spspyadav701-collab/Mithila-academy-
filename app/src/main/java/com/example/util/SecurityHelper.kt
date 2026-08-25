package com.example.util

import java.security.MessageDigest

object SecurityHelper {
    // Exclusive Admin passcode: 'spyadav9631473150@'
    const val EXCLUSIVE_ADMIN_PASSCODE = "spyadav9631473150@"

    fun hashPin(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private val ADMIN_PASSCODE_HASH = hashPin(EXCLUSIVE_ADMIN_PASSCODE)

    /**
     * Verifies if input matches the mandatory Admin passcode 'spyadav9631473150@'
     */
    fun verifyAdminPasscode(inputPasscode: String): Boolean {
        val trimmed = inputPasscode.trim()
        if (trimmed.isEmpty()) return false
        return trimmed == EXCLUSIVE_ADMIN_PASSCODE || hashPin(trimmed) == ADMIN_PASSCODE_HASH
    }

    fun verifyOwnerPin(inputPin: String, customHash: String? = null): Boolean {
        return verifyAdminPasscode(inputPin)
    }
}


