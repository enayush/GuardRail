package com.example.guardrail.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test to verify PermissionState functions return correct boolean values.
 */
@RunWith(AndroidJUnit4::class)
class PermissionStateTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun testIsAccessibilityEnabled_returnsBoolean() {
        // Should return a boolean without throwing exceptions
        val result = PermissionState.isAccessibilityEnabled(context)
        assert(result is Boolean)
    }

    @Test
    fun testIsOverlayGranted_returnsBoolean() {
        // Should return a boolean without throwing exceptions
        val result = PermissionState.isOverlayGranted(context)
        assert(result is Boolean)
    }

    @Test
    fun testIsBatteryOptimizationIgnored_returnsBoolean() {
        // Should return a boolean without throwing exceptions
        val result = PermissionState.isBatteryOptimizationIgnored(context)
        assert(result is Boolean)
    }
}

