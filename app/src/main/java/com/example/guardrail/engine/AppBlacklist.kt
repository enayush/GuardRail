package com.example.guardrail.engine

/**
 * Centralized blacklist of package name prefixes.
 * Android-independent, no Context usage.
 */
object AppBlacklist {

    private val blockedPackagePrefixes = setOf(
        "com.example.guardrail",
        "com.android.systemui",
        "com.android.settings",
        "com.android.launcher",
        "com.google.android.launcher"
    )

    private val blockedImePrefixes = setOf(
        "com.google.android.inputmethod",
        "com.android.inputmethod",
        "com.samsung.android.honeyboard",
        "com.microsoft.swiftkey",
        "com.touchtype.swiftkey",
        "com.fleksy",
        "com.grammarly.keyboard"
    )

    /**
     * Check if a package should be blocked from detection.
     *
     * @param packageName The package name to check
     * @return true if blocked, false otherwise
     */
    fun isBlocked(packageName: String?): Boolean {
        if (packageName == null) return true

        if (blockedPackagePrefixes.any { packageName.startsWith(it) }) {
            return true
        }

        if (blockedImePrefixes.any { packageName.startsWith(it) }) {
            return true
        }

        return false
    }
}

