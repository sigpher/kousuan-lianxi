package com.example.myapplication.arithmetic

import java.security.MessageDigest
import java.security.SecureRandom

class UserAccount(
    val id: Int,
    val username: String,
    val avatarPath: String?
)

object PasswordHasher {

    private val hexChars = "0123456789abcdef".toCharArray()

    fun newSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.toHex()
    }

    fun hash(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest("$salt:$password".toByteArray(Charsets.UTF_8))
        return digest.toHex()
    }

    fun verify(password: String, stored: String): Boolean {
        val split = stored.split(":", limit = 2)
        if (split.size != 2) return false
        return hash(password, split[0]) == split[1]
    }

    fun buildStored(salt: String, hash: String): String = "$salt:$hash"

    private fun ByteArray.toHex(): String = buildString(size * 2) {
        for (b in this@toHex) {
            val v = b.toInt() and 0xFF
            append(hexChars[v ushr 4])
            append(hexChars[v and 0x0F])
        }
    }
}

object UserValidation {

    fun validateUsername(username: String): String? {
        val trimmed = username.trim()
        if (trimmed.isEmpty()) return "用户名不能为空"
        if (trimmed.length < 2 || trimmed.length > 20) return "用户名长度需为 2~20 个字符"
        return null
    }

    fun validatePassword(password: String): String? {
        if (password.isEmpty()) return "密码不能为空"
        if (password.length < 4 || password.length > 32) return "密码长度需为 4~32 个字符"
        return null
    }
}