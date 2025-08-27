package nl.skbotnl.chatog

import java.util.*
import kotlin.concurrent.read
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.trueog.utilitiesog.UtilitiesOG
import nl.skbotnl.chatog.ChatOG.Companion.config
import nl.skbotnl.chatog.ChatOG.Companion.discordBridgeLock
import nl.skbotnl.chatog.ChatUtil.legacyToMm
import nl.skbotnl.chatog.commands.TranslateMessage
import org.bukkit.Bukkit
import org.bukkit.entity.Player

@OptIn(DelicateCoroutinesApi::class)
internal object ChatSystem {
    enum class ChatType {
        GENERAL_CHAT,
        STAFF_CHAT,
        PREMIUM_CHAT,
        DEVELOPER_CHAT,
    }

    var inChat: MutableMap<UUID, ChatType> = HashMap()

    fun sendMessageInStaffChat(player: Player, text: String) {
        var playerPartString = ChatUtil.getPlayerPartString(player)
        playerPartString = "&cSTAFF | $playerPartString"

        if (config.staffDiscordEnabled) {
            val discordMessageString = ChatUtil.convertEmojis(text)

            ChatOG.scope.launch {
                discordBridgeLock.read {
                    ChatOG.discordBridge?.sendStaffMessage(discordMessageString, playerPartString, player.uniqueId)
                }
            }
        }

        val messageComponent = ChatUtil.processText(text, player)
        if (messageComponent == null) {
            return
        }

        val chatComponent =
            UtilitiesOG.trueogColorize(legacyToMm("$playerPartString<reset>${PlayerAffix.getSuffix(player.uniqueId)}"))

        var textComponent = Component.join(JoinConfiguration.noSeparators(), chatComponent, messageComponent)
        textComponent =
            textComponent.hoverEvent(
                HoverEvent.hoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    UtilitiesOG.trueogColorize("<green>Click to translate this message"),
                )
            )

        val randomUUID = UUID.randomUUID()
        textComponent =
            textComponent.clickEvent(
                ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/translatemessage $randomUUID 1")
            )

        TranslateMessage.chatMessages[randomUUID] = TranslateMessage.SentChatMessage(text, player)

        for (p in Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("chat-og.staff")) {
                p.sendMessage(textComponent)
            }
        }
    }

    fun sendMessageInPremiumChat(player: Player, text: String) {
        var playerPartString = ChatUtil.getPlayerPartString(player)
        playerPartString = "&aPREMIUM | $playerPartString"

        if (config.premiumDiscordEnabled) {
            val discordMessageString = ChatUtil.convertEmojis(text)

            ChatOG.scope.launch {
                discordBridgeLock.read {
                    ChatOG.discordBridge?.sendPremiumMessage(discordMessageString, playerPartString, player.uniqueId)
                }
            }
        }

        val messageComponent = ChatUtil.processText(text, player)
        if (messageComponent == null) {
            return
        }

        val chatComponent =
            UtilitiesOG.trueogColorize(legacyToMm("$playerPartString<reset>${PlayerAffix.getSuffix(player.uniqueId)}"))

        var textComponent = Component.join(JoinConfiguration.noSeparators(), chatComponent, messageComponent)
        textComponent =
            textComponent.hoverEvent(
                HoverEvent.hoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    UtilitiesOG.trueogColorize("<green>Click to translate this message"),
                )
            )

        val randomUUID = UUID.randomUUID()
        textComponent =
            textComponent.clickEvent(
                ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/translatemessage $randomUUID 1")
            )

        TranslateMessage.chatMessages[randomUUID] = TranslateMessage.SentChatMessage(text, player)

        for (p in Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("chat-og.premium")) {
                p.sendMessage(textComponent)
            }
        }
    }

    fun sendMessageInDeveloperChat(player: Player, text: String) {
        var playerPartString = ChatUtil.getPlayerPartString(player)
        playerPartString = "<aqua>DEVELOPER | $playerPartString"

        if (config.premiumDiscordEnabled) {
            val discordMessageString = ChatUtil.convertEmojis(text)

            ChatOG.scope.launch {
                discordBridgeLock.read {
                    ChatOG.discordBridge?.sendDeveloperMessage(discordMessageString, playerPartString, player.uniqueId)
                }
            }
        }

        val messageComponent = ChatUtil.processText(text, player)
        if (messageComponent == null) {
            return
        }

        val chatComponent =
            UtilitiesOG.trueogColorize(legacyToMm("$playerPartString<reset>${PlayerAffix.getSuffix(player.uniqueId)}"))

        var textComponent = Component.join(JoinConfiguration.noSeparators(), chatComponent, messageComponent)
        textComponent =
            textComponent.hoverEvent(
                HoverEvent.hoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    UtilitiesOG.trueogColorize("<green>Click to translate this message"),
                )
            )

        val randomUUID = UUID.randomUUID()
        textComponent =
            textComponent.clickEvent(
                ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/translatemessage $randomUUID 1")
            )

        TranslateMessage.chatMessages[randomUUID] = TranslateMessage.SentChatMessage(text, player)

        for (p in Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("chat-og.developer")) {
                p.sendMessage(textComponent)
            }
        }
    }
}
