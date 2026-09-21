package com.example.myapplication.arithmetic

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ArithmeticDbHelper(context: Context) :
    SQLiteOpenHelper(context, "arithmetic.db", null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_USERS)
        db.execSQL(CREATE_WRONG_PROBLEMS)
        db.execSQL(CREATE_HISTORY)
        db.execSQL(CREATE_REWARDS)
        db.execSQL(CREATE_RECORDS)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) db.execSQL(CREATE_REWARDS)
        if (oldVersion < 3) db.execSQL(CREATE_RECORDS_V4)
        if (oldVersion < 4) db.execSQL(CREATE_HISTORY)
        if (oldVersion < 5) {
            db.execSQL(CREATE_USERS)
            db.execSQL("ALTER TABLE $TABLE ADD COLUMN $USER_ID INTEGER NOT NULL DEFAULT $GUEST_USER_ID")
            db.execSQL("ALTER TABLE $HISTORY_TABLE ADD COLUMN $USER_ID INTEGER NOT NULL DEFAULT $GUEST_USER_ID")
            db.execSQL("ALTER TABLE $REWARDS_TABLE ADD COLUMN $USER_ID INTEGER NOT NULL DEFAULT $GUEST_USER_ID")
            db.execSQL("ALTER TABLE $RECORDS_TABLE RENAME TO ${RECORDS_TABLE}_v4")
            db.execSQL(CREATE_RECORDS)
            db.execSQL(
                "INSERT INTO $RECORDS_TABLE ($USER_ID, $RECORD_COUNT, $RECORD_BEST_SCORE, " +
                    "$RECORD_BEST_TIME, $RECORD_BEST_COMBO, $RECORD_UPDATED_AT) " +
                    "SELECT $GUEST_USER_ID, $RECORD_COUNT, $RECORD_BEST_SCORE, " +
                    "$RECORD_BEST_TIME, $RECORD_BEST_COMBO, $RECORD_UPDATED_AT " +
                    "FROM ${RECORDS_TABLE}_v4"
            )
            db.execSQL("DROP TABLE ${RECORDS_TABLE}_v4")
        }
    }

    companion object {
        private const val DATABASE_VERSION = 5

        const val TABLE = "wrong_problems"
        const val HISTORY_TABLE = "wrong_history"
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

        const val USERS_TABLE = "users"
        const val USER_ID = "user_id"
        const val USERNAME = "username"
        const val PASSWORD_HASH = "password_hash"
        const val AVATAR_PATH = "avatar_path"
        const val USER_CREATED_AT = "created_at"
        const val GUEST_USER_ID = 0

        private const val USER_COLUMNS =
            "$USER_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$USERNAME TEXT NOT NULL UNIQUE, " +
                "$PASSWORD_HASH TEXT NOT NULL, " +
                "$AVATAR_PATH TEXT, " +
                "$USER_CREATED_AT INTEGER NOT NULL"

        private const val WRONG_COLUMNS =
            "$USER_ID INTEGER NOT NULL DEFAULT $GUEST_USER_ID, " +
                "$OP_A INTEGER NOT NULL, " +
                "$OP_B INTEGER NOT NULL, " +
                "$OPERATOR TEXT NOT NULL, " +
                "$CORRECT INTEGER NOT NULL, " +
                "$USER_ANSWER TEXT NOT NULL, " +
                "$CREATED_AT INTEGER NOT NULL"

        private const val CREATE_USERS = "CREATE TABLE $USERS_TABLE ($USER_COLUMNS)"

        private const val CREATE_WRONG_PROBLEMS = "CREATE TABLE $TABLE ($WRONG_COLUMNS)"

        private const val CREATE_HISTORY = "CREATE TABLE $HISTORY_TABLE ($WRONG_COLUMNS)"

        private const val CREATE_REWARDS =
            "CREATE TABLE $REWARDS_TABLE (" +
                "$REWARD_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$USER_ID INTEGER NOT NULL DEFAULT $GUEST_USER_ID, " +
                "$REWARD_TYPE TEXT NOT NULL, " +
                "$REWARD_EARNED_AT INTEGER NOT NULL)"

        private const val CREATE_RECORDS_V4 =
            "CREATE TABLE $RECORDS_TABLE (" +
                "$RECORD_COUNT INTEGER PRIMARY KEY, " +
                "$RECORD_BEST_SCORE INTEGER NOT NULL, " +
                "$RECORD_BEST_TIME INTEGER NOT NULL, " +
                "$RECORD_BEST_COMBO INTEGER NOT NULL, " +
                "$RECORD_UPDATED_AT INTEGER NOT NULL)"

        private const val CREATE_RECORDS =
            "CREATE TABLE $RECORDS_TABLE (" +
                "$USER_ID INTEGER NOT NULL DEFAULT $GUEST_USER_ID, " +
                "$RECORD_COUNT INTEGER NOT NULL, " +
                "$RECORD_BEST_SCORE INTEGER NOT NULL, " +
                "$RECORD_BEST_TIME INTEGER NOT NULL, " +
                "$RECORD_BEST_COMBO INTEGER NOT NULL, " +
                "$RECORD_UPDATED_AT INTEGER NOT NULL, " +
                "PRIMARY KEY ($USER_ID, $RECORD_COUNT))"
    }
}