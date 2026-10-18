package com.devin.virtuetracker.data

import java.util.UUID

/**
 * A single item that the user tracks each day (e.g. one of Franklin's virtues
 * or a custom habit the user adds).
 */
data class TrackedItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val subItems: List<String> = emptyList(),
    val active: Boolean = true
)

/**
 * Benjamin Franklin's 13 virtues, with his original precepts, used as the
 * default set of items the app ships with.
 */
object DefaultVirtues {
    val list: List<TrackedItem> = listOf(
        TrackedItem(id = "virtue_temperance", name = "Temperance",
            description = "Eat not to dullness; drink not to elevation."),
        TrackedItem(id = "virtue_silence", name = "Silence",
            description = "Speak not but what may benefit others or yourself; avoid trifling conversation."),
        TrackedItem(id = "virtue_order", name = "Order",
            description = "Let all your things have their places; let each part of your business have its time."),
        TrackedItem(id = "virtue_resolution", name = "Resolution",
            description = "Resolve to perform what you ought; perform without fail what you resolve."),
        TrackedItem(id = "virtue_frugality", name = "Frugality",
            description = "Make no expense but to do good to others or yourself; i.e., waste nothing."),
        TrackedItem(id = "virtue_industry", name = "Industry",
            description = "Lose no time; be always employ'd in something useful; cut off all unnecessary actions."),
        TrackedItem(id = "virtue_sincerity", name = "Sincerity",
            description = "Use no hurtful deceit; think innocently and justly, and, if you speak, speak accordingly."),
        TrackedItem(id = "virtue_justice", name = "Justice",
            description = "Wrong none by doing injuries or omitting the benefits that are your duty."),
        TrackedItem(id = "virtue_moderation", name = "Moderation",
            description = "Avoid extremes; forbear resenting injuries so much as you think they deserve."),
        TrackedItem(id = "virtue_cleanliness", name = "Cleanliness",
            description = "Tolerate no uncleanliness in body, clothes, or habitation."),
        TrackedItem(id = "virtue_tranquillity", name = "Tranquillity",
            description = "Be not disturbed at trifles, or at accidents common or unavoidable."),
        TrackedItem(id = "virtue_chastity", name = "Chastity",
            description = "Rarely use venery but for health or offspring; never to dullness, weakness, or the injury of your own or another's peace or reputation."),
        TrackedItem(id = "virtue_humility", name = "Humility",
            description = "Imitate Jesus and Socrates.")
    )
}
