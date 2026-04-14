package com.johnny.tier1bankdemo.deepLink

import android.content.Intent

/**
 * Parses incoming deep link Intents for verification parameters.
 *
 * Expected URI format:
 *   tier1bank://verify?token=abc123&userId=user_42
 *
 * Usage in MainActivity (or wherever you handle new intents):
 *   val token  = VerificationDeepLinkParser.parseToken(intent)
 *   val userId = VerificationDeepLinkParser.parseUserId(intent)
 */
object VerificationDeepLinkParser {

    fun parseToken(intent: Intent): String? =
        intent.data?.getQueryParameter("token")

    fun parseUserId(intent: Intent): String? =
        intent.data?.getQueryParameter("userId")
}
