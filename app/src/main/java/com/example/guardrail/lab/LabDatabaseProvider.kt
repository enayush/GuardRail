package com.example.guardrail.lab

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object LabDatabaseProvider {
    @Volatile
    private var instance: GuardRailDatabase? = null

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS survey_responses (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    userId TEXT NOT NULL,
                    q_helpfulness INTEGER NOT NULL,
                    q_intrusiveness INTEGER NOT NULL,
                    q_changed_decision INTEGER NOT NULL,
                    q_trust INTEGER NOT NULL,
                    freeText TEXT,
                    timestamp INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE detection_logs ADD COLUMN timeToNextActionMs INTEGER")
            db.execSQL("ALTER TABLE detection_logs ADD COLUMN postDetectionAction TEXT")
            db.execSQL("ALTER TABLE detection_logs ADD COLUMN warningIgnored INTEGER")
        }
    }

    fun get(context: Context): GuardRailDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                GuardRailDatabase::class.java,
                "guardrail_database"
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
                .also { instance = it }
        }
    }
}
