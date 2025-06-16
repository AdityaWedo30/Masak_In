package com.example.masakin

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "meals.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "favorites"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_IMAGE = "image"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_IMAGE TEXT NOT NULL
            );
        """.trimIndent()

        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertFavorite(name: String, image: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_IMAGE, image)
        }

        val result = db.insert(TABLE_NAME, null, values)
        db.close()
        return result != -1L
    }

    fun getAllFavorites(): List<Pair<String, String>> {
        val list = mutableListOf<Pair<String, String>>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_NAME, $COLUMN_IMAGE FROM $TABLE_NAME", null)

        if (cursor.moveToFirst()) {
            do {
                val name = cursor.getString(0)
                val image = cursor.getString(1)
                list.add(Pair(name, image))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return list
    }
}
