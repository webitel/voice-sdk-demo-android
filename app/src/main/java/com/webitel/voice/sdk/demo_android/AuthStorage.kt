package com.webitel.voice.sdk.demo_android

import android.content.Context
import android.content.SharedPreferences


class AuthStorage(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_HOST = "key_host"
        private const val KEY_TOKEN = "key_token"
        private const val KEY_ISSUER = "key_issuer"
        private const val KEY_USER_NAME = "key_user_name"
    }


    fun saveAuthInfo(info: AuthInfo) {
        prefs.edit()
            .putString(KEY_HOST, info.host)
            .putString(KEY_TOKEN, info.token)
            .putString(KEY_ISSUER, info.issuer)
            .putString(KEY_USER_NAME, info.userName)
            .apply()
    }


    fun getAuthInfo(): AuthInfo? {
        val host = prefs.getString(KEY_HOST, null)
        val token = prefs.getString(KEY_TOKEN, null)
        val issuer = prefs.getString(KEY_ISSUER, null)
        val userName = prefs.getString(KEY_USER_NAME, null)

        return if (host != null && token != null && issuer != null && userName != null) {
            AuthInfo(host, token, issuer, userName)
        } else null
    }


    fun clear() {
        prefs.edit().clear().apply()
    }


    fun getHost(): String? = prefs.getString(KEY_HOST, null)
    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)
    fun getIssuer(): String? = prefs.getString(KEY_ISSUER, null)
    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)
}