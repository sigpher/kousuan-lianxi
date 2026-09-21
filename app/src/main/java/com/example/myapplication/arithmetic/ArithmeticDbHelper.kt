package com.example.myapplication.arithmetic

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ArithmeticDbHelper(context: Context) :
    SQLiteOpenHelper(context, "arithmetic.db", null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE (
                $OP_A INTEGER NOT NULL,
                $OP_B INTEGER NOT NULL,
                $OPERATOR TEXT NOT NULL,
                $CORRECT INTEGER NOT NULL,
                $USER_ANSWER TEXT NOT NULL,
                $CREATED_AT INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(CREATE_REWARDS)
        db.execSQL(CREATE_RECORDS)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) db.execSQL(CREATE_REWARDS)
        if (oldVersion < 3) db.execSQL(CREATE_RECORDS)
    }

    companion object {
        private const val DATABASE_VERSION = 3

        const val TABLE = "wrong_problems"
        const val OP_A = "opa"
        const val OP_B = "opb"
        const val OPERATOR = "opr"
        const val CORRECT = "correct"
        const val USER_ANSWER = "user_answer"
        const val CREATED_AT = "created_at"

        const val REWARDS_TABLE = "rewards"
        const val REWARD_ID = "id"
        const val REWARD_TYPE = "type"
        const val REWARD_EARNED_AT = "earned_at"

        const val RECORDS_TABLE = "records"
        const val RECORD_COUNT = "count"
        const val RECORD_BEST_SCORE = "best_score"
        const val RECORD_BEST_TIME = "best_time_seconds"
        const val RECORD_BEST_COMBO = "best_combo"
        const val RECORD_UPDATED_AT = "updated_at"

        private const val CREATE_REWARDS =
            "CREATE TABLE $REWARDS_TABLE (" +
                "$REWARD_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$REWARD_TYPE TEXT NOT NULL, " +
                "$REWARD_EARNED_AT INTEGER NOT NULL)"

        private const val CREATE_RECORDS =
            "CREATE TABLE $RECORDS_TABLE (" +
                "$RECORD_COUNT INTEGER PRIMARY KEY, " +
                "$RECORD_BEST_SCORE INTEGER NOT NULL, " +
                "$RECORD_BEST_TIME INTEGER NOT NULL, " +
                "$RECORD_BEST_COMBO INTEGER NOT NULL, " +
                "$RECORD_UPDATED_AT INTEGER NOT NULL)"
    }
}