package com.example.myapplication.arithmetic

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import com.example.myapplication.arithmetic.ArithmeticDbHelper.Companion.AVATAR_PATH
import com.example.myapplication.arithmetic.ArithmeticDbHelper.Companion.GUEST_USER_ID
import com.example.myapplication.arithmetic.ArithmeticDbHelper.Companion.PASSWORD_HASH
import com.example.myapplication.arithmetic.ArithmeticDbHelper.Companion.USERNAME
import com.example.myapplication.arithmetic.ArithmeticDbHelper.Companion.USERS_TABLE
import com.example.myapplication.arithmetic.ArithmeticDbHelper.Companion.USER_CREATED_AT
import com.example.myapplication.arithmetic.ArithmeticDbHelper.Companion.USER_ID
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AccountRepository(context: Context) {

    private val appContext = context.applicationContext
    private val dbHelper = ArithmeticDbHelper(appContext)
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val prefs = appContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun currentUserId(): Int = prefs.getInt(KEY_CURRENT_USER_ID, GUEST_USER_ID)

    fun isGuest(): Boolean = currentUserId() == GUEST_USER_ID

    fun avatarDirectory(): File =
        File(appContext.filesDir, "avatars").apply { mkdirs() }

    fun stagedAvatarFile(): File =
        File(avatarDirectory(), "_new_${System.currentTimeMillis()}.jpg")

    fun finalAvatarFile(userId: Int): File = File(avatarDirectory(), "avatar_$userId.jpg")

    fun currentUser(callback: (UserAccount?) -> Unit) {
        executor.execute {
            callback(findUser(currentUserId()))
        }
    }

    fun register(
        username: String,
        password: String,
        avatarPath: String?,
        callback: (Boolean, String?) -> Unit
    ) {
        executor.execute {
            try {
                if (usernameExists(username)) {
                    callback(false, "用户名已被使用")
                    return@execute
                }
                val salt = PasswordHasher.newSalt()
                val hash = PasswordHasher.hash(password, salt)
                val db = dbHelper.writableDatabase
                val values = ContentValues().apply {
                    put(USERNAME, username.trim())
                    put(PASSWORD_HASH, PasswordHasher.buildStored(salt, hash))
                    put(USER_CREATED_AT, System.currentTimeMillis())
                    avatarPath?.let { put(AVATAR_PATH, it) }
                }
                val id = db.insertOrThrow(USERS_TABLE, null, values).toInt()
                val finalPath = commitAvatar(id, avatarPath)
                if (finalPath != null) {
                    db.update(
                        USERS_TABLE,
                        ContentValues().apply { put(AVATAR_PATH, finalPath) },
                        "$USER_ID=?",
                        arrayOf(id.toString())
                    )
                }
                setCurrentUserId(id)
                callback(true, null)
            } catch (e: SQLiteConstraintException) {
                callback(false, "用户名已被使用")
            }
        }
    }

    fun login(username: String, password: String, callback: (Boolean, String?) -> Unit) {
        executor.execute {
            val db = dbHelper.readableDatabase
            db.query(
                USERS_TABLE,
                null,
                "$USERNAME=?",
                arrayOf(username.trim()),
                null,
                null,
                null
            ).use { cursor ->
                if (!cursor.moveToFirst()) {
                    callback(false, "用户不存在")
                    return@execute
                }
                val stored = cursor.getString(cursor.getColumnIndexOrThrow(PASSWORD_HASH))
                if (!PasswordHasher.verify(password, stored)) {
                    callback(false, "密码错误")
                    return@execute
                }
                setCurrentUserId(cursor.getInt(cursor.getColumnIndexOrThrow(USER_ID)))
                callback(true, null)
            }
        }
    }

    fun changeAvatar(userId: Int, stagedPath: String?, callback: (String?) -> Unit) {
        executor.execute {
            val db = dbHelper.writableDatabase
            val finalPath = commitAvatar(userId, stagedPath)
            if (finalPath != null) {
                db.update(
                    USERS_TABLE,
                    ContentValues().apply { put(AVATAR_PATH, finalPath) },
                    "$USER_ID=?",
                    arrayOf(userId.toString())
                )
            }
            callback(finalPath)
        }
    }

    fun logout() {
        setCurrentUserId(GUEST_USER_ID)
    }

    private fun commitAvatar(userId: Int, stagedPath: String?): String? {
        if (stagedPath.isNullOrEmpty()) return null
        val staged = File(stagedPath)
        val final = finalAvatarFile(userId)
        if (final.exists()) final.delete()
        return if (staged.renameTo(final)) final.path else {
            staged.delete()
            null
        }
    }

    private fun usernameExists(username: String): Boolean {
        val db = dbHelper.readableDatabase
        db.query(
            USERS_TABLE,
            null,
            "$USERNAME=?",
            arrayOf(username.trim()),
            null,
            null,
            null
        ).use { return it.count > 0 }
    }

    private fun findUser(id: Int): UserAccount? {
        if (id == GUEST_USER_ID) return null
        val db = dbHelper.readableDatabase
        db.query(
            USERS_TABLE,
            null,
            "$USER_ID=?",
            arrayOf(id.toString()),
            null,
            null,
            null
        ).use { cursor ->
            if (!cursor.moveToFirst()) return null
            val avatar = if (cursor.isNull(cursor.getColumnIndexOrThrow(AVATAR_PATH))) {
                null
            } else {
                cursor.getString(cursor.getColumnIndexOrThrow(AVATAR_PATH))
            }
            return UserAccount(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(USER_ID)),
                username = cursor.getString(cursor.getColumnIndexOrThrow(USERNAME)),
                avatarPath = avatar
            )
        }
    }

    private fun setCurrentUserId(id: Int) {
        prefs.edit().putInt(KEY_CURRENT_USER_ID, id).apply()
    }

    private companion object {
        const val KEY_CURRENT_USER_ID = "current_user_id"
    }
}