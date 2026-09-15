package com.subhunt.app.notification

import com.subhunt.app.R

data class ReminderSound(
    val id: String,
    val label: String,
    val tagline: String,
    val resId: Int?,
    val isPro: Boolean
)

object ReminderSounds {
    const val SYSTEM_DEFAULT_ID = "system"

    val ALL: List<ReminderSound> = listOf(
        ReminderSound(
            id = SYSTEM_DEFAULT_ID,
            label = "System Default",
            tagline = "Your phone's notification sound",
            resId = null,
            isPro = false
        ),
        ReminderSound(
            id = "chime",
            label = "Soft Chime",
            tagline = "Gentle bell, easy mornings",
            resId = R.raw.tone_chime,
            isPro = false
        ),
        ReminderSound(
            id = "pop",
            label = "Pop",
            tagline = "Quick playful blip",
            resId = R.raw.tone_pop,
            isPro = false
        ),
        ReminderSound(
            id = "dingdong",
            label = "Ding-Dong",
            tagline = "Classic two-tone",
            resId = R.raw.tone_dingdong,
            isPro = false
        ),
        ReminderSound(
            id = "kaching",
            label = "Ka-Ching",
            tagline = "Hear your money move",
            resId = R.raw.tone_kaching,
            isPro = true
        ),
        ReminderSound(
            id = "drama",
            label = "Drama Alert",
            tagline = "For bills that hurt",
            resId = R.raw.tone_drama,
            isPro = true
        ),
        ReminderSound(
            id = "jackpot",
            label = "Jackpot",
            tagline = "Celebrate every charge",
            resId = R.raw.tone_jackpot,
            isPro = true
        )
    )

    val FREE: List<ReminderSound> = ALL.filter { !it.isPro }
    val PRO: List<ReminderSound> = ALL.filter { it.isPro }

    fun byId(id: String?): ReminderSound =
        ALL.firstOrNull { it.id == id } ?: ALL.first()

    fun effective(soundId: String?, isProUser: Boolean): ReminderSound {
        val sound = byId(soundId)
        return if (sound.isPro && !isProUser) byId(SYSTEM_DEFAULT_ID) else sound
    }
}
