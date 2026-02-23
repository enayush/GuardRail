package com.example.guardrail.lab

import android.content.Context
import java.util.UUID

object UserSession {
    private const val PREFS_NAME = "guardrail_user_session"
    private const val KEY_USER_ID = "user_id"

    @Volatile
    private var cachedUserId: String? = null

    fun getUserId(context: Context): String {
        cachedUserId?.let { return it }

        synchronized(this) {
            cachedUserId?.let { return it }

            val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val storedId = prefs.getString(KEY_USER_ID, null)

            return if (storedId != null) {
                storedId.also { cachedUserId = it }
            } else {
                val newId = UUID.randomUUID().toString()
                prefs.edit().putString(KEY_USER_ID, newId).apply()
                newId.also { cachedUserId = it }
            }
        }
    }
}

