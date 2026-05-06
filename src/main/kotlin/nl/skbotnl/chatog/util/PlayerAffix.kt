package nl.skbotnl.chatog.util

import java.util.*
import nl.skbotnl.chatog.ChatOG.Companion.luckPerms
import nl.skbotnl.chatog.util.ChatUtil.legacyToMm

internal object PlayerAffix {
    fun getPrefix(uuid: UUID): String {
        val raw = getMetaData(uuid).prefix ?: return ""
        val cleaned = stripOuterSpaces(legacyToMm(raw))
        if (ChatUtil.stripFormatting(cleaned).isEmpty()) return ""
        return "$cleaned "
    }

    fun getSuffix(uuid: UUID): String {
        val raw = getMetaData(uuid).suffix ?: return ""
        val cleaned = stripOuterSpaces(legacyToMm(raw))
        if (ChatUtil.stripFormatting(cleaned).isEmpty()) return ""
        return " $cleaned"
    }

    private fun stripOuterSpaces(mm: String): String = stripTrailingSpaces(stripLeadingSpaces(mm))

    private fun stripLeadingSpaces(mm: String): String {
        val keptTags = StringBuilder()
        var i = 0
        while (i < mm.length) {
            val c = mm[i]
            when {
                c.isWhitespace() -> i++
                c == '<' -> {
                    val close = mm.indexOf('>', i)
                    if (close < 0) break
                    keptTags.append(mm, i, close + 1)
                    i = close + 1
                }
                else -> return keptTags.toString() + mm.substring(i)
            }
        }
        return keptTags.toString()
    }

    private fun stripTrailingSpaces(mm: String): String {
        val keptTags = StringBuilder()
        var end = mm.length
        while (end > 0) {
            val c = mm[end - 1]
            when {
                c.isWhitespace() -> end--
                c == '>' -> {
                    val open = mm.lastIndexOf('<', end - 1)
                    if (open < 0) break
                    keptTags.insert(0, mm.substring(open, end))
                    end = open
                }
                else -> return mm.substring(0, end) + keptTags.toString()
            }
        }
        return keptTags.toString()
    }

    private fun getMetaData(uuid: UUID) =
        with(luckPerms.userManager.getUser(uuid) ?: luckPerms.userManager.loadUser(uuid).join()) {
            val queryOptions = luckPerms.contextManager.getQueryOptions(this).orElse(queryOptions)
            cachedData.getMetaData(queryOptions)
        }
}
