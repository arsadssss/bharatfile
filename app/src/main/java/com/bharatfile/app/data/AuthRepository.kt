package com.bharatfile.app.data

import android.content.Context
import android.content.SharedPreferences
import com.bharatfile.app.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.security.MessageDigest

interface IAuthRepository {
    val currentUser: StateFlow<UserProfile?>
    fun signUp(fullName: String, username: String, email: String, phone: String, password: String): Result<UserProfile>
    fun signIn(emailOrUsername: String, password: String): Result<UserProfile>
    fun signOut()
    fun updateProfile(fullName: String, username: String, email: String, phone: String, avatarUri: String?): Result<UserProfile>
    fun changePassword(oldPassword: String, newPassword: String): Result<Unit>
    fun recordProcessedFile(savedBytes: Long)
}

class LocalAuthRepository(context: Context) : IAuthRepository {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    override val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    init {
        loadUserSession()
    }

    private fun loadUserSession() {
        val userJson = prefs.getString(KEY_CURRENT_USER_JSON, null)
        if (userJson != null) {
            try {
                val obj = JSONObject(userJson)
                _currentUser.value = UserProfile(
                    id = obj.getString("id"),
                    fullName = obj.getString("fullName"),
                    username = obj.getString("username"),
                    email = obj.getString("email"),
                    phoneNumber = obj.getString("phoneNumber"),
                    avatarUri = obj.optString("avatarUri", "").ifEmpty { null },
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    filesProcessedCount = obj.optInt("filesProcessedCount", 0),
                    totalBytesSaved = obj.optLong("totalBytesSaved", 0L)
                )
                return
            } catch (_: Exception) {}
        }

        // Initialize with default demo/active profile for instant out-of-the-box readiness
        val defaultUser = UserProfile(
            id = "user_default_in",
            fullName = "Aarav Sharma",
            username = "aarav.sharma",
            email = "aarav.sharma@bharatfile.app",
            phoneNumber = "+91 98765 43210",
            createdAt = System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 30, // 30 days ago
            filesProcessedCount = 14,
            totalBytesSaved = 48L * 1024L * 1024L // 48 MB saved
        )
        saveUser(defaultUser, hashPassword("Bharat@123"))
        _currentUser.value = defaultUser
    }

    override fun signUp(
        fullName: String,
        username: String,
        email: String,
        phone: String,
        password: String
    ): Result<UserProfile> {
        val trimmedName = fullName.trim()
        val trimmedUser = username.trim().lowercase()
        val trimmedEmail = email.trim().lowercase()
        val trimmedPhone = phone.trim()

        if (trimmedName.isEmpty()) return Result.failure(IllegalArgumentException("Full name is required."))
        if (trimmedUser.length < 3) return Result.failure(IllegalArgumentException("Username must be at least 3 characters."))
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            return Result.failure(IllegalArgumentException("Invalid email address format."))
        }
        if (password.length < 6) return Result.failure(IllegalArgumentException("Password must be at least 6 characters."))

        val newUser = UserProfile(
            fullName = trimmedName,
            username = trimmedUser,
            email = trimmedEmail,
            phoneNumber = trimmedPhone,
            createdAt = System.currentTimeMillis(),
            filesProcessedCount = 0,
            totalBytesSaved = 0L
        )

        saveUser(newUser, hashPassword(password))
        _currentUser.value = newUser
        return Result.success(newUser)
    }

    override fun signIn(emailOrUsername: String, password: String): Result<UserProfile> {
        val key = emailOrUsername.trim().lowercase()
        val storedJson = prefs.getString(KEY_CURRENT_USER_JSON, null)
            ?: return Result.failure(IllegalArgumentException("No account found. Please sign up first."))

        val storedHash = prefs.getString(KEY_PASSWORD_HASH, "") ?: ""
        if (storedHash != hashPassword(password)) {
            return Result.failure(IllegalArgumentException("Incorrect password. Please try again."))
        }

        try {
            val obj = JSONObject(storedJson)
            val user = UserProfile(
                id = obj.getString("id"),
                fullName = obj.getString("fullName"),
                username = obj.getString("username"),
                email = obj.getString("email"),
                phoneNumber = obj.getString("phoneNumber"),
                avatarUri = obj.optString("avatarUri", "").ifEmpty { null },
                createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                filesProcessedCount = obj.optInt("filesProcessedCount", 0),
                totalBytesSaved = obj.optLong("totalBytesSaved", 0L)
            )
            _currentUser.value = user
            return Result.success(user)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override fun signOut() {
        _currentUser.value = null
        prefs.edit().remove(KEY_CURRENT_USER_JSON).apply()
    }

    override fun updateProfile(
        fullName: String,
        username: String,
        email: String,
        phone: String,
        avatarUri: String?
    ): Result<UserProfile> {
        val current = _currentUser.value ?: return Result.failure(IllegalStateException("Not logged in"))
        val updated = current.copy(
            fullName = fullName.trim(),
            username = username.trim(),
            email = email.trim(),
            phoneNumber = phone.trim(),
            avatarUri = avatarUri ?: current.avatarUri
        )
        saveUser(updated, null)
        _currentUser.value = updated
        return Result.success(updated)
    }

    override fun changePassword(oldPassword: String, newPassword: String): Result<Unit> {
        val storedHash = prefs.getString(KEY_PASSWORD_HASH, "") ?: ""
        if (storedHash.isNotEmpty() && storedHash != hashPassword(oldPassword)) {
            return Result.failure(IllegalArgumentException("Existing password does not match."))
        }
        if (newPassword.length < 6) {
            return Result.failure(IllegalArgumentException("New password must be at least 6 characters."))
        }
        prefs.edit().putString(KEY_PASSWORD_HASH, hashPassword(newPassword)).apply()
        return Result.success(Unit)
    }

    override fun recordProcessedFile(savedBytes: Long) {
        val current = _currentUser.value ?: return
        val updated = current.copy(
            filesProcessedCount = current.filesProcessedCount + 1,
            totalBytesSaved = current.totalBytesSaved + (if (savedBytes > 0) savedBytes else 0L)
        )
        saveUser(updated, null)
        _currentUser.value = updated
    }

    private fun saveUser(user: UserProfile, passwordHash: String?) {
        val obj = JSONObject().apply {
            put("id", user.id)
            put("fullName", user.fullName)
            put("username", user.username)
            put("email", user.email)
            put("phoneNumber", user.phoneNumber)
            put("avatarUri", user.avatarUri ?: "")
            put("createdAt", user.createdAt)
            put("filesProcessedCount", user.filesProcessedCount)
            put("totalBytesSaved", user.totalBytesSaved)
        }
        val editor = prefs.edit().putString(KEY_CURRENT_USER_JSON, obj.toString())
        if (passwordHash != null) {
            editor.putString(KEY_PASSWORD_HASH, passwordHash)
        }
        editor.apply()
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(("bharat_salt_" + password).toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val PREFS_NAME = "bharatfile_auth_prefs"
        private const val KEY_CURRENT_USER_JSON = "current_user_json"
        private const val KEY_PASSWORD_HASH = "password_hash"

        @Volatile
        private var instance: LocalAuthRepository? = null

        fun getInstance(context: Context): LocalAuthRepository {
            return instance ?: synchronized(this) {
                instance ?: LocalAuthRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
