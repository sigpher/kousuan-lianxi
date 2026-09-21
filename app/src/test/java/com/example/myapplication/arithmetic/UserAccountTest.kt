package com.example.myapplication.arithmetic

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UserAccountTest {

    @Test
    fun hashIsDeterministicForSameSalt() {
        val hash1 = PasswordHasher.hash("1234", "abcd")
        val hash2 = PasswordHasher.hash("1234", "abcd")
        assertTrue(hash1 == hash2)
    }

    @Test
    fun hashDiffersWithDifferentSalt() {
        assertNotEquals(PasswordHasher.hash("1234", "abcd"), PasswordHasher.hash("1234", "efgh"))
    }

    @Test
    fun hashDiffersWithDifferentPassword() {
        assertNotEquals(PasswordHasher.hash("1234", "abcd"), PasswordHasher.hash("4321", "abcd"))
    }

    @Test
    fun saltIsRandom() {
        assertNotEquals(PasswordHasher.newSalt(), PasswordHasher.newSalt())
    }

    @Test
    fun verifyAcceptsCorrectPassword() {
        val salt = PasswordHasher.newSalt()
        val stored = PasswordHasher.buildStored(salt, PasswordHasher.hash("hunter2", salt))
        assertTrue(PasswordHasher.verify("hunter2", stored))
    }

    @Test
    fun verifyRejectsWrongPassword() {
        val salt = PasswordHasher.newSalt()
        val stored = PasswordHasher.buildStored(salt, PasswordHasher.hash("hunter2", salt))
        assertFalse(PasswordHasher.verify("hunter3", stored))
    }

    @Test
    fun verifyRejectsMalformedStoredValue() {
        assertFalse(PasswordHasher.verify("1234", "not-a-valid-hash"))
        assertFalse(PasswordHasher.verify("1234", ""))
    }

    @Test
    fun usernameValidation() {
        assertNotNull(UserValidation.validateUsername(""))
        assertNotNull(UserValidation.validateUsername("a"))
        assertNotNull(UserValidation.validateUsername("a".repeat(21)))
        assertNull(UserValidation.validateUsername("ab"))
        assertNull(UserValidation.validateUsername("小明"))
        assertNull(UserValidation.validateUsername("  trim  "))
    }

    @Test
    fun passwordValidation() {
        assertNotNull(UserValidation.validatePassword(""))
        assertNotNull(UserValidation.validatePassword("123"))
        assertNotNull(UserValidation.validatePassword("a".repeat(33)))
        assertNull(UserValidation.validatePassword("1234"))
    }
}