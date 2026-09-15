package com.subhunt.app.security

import android.content.Context
import android.util.Base64
import androidx.biometric.BiometricManager
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.subhunt.app.data.local.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

@Singleton
class AppLockManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val PIN_CIPHERTEXT = stringPreferencesKey("app_lock_pin")
        val BIOMETRIC_ALLOWED = booleanPreferencesKey("app_lock_biometric")
        val FAILED_ATTEMPTS = intPreferencesKey("app_lock_failed_attempts")
        val COOLDOWN_UNTIL = longPreferencesKey("app_lock_cooldown_until")
        val BACKGROUNDED_AT = longPreferencesKey("app_lock_backgrounded_at")
    }

    val lockEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.LOCK_ENABLED] == true && prefs[Keys.PIN_CIPHERTEXT] != null
    }

    val biometricAllowed: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.BIOMETRIC_ALLOWED] ?: false
    }

    private val _sessionUnlocked = MutableStateFlow(false)
    val sessionUnlocked: StateFlow<Boolean> = _sessionUnlocked.asStateFlow()

    val backgroundedAt: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[Keys.BACKGROUNDED_AT] ?: 0L
    }

    suspend fun setBackgroundedAt(time: Long) {
        context.dataStore.edit { prefs ->
            prefs[Keys.BACKGROUNDED_AT] = time
        }
    }

    fun markUnlocked() {
        _sessionUnlocked.value = true
    }

    fun markLocked() {
        _sessionUnlocked.value = false
    }

    fun canUseBiometric(): Boolean {
        val result = BiometricManager.from(context).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.BIOMETRIC_WEAK
        )
        return result == BiometricManager.BIOMETRIC_SUCCESS
    }

    suspend fun setPin(pin: String) {
        require(pin.length in 4..6 && pin.all { it.isDigit() })
        val (iv, cipher) = encrypt(pin.toByteArray())
        context.dataStore.edit { prefs ->
            prefs[Keys.PIN_CIPHERTEXT] =
                Base64.encodeToString(iv, Base64.NO_WRAP) + ":" +
                Base64.encodeToString(cipher, Base64.NO_WRAP)
            prefs[Keys.LOCK_ENABLED] = true
        }
        markUnlocked()
    }

    suspend fun checkPin(pin: String): Boolean {
        val raw = context.dataStore.data.map { it[Keys.PIN_CIPHERTEXT] }.firstOrNull()
            ?: return false
        val parts = raw.split(":")
        if (parts.size != 2) return false
        return try {
            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val cipher = Base64.decode(parts[1], Base64.NO_WRAP)
            val plain = decrypt(iv, cipher)
            MessageDigest.isEqual(plain, pin.toByteArray())
        } catch (e: Exception) {
            false
        }
    }

    suspend fun setBiometricAllowed(allowed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.BIOMETRIC_ALLOWED] = allowed && canUseBiometric()
        }
    }

    suspend fun disableLock() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.PIN_CIPHERTEXT)
            prefs[Keys.LOCK_ENABLED] = false
            prefs[Keys.BIOMETRIC_ALLOWED] = false
            prefs[Keys.FAILED_ATTEMPTS] = 0
            prefs[Keys.COOLDOWN_UNTIL] = 0L
        }
        markUnlocked()
    }

    val failedAttempts: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.FAILED_ATTEMPTS] ?: 0
    }

    val cooldownUntil: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[Keys.COOLDOWN_UNTIL] ?: 0L
    }

    suspend fun setFailedAttempts(attempts: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.FAILED_ATTEMPTS] = attempts
        }
    }

    suspend fun setCooldownUntil(until: Long) {
        context.dataStore.edit { prefs ->
            prefs[Keys.COOLDOWN_UNTIL] = until
        }
    }

    private fun getOrCreateKey(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.let {
            return it.secretKey
        }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(false)
                .setRandomizedEncryptionRequired(true)
                .setUnlockedDeviceRequired(true)
                .build()
        )
        return generator.generateKey()
    }

    private fun encrypt(plain: ByteArray): Pair<ByteArray, ByteArray> {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        return cipher.iv to cipher.doFinal(plain)
    }

    private fun decrypt(iv: ByteArray, cipherText: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateKey(),
            GCMParameterSpec(128, iv)
        )
        return cipher.doFinal(cipherText)
    }

    companion object {
        private const val KEY_ALIAS = "subhunt_pin_key"
    }
}
