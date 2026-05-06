package nl.skbotnl.chatog.util

import java.util.*
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import nl.skbotnl.chatog.ChatOG.Companion.luckPerms

internal object PlayerUtils {
    fun getPrefix(uuid: UUID): String {
        val prefix = getMetaData(uuid).prefix?.trim() ?: return ""
        return if (prefix.endsWith(' ')) prefix else "$prefix "
    }

    fun getSuffix(uuid: UUID) = getMetaData(uuid).suffix?.trim() ?: ""

    fun getMessageColor(uuid: UUID): TextColor {
        return getMetaData(uuid).getMetaValue("chat-og.messageColor")?.lowercase()?.let {
            NamedTextColor.NAMES.value(it)
        } ?: NamedTextColor.GRAY
    }

    private fun getMetaData(uuid: UUID) =
        with(luckPerms.userManager.getUser(uuid) ?: luckPerms.userManager.loadUser(uuid).join()) { cachedData.metaData }
}
