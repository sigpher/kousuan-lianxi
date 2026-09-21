package com.example.myapplication.arithmetic

import android.content.ContentValues
import android.content.Context
import com.example.myapplication.arithmetic.ArithmeticDbHelper.Companion.CORRECT
import com.example.myapplication.arithmetic.ArithmeticDbHelper.Companion.CREATED_AT
import com.example.myapplication.arithmetic.ArithmeticDbHelper.Companion.OP_A
import com.example.myapplication.arithmetic.ArithmeticDbHelper.Companion.OP_B
import com.example.myapplication.arithmetic.ArithmeticDbHelper.Companion.OPERATOR
import com.example.myapplication.arithmetic.ArithmeticDbHelper.Companion.RECORD_BEST_COMBO
import com.example.myapplication.arithmetic.ArithmeticDbHelper.Companion.RECORD_BEST_SCORE
import com.example.myapplication.arithmetic.ArithmeticDbHelper.Companion.RECORD_BEST_TIME
import com.example.myapplication.arithmetic.ArithmeticDbHelper.Companion.RECORD_COUNT
import com.example.myapplication.arithmetic.ArithmeticDbHelper.Companion.RECORDS_TABLE
import com.example.myapplication.arithmetic.ArithmeticDbHelper.Companion.RECORD_UPDATED_AT
import com.example.myapplication.arithmetic.ArithmeticDbHelper.Companion.REWARD_EARNED_AT
import com.example.myapplication.arithmetic.ArithmeticDbHelper.Companion.REWARDS_TABLE
import com.example.myapplication.arithmetic.ArithmeticDbHelper.Companion.REWARD_TYPE
import com.example.myapplication.arithmetic.ArithmeticDbHelper.Companion.TABLE
import com.example.myapplication.arithmetic.ArithmeticDbHelper.Companion.USER_ANSWER
import android.database.sqlite.SQLiteDatabase
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ArithmeticRepository(context: Context) {

    private val dbHelper = ArithmeticDbHelper(context.applicationContext)
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    fun recordWrong(question: ArithmeticQuestion, userAnswer: String) {
        executor.execute {
            val db = dbHelper.writableDatabase
            val where = "$OP_A=? AND $OP_B=? AND $OPERATOR=?"
            db.delete(
                TABLE,
                where,
                wrongKeyArgs(question)
            )
            val values = ContentValues().apply {
                put(OP_A, question.operandA)
                put(OP_B, question.operandB)
                put(OPERATOR, question.operator.name)
                put(CORRECT, question.answer)
                put(USER_ANSWER, userAnswer)
                put(CREATED_AT, System.currentTimeMillis())
            }
            db.insert(TABLE, null, values)
        }
    }

    fun deleteWrong(question: ArithmeticQuestion) {
        executor.execute {
            dbHelper.writableDatabase.delete(
                TABLE,
                "$OP_A=? AND $OP_B=? AND $OPERATOR=?",
                wrongKeyArgs(question)
            )
        }
    }

    private fun wrongKeyArgs(question: ArithmeticQuestion): Array<String> = arrayOf(
        question.operandA.toString(),
        question.operandB.toString(),
        question.operator.name
    )

    fun insertReward(type: String) {
        executor.execute {
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put(REWARD_TYPE, type)
                put(REWARD_EARNED_AT, System.currentTimeMillis())
            }
            db.insert(REWARDS_TABLE, null, values)
        }
    }

    data class Record(
        val count: Int,
        val bestScore: Int,
        val bestTimeSeconds: Int,
        val bestCombo: Int
    )

    fun updateRecord(
        count: Int,
        score: Int,
        timeSeconds: Int,
        combo: Int,
        callback: (Boolean) -> Unit
    ) {
        executor.execute {
            val db = dbHelper.writableDatabase
            val existing = queryRecord(count)
            val improved = existing == null ||
                score > existing.bestScore ||
                (score == existing.bestScore && timeSeconds < existing.bestTimeSeconds)
            if (improved) {
                val values = ContentValues().apply {
                    put(RECORD_COUNT, count)
                    put(RECORD_BEST_SCORE, score)
                    put(RECORD_BEST_TIME, timeSeconds)
                    put(RECORD_BEST_COMBO, combo)
                    put(RECORD_UPDATED_AT, System.currentTimeMillis())
                }
                db.insertWithOnConflict(RECORDS_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
            callback(improved)
        }
    }

    fun fetchRecords(callback: (List<Record>) -> Unit) {
        executor.execute {
            val db = dbHelper.readableDatabase
            val records = mutableListOf<Record>()
            db.query(RECORDS_TABLE, null, null, null, null, null, "$RECORD_COUNT ASC").use { cursor ->
                while (cursor.moveToNext()) {
                    records += Record(
                        count = cursor.getInt(cursor.getColumnIndexOrThrow(RECORD_COUNT)),
                        bestScore = cursor.getInt(cursor.getColumnIndexOrThrow(RECORD_BEST_SCORE)),
                        bestTimeSeconds = cursor.getInt(cursor.getColumnIndexOrThrow(RECORD_BEST_TIME)),
                        bestCombo = cursor.getInt(cursor.getColumnIndexOrThrow(RECORD_BEST_COMBO))
                    )
                }
            }
            callback(records)
        }
    }

    private fun queryRecord(count: Int): Record? {
        val db = dbHelper.readableDatabase
        db.query(
            RECORDS_TABLE,
            null,
            "$RECORD_COUNT=?",
            arrayOf(count.toString()),
            null,
            null,
            null
        ).use { cursor ->
            return if (cursor.moveToFirst()) {
                Record(
                    count = cursor.getInt(cursor.getColumnIndexOrThrow(RECORD_COUNT)),
                    bestScore = cursor.getInt(cursor.getColumnIndexOrThrow(RECORD_BEST_SCORE)),
                    bestTimeSeconds = cursor.getInt(cursor.getColumnIndexOrThrow(RECORD_BEST_TIME)),
                    bestCombo = cursor.getInt(cursor.getColumnIndexOrThrow(RECORD_BEST_COMBO))
                )
            } else {
                null
            }
        }
    }

    fun countRewards(type: String, callback: (Int) -> Unit) {
        executor.execute {
            val db = dbHelper.readableDatabase
            val count = db.query(
                REWARDS_TABLE,
                null,
                "$REWARD_TYPE=?",
                arrayOf(type),
                null,
                null,
                null
            ).use { it.count }
            callback(count)
        }
    }

    fun fetchWrong(callback: (List<WrongProblem>) -> Unit) {
        executor.execute {
            val db = dbHelper.readableDatabase
            val problems = mutableListOf<WrongProblem>()
            db.query(TABLE, null, null, null, null, null, "$CREATED_AT DESC").use { cursor ->
                while (cursor.moveToNext()) {
                    problems += WrongProblem(
                        question = ArithmeticQuestion(
                            operator = Operation.valueOf(
                                cursor.getString(cursor.getColumnIndexOrThrow(OPERATOR))
                            ),
                            operandA = cursor.getInt(cursor.getColumnIndexOrThrow(OP_A)),
                            operandB = cursor.getInt(cursor.getColumnIndexOrThrow(OP_B)),
                            answer = cursor.getInt(cursor.getColumnIndexOrThrow(CORRECT))
                        ),
                        userAnswer = cursor.getString(cursor.getColumnIndexOrThrow(USER_ANSWER)),
                        createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(CREATED_AT))
                    )
                }
            }
            callback(problems)
        }
    }
}