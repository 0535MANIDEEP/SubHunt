package com.subhunt.app.billing

// Copyright (c) 2026 Manideep Daram. All rights reserved.
// Activation code generator and verifier.
// Each code is tied to an email and plan type. Can't be shared or guessed.

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.security.MessageDigest

object ActivationCodeGenerator {

    private const val SECRET = "SubHunt2026SecureKey_Xk9mP2vL"
    private const val CODE_LENGTH = 12

    private const val PLAN_MONTHLY = "monthly"
    private const val PLAN_YEARLY = "yearly"
    private const val PLAN_LIFETIME = "lifetime"

    fun generateCode(email: String, plan: String): String {
        val cleanEmail = email.trim().lowercase()
        val cleanPlan = plan.trim().lowercase()

        val timestamp = System.currentTimeMillis() / 1000
        val payload = "$cleanEmail|$cleanPlan|$timestamp"
        val hmac = hmacSha256(payload)

        val planChar = when (cleanPlan) {
            PLAN_MONTHLY -> "M"
            PLAN_YEARLY -> "Y"
            PLAN_LIFETIME -> "L"
            else -> "X"
        }

        val raw = "$planChar$hmac"
        return raw.take(CODE_LENGTH).uppercase()
    }

    fun verify(code: String, email: String): Boolean {
        val cleanCode = code.trim().uppercase()
        val cleanEmail = email.trim().lowercase()

        if (cleanCode.length != CODE_LENGTH) return false

        val planChar = cleanCode.first()
        val plan = when (planChar) {
            'M' -> PLAN_MONTHLY
            'Y' -> PLAN_YEARLY
            'L' -> PLAN_LIFETIME
            else -> return false
        }

        val testCode = generateCode(cleanEmail, plan)
        return cleanCode == testCode
    }

    fun getPlanFromCode(code: String): String {
        return when (code.firstOrNull()?.uppercaseChar()) {
            'M' -> PLAN_MONTHLY
            'Y' -> PLAN_YEARLY
            'L' -> PLAN_LIFETIME
            else -> "unknown"
        }
    }

    fun getPlanDisplayName(plan: String): String {
        return when (plan) {
            PLAN_MONTHLY -> "Monthly Pro"
            PLAN_YEARLY -> "Yearly Pro"
            PLAN_LIFETIME -> "Lifetime Pro"
            else -> "Pro"
        }
    }

    fun getPlanPrice(plan: String): String {
        return when (plan) {
            PLAN_MONTHLY -> "$4.99/month"
            PLAN_YEARLY -> "$29.99/year"
            PLAN_LIFETIME -> "$79.99 one-time"
            else -> ""
        }
    }

    private fun hmacSha256(data: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(SECRET.toByteArray(), "HmacSHA256"))
        val hash = mac.doFinal(data.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    fun generateForAdmin(email: String, plan: String): String {
        return generateCode(email, plan)
    }
}
