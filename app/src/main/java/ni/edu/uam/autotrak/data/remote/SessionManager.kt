package ni.edu.uam.autotrak.data.remote

import android.content.Context

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("autotrak_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_USER_ID = "user_id"
    }

    fun saveAuthData(token: String, userId: Long) {
        prefs.edit().apply {
            putString(KEY_TOKEN, token)
            putLong(KEY_USER_ID, userId)
            apply()
        }
    }

    fun saveUserId(userId: Long) {
        prefs.edit().putLong(KEY_USER_ID, userId).apply()
    }

    fun getUserId(): Long {
        return prefs.getLong(KEY_USER_ID, -1L)
    }

    fun getAuthHeader(): String? {
        val token = prefs.getString(KEY_TOKEN, null)
        if (token != null) {
            return "Bearer $token"
        }
        return null
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.contains(KEY_TOKEN)
    }

    fun isUserSelected(): Boolean {
        return prefs.getLong(KEY_USER_ID, -1L) != -1L
    }
}
