package nl.skbotnl.chatog.util

import java.util.*
import nl.skbotnl.chatog.ChatOG.Companion.luckPerms
import nl.skbotnl.chatog.util.ChatUtil.legacyToMm

internal object PlayerAffix {
    fun getPrefix(uuid: UUID): String {
        val prefix = getMetaData(uuid).prefix ?: return ""
        return legacyToMm(prefix).trimEnd() + " "
    }

    fun getSuffix(uuid: UUID): String {
        val suffix = getMetaData(uuid).suffix ?: return ""
        return " " + legacyToMm(suffix).trim()
    }

    private fun getMetaData(uuid: UUID) =
        with(luckPerms.userManager.getUser(uuid) ?: luckPerms.userManager.loadUser(uuid).join()) {
            val queryOptions = luckPerms.contextManager.getQueryOptions(this).orElse(queryOptions)
            cachedData.getMetaData(queryOptions)
        }
}
