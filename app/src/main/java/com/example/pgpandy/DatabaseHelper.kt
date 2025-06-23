package com.example.pgpandy

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues

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
            CREATE TABLE pgp_keys (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
            
                -- Identity Info
                user_id TEXT,                      -- e.g. "Alice <alice@example.com>"
                fingerprint TEXT UNIQUE NOT NULL, -- full key fingerprint
                key_id TEXT,                      -- short key ID (8 or 16 chars)
            
                -- Key Type
                is_private INTEGER DEFAULT 0,     -- 1 if this is a private key
                is_revoked INTEGER DEFAULT 0,     -- 1 if revoked
                is_expired INTEGER DEFAULT 0,     -- 1 if expired
                is_trusted INTEGER DEFAULT 0,     -- 1 if user marked trusted
            
                -- Dates
                created_at INTEGER,               -- UNIX timestamp (UTC)
                expires_at INTEGER,               -- nullable if key never expires
            
                -- Key Material
                armored_key TEXT NOT NULL,        -- the ASCII-armored public/private key block
            
                -- Metadata
                algorithm TEXT,                   -- e.g. "RSA", "ED25519"
                bit_length INTEGER,               -- e.g. 4096
                comment TEXT,                     -- optional label or notes
            
                -- App Info
                last_used INTEGER,                -- for cleanup/rotation tracking
                inserted_at INTEGER DEFAULT (strftime('%s', 'now'))
            );

            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Future database migrations can be handled here. The simple example
        // just recreates the schema if a version change is detected.
        if (oldVersion != newVersion) {

        }
    }

    fun getPreference(name: String): String? {
        readableDatabase.rawQuery(
            "SELECT value FROM preferences WHERE name = ?",
            arrayOf(name)
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    }

    fun setPreference(name: String, value: String) {
        val values = ContentValues().apply {
            put("name", name)
            put("value", value)
        }
        writableDatabase.insertWithOnConflict(
            "preferences",
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    /**
     * Inserts a parsed PGP key into the database.
     */
    fun insertKey(info: PgpKeyInfo) {
        val values = ContentValues().apply {
            put("user_id", info.userId)
            put("fingerprint", info.fingerprint)
            put("key_id", info.keyId)
            put("is_private", if (info.isPrivate) 1 else 0)
            put("armored_key", info.armoredKey)
            put("algorithm", info.algorithm)
            put("bit_length", info.bitLength)
            put("comment", info.comment)
            put("created_at", info.createdAt)
            put("expires_at", info.expiresAt)
        }
        writableDatabase.insertWithOnConflict(
            "pgp_keys",
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    /**
     * Returns all private keys stored in the database.
     */
    fun getPrivateKeys(): List<PgpKeyInfo> {
        val keys = mutableListOf<PgpKeyInfo>()
        readableDatabase.rawQuery(
            "SELECT user_id, fingerprint, key_id, is_private, armored_key, algorithm, bit_length, comment, created_at, expires_at FROM pgp_keys WHERE is_private = 1",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                keys.add(
                    PgpKeyInfo(
                        userId = cursor.getString(0),
                        fingerprint = cursor.getString(1),
                        keyId = cursor.getString(2),
                        isPrivate = cursor.getInt(3) == 1,
                        armoredKey = cursor.getString(4),
                        algorithm = cursor.getString(5),
                        bitLength = if (cursor.isNull(6)) null else cursor.getInt(6),
                        comment = cursor.getString(7),
                        createdAt = if (cursor.isNull(8)) null else cursor.getLong(8),
                        expiresAt = if (cursor.isNull(9)) null else cursor.getLong(9)
                    )
                )
            }
        }
        return keys
    }

    /**
     * Deletes a key by its fingerprint.
     */
    fun deleteKey(fingerprint: String) {
        writableDatabase.delete("pgp_keys", "fingerprint = ?", arrayOf(fingerprint))
    }
}
