package nl.skbotnl.chatog

import io.papermc.paper.advancement.AdvancementDisplay.Frame.*
import io.papermc.paper.event.player.AsyncChatEvent
import java.util.*
import kotlin.concurrent.read
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import me.clip.placeholderapi.PlaceholderAPI
import net.ess3.api.events.VanishStatusChangeEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.trueog.utilitiesog.UtilitiesOG
import nl.skbotnl.chatog.ChatOG.Companion.config
import nl.skbotnl.chatog.ChatOG.Companion.discordBridgeLock
import nl.skbotnl.chatog.ChatSystem.ChatType
import nl.skbotnl.chatog.ChatUtil.legacyToMm
import nl.skbotnl.chatog.commands.TranslateMessage
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.*
import org.bukkit.event.server.BroadcastMessageEvent
import xyz.jpenilla.announcerplus.listener.JoinQuitListener

@OptIn(DelicateCoroutinesApi::class)
internal class Events : Listener {
    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        if (!config.discordEnabled) {
            return
        }
        if (ChatOG.essentials.getUser(event.player).isVanished && event.joinMessage() !is TextComponent) {
            return
        }

        val playerPartString = ChatUtil.getPlayerPartString(event.player)

        ChatOG.scope.launch {
            discordBridgeLock.read {
                ChatOG.discordBridge?.sendEmbed(
                    "$playerPartString has joined the game. ${
                        Bukkit.getOnlinePlayers().count()
                    } player(s) online.",
                    event.player.uniqueId,
                    0x00FF00,
                )
            }
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        if (!config.discordEnabled) {
            return
        }
        if (ChatOG.essentials.getUser(event.player).isVanished) {
            return
        }

        val playerPartString = ChatUtil.getPlayerPartString(event.player)

        ChatOG.scope.launch {
            discordBridgeLock.read {
                ChatOG.discordBridge?.sendEmbed(
                    "$playerPartString has left the game. ${
                        Bukkit.getOnlinePlayers().count() - 1
                    } player(s) online.",
                    event.player.uniqueId,
                    0xFF0000,
                )
            }
        }
    }

    @EventHandler
    fun onKick(event: PlayerKickEvent) {
        if (!config.discordEnabled) {
            return
        }
        if (ChatOG.essentials.getUser(event.player).isVanished) {
            return
        }

        val playerPartString = ChatUtil.getPlayerPartString(event.player)

        val reason = PlainTextComponentSerializer.plainText().serialize(event.reason())

        ChatOG.scope.launch {
            discordBridgeLock.read {
                ChatOG.discordBridge?.sendEmbed(
                    "$playerPartString was kicked with reason: \"${reason}\". ${
                        Bukkit.getOnlinePlayers().count() - 1
                    } player(s) online.",
                    event.player.uniqueId,
                    0xFF0000,
                )
            }
        }
    }

    @EventHandler
    fun onAdvancement(event: PlayerAdvancementDoneEvent) {
        if (!config.discordEnabled) {
            return
        }
        if (ChatOG.essentials.getUser(event.player).isVanished) {
            return
        }

        val playerPartString = ChatUtil.getPlayerPartString(event.player)

        val advancementTitleKey = event.advancement.display?.title() ?: return
        val advancementTitle = PlainTextComponentSerializer.plainText().serialize(advancementTitleKey)

        val advancementMessage =
            when (event.advancement.display?.frame()) {
                GOAL -> "has reached the goal [$advancementTitle]"
                TASK -> "has made the advancement [$advancementTitle]"
                CHALLENGE -> "has completed the challenge [$advancementTitle]"
                else -> {
                    return
                }
            }

        ChatOG.scope.launch {
            discordBridgeLock.read {
                ChatOG.discordBridge?.sendEmbed(
                    "$playerPartString $advancementMessage.",
                    event.player.uniqueId,
                    0xFFFF00,
                )
            }
        }
    }

    @EventHandler
    fun onBroadcast(event: BroadcastMessageEvent) {
        if (!config.discordEnabled) {
            return
        }
        if (event.message() !is TextComponent) {
            return
        }

        val content = (event.message() as TextComponent).content()
        if (content == "") {
            return
        }

        ChatOG.scope.launch {
            discordBridgeLock.read { ChatOG.discordBridge?.sendMessage(content, "[Server] Broadcast", null) }
        }
    }

    @EventHandler
    fun onChat(event: AsyncChatEvent) {
        if (event.isCancelled) return
        event.isCancelled = true

        val eventMessage = event.message() as TextComponent

        when (ChatSystem.inChat[event.player.uniqueId]) {
            ChatType.STAFF_CHAT -> {
                ChatSystem.sendMessageInStaffChat(event.player, eventMessage.content())
                return
            }
            ChatType.PREMIUM_CHAT -> {
                ChatSystem.sendMessageInPremiumChat(event.player, eventMessage.content())
                return
            }
            ChatType.DEVELOPER_CHAT -> {
                ChatSystem.sendMessageInDeveloperChat(event.player, eventMessage.content())
                return
            }
            else -> {}
        }

        val discordMessageString = ChatUtil.convertEmojis(eventMessage.content())
        ChatOG.scope.launch {
            discordBridgeLock.read { ChatOG.discordBridge?.sendMessage(discordMessageString, event.player) }
        }

        val playerPart = ChatUtil.getPlayerPart(event.player, true)

        val messageComponent = ChatUtil.processText(eventMessage.content(), event.player)
        if (messageComponent == null) {
            return
        }

        var textComponent = Component.join(JoinConfiguration.noSeparators(), playerPart, messageComponent)
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

        event.viewers().forEach { it.sendMessage(textComponent) }

        ChatUtil.dingForMentions(event.player.uniqueId, messageComponent)

        TranslateMessage.chatMessages[randomUUID] =
            TranslateMessage.SentChatMessage(eventMessage.content(), event.player)
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        if (!config.discordEnabled) {
            return
        }
        if (ChatOG.essentials.getUser(event.player).isVanished) {
            return
        }

        if (event.deathMessage() is TextComponent) {
            ChatOG.scope.launch {
                discordBridgeLock.read {
                    ChatOG.discordBridge?.sendEmbed(
                        (event.deathMessage() as TextComponent).content(),
                        event.player.uniqueId,
                        0xFF0000,
                    )
                }
            }
            return
        }

        var nameString = "${PlayerAffix.getPrefix(event.player.uniqueId)}${event.player.name}"

        if (PlaceholderAPI.setPlaceholders(event.player, "%simpleclans_clan_color_tag%") != "") {
            nameString = PlaceholderAPI.setPlaceholders(event.player, "&8[%simpleclans_clan_color_tag%&8] $nameString")
        }
        val nameComponent = UtilitiesOG.trueogColorize(legacyToMm(nameString))

        var oldDeathMessage = event.deathMessage() as TranslatableComponent
        oldDeathMessage = oldDeathMessage.color(TextColor.color(16755200))
        oldDeathMessage = oldDeathMessage.append(Component.text("."))

        val argList = oldDeathMessage.args().toMutableList()
        argList[0] = nameComponent
        val deathMessage = oldDeathMessage.args(argList)

        event.deathMessage(deathMessage)

        val translatedDeathMessage = PlainTextComponentSerializer.plainText().serialize(deathMessage)

        ChatOG.scope.launch {
            discordBridgeLock.read {
                ChatOG.discordBridge?.sendEmbed(translatedDeathMessage, event.player.uniqueId, 0xFF0000)
            }
        }
    }

    @EventHandler
    fun onVanish(event: VanishStatusChangeEvent) {
        if (event.value) {
            val playerQuitEvent =
                PlayerQuitEvent(
                    event.affected.base,
                    Component.translatable(
                        "multiplayer.player.left",
                        NamedTextColor.YELLOW,
                        event.affected.base.displayName(),
                    ),
                    PlayerQuitEvent.QuitReason.DISCONNECTED,
                )
            JoinQuitListener().onQuit(playerQuitEvent)
            onQuit(playerQuitEvent)
        } else {
            val playerJoinEvent = PlayerJoinEvent(event.affected.base, Component.text(""))
            JoinQuitListener().onJoin(playerJoinEvent)
            onJoin(playerJoinEvent)
        }
    }
}
