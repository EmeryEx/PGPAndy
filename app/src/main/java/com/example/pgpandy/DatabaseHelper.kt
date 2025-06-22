package com.example.pgpandy

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

private const val DB_NAME = "pgpandy.db"
private const val DB_VERSION = 1

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS preferences (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE,
                value TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS keys (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                owner_name TEXT NOT NULL,
                owner_email TEXT,
                public_key TEXT NOT NULL,
                private_key TEXT,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Future database migrations can be handled here. The simple example
        // just recreates the schema if a version change is detected.
        if (oldVersion != newVersion) {
            db.execSQL("DROP TABLE IF EXISTS preferences")
            db.execSQL("DROP TABLE IF EXISTS keys")
            onCreate(db)
        }
    }
}
