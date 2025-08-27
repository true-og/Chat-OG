package nl.skbotnl.chatog

import java.util.*
import nl.skbotnl.chatog.ChatUtil.legacyToMm

internal object PlayerAffix {
    fun getPrefix(uuid: UUID): String {
        var user = ChatOG.luckPerms.userManager.getUser(uuid)
        if (user == null) {
            user = ChatOG.luckPerms.userManager.loadUser(uuid).get()
        }
        val prefixNode = user.nodes.singleOrNull { node -> node.key.startsWith("prefix.1.") }
        if (prefixNode == null) return ""
        return legacyToMm(prefixNode.key.split(".").last()) + " "
    }

    fun getSuffix(uuid: UUID): String {
        var user = ChatOG.luckPerms.userManager.getUser(uuid)
        if (user == null) {
            user = ChatOG.luckPerms.userManager.loadUser(uuid).get()
        }
        val prefixNode = user.nodes.singleOrNull { node -> node.key.startsWith("suffix.1.") }
        if (prefixNode == null) return "> "
        return legacyToMm(prefixNode.key.split(".").last())
    }
}
