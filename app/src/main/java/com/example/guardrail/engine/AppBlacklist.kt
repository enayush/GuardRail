package com.example.guardrail.engine

/**
 * Centralized blacklist of package name prefixes.
 * Android-independent, no Context usage.
 * Expanded to cover common OEMs and system overlays.
 */
object AppBlacklist {

    // 1. Core System & App Store (Prevents scanning permission popups and system dialogs)
    private val systemPrefixes = setOf(
        "com.example.guardrail",                  // Self
        "android",                                // Core Android framework/dialogs
        "com.android.systemui",                   // Status bar, notification shade
        "com.android.settings",                   // Settings app
        "com.android.vending",                    // Google Play Store
        "com.google.android.gms",                 // Google Play Services
        "com.google.android.permissioncontroller",// Android permission allow/deny popups
        "com.google.android.googlequicksearchbox" // Google Assistant overlay
    )

    // 2. OEM Specific System UIs (Crucial for Oppo/Realme/OnePlus/Xiaomi)
    private val oemSystemPrefixes = setOf(
        "com.coloros.",     // Oppo / Realme system apps
        "com.oplus.",       // OnePlus / Oppo system apps
        "com.miui.",        // Xiaomi system apps
        "com.sec.android.", // Samsung system apps
        "com.vivo."         // Vivo system apps
    )

    // 3. Common Launchers (Default and 3rd Party)
    private val launcherPrefixes = setOf(
        "com.android.launcher",
        "com.google.android.launcher",
        "com.google.android.apps.nexuslauncher", // Pixel Launcher
        "com.sec.android.app.launcher",          // Samsung OneUI Launcher
        "com.teslacoilsw.launcher",              // Nova Launcher
        "com.microsoft.launcher"                 // Microsoft Launcher
    )

    // 4. Keyboards (IMEs) to prevent scanning every keystroke
    private val imePrefixes = setOf(
        "com.google.android.inputmethod", // Gboard
        "com.android.inputmethod",        // AOSP keyboard
        "com.samsung.android.honeyboard", // Samsung Keyboard
        "com.microsoft.swiftkey",         // SwiftKey
        "com.touchtype.swiftkey",         // SwiftKey (older)
        "com.fleksy",                     // Fleksy
        "com.grammarly.keyboard",         // Grammarly
        "com.emoji.keyboard.touchpal"     // TouchPal (Pre-installed on many Oppo/Vivo devices)
    )

    /**
     * Check if a package should be blocked from detection.
     *
     * @param packageName The package name to check
     * @return true if blocked, false otherwise
     */
    fun isBlocked(packageName: String?): Boolean {
        if (packageName.isNullOrBlank()) return true

        // Check against all categories
        return systemPrefixes.any { packageName.startsWith(it) } ||
                oemSystemPrefixes.any { packageName.startsWith(it) } ||
                launcherPrefixes.any { packageName.startsWith(it) } ||
                imePrefixes.any { packageName.startsWith(it) }
    }
}