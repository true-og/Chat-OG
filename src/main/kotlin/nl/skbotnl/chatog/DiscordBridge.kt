package nl.skbotnl.chatog

import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.send.WebhookEmbed
import club.minnced.discord.webhook.send.WebhookEmbedBuilder
import club.minnced.discord.webhook.send.WebhookMessageBuilder
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.generics.getChannel
import dev.minn.jda.ktx.jdabuilder.cache
import dev.minn.jda.ktx.jdabuilder.intents
import dev.minn.jda.ktx.jdabuilder.light
import java.util.*
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.trueog.utilitiesog.UtilitiesOG
import nl.skbotnl.chatog.ChatOG.Companion.config
import nl.skbotnl.chatog.ChatUtil.convertMentions
import nl.skbotnl.chatog.ChatUtil.recolorComponent
import nl.skbotnl.chatog.ChatUtil.stripGroupMentions
import nl.skbotnl.chatog.commands.TranslateMessage
import org.bukkit.Bukkit
import org.bukkit.entity.Player

internal class DiscordBridge private constructor() {
    private lateinit var jda: JDA
    private lateinit var webhook: WebhookClient
    private var staffWebhook: WebhookClient? = null
    private var premiumWebhook: WebhookClient? = null
    private var developerWebhook: WebhookClient? = null

    companion object {
        fun create(): DiscordBridge? {
            val discordBridge = DiscordBridge()
            if (config.webhook != null) {
                try {
                    discordBridge.webhook = WebhookClient.withUrl(config.webhook!!)
                } catch (_: IllegalArgumentException) {
                    ChatOG.plugin.logger.warning("Config option \"webhook\" is invalid")
                }
            } else {
                ChatOG.plugin.logger.severe("You have enabled Discord but have not set up the webhook")
                return null
            }
            if (config.staffWebhook != null) {
                try {
                    discordBridge.staffWebhook = WebhookClient.withUrl(config.staffWebhook!!)
                } catch (_: IllegalArgumentException) {
                    ChatOG.plugin.logger.warning("Config option \"staffWebhook\" is invalid")
                }
            } else if (config.staffDiscordEnabled) {
                ChatOG.plugin.logger.warning("You have enabled staff Discord but have not set up the staff webhook")
            }
            if (config.premiumWebhook != null) {
                try {
                    discordBridge.premiumWebhook = WebhookClient.withUrl(config.premiumWebhook!!)
                } catch (_: IllegalArgumentException) {
                    ChatOG.plugin.logger.warning("Config option \"premiumWebhook\" is invalid")
                }
            } else if (config.premiumDiscordEnabled) {
                ChatOG.plugin.logger.warning("You have enabled premium Discord but have not set up the premium webhook")
            }
            if (config.developerWebhook != null) {
                try {
                    discordBridge.developerWebhook = WebhookClient.withUrl(config.developerWebhook!!)
                } catch (_: IllegalArgumentException) {
                    ChatOG.plugin.logger.warning("Config option \"developer\" is invalid")
                }
            } else if (config.premiumDiscordEnabled) {
                ChatOG.plugin.logger.warning(
                    "You have enabled developer Discord but have not set up the developer webhook"
                )
            }

            if (config.botToken == null) {
                ChatOG.plugin.logger.severe("You have enabled Discord but have not set up the bot token")
                return null
            }
            discordBridge.jda =
                light(config.botToken!!, enableCoroutines = true) {
                    intents += listOf(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
                    cache += listOf(CacheFlag.EMOJI)
                    setMemberCachePolicy(MemberCachePolicy.ALL)
                }

            val status =
                if (config.status == null) {
                    ChatOG.plugin.logger.warning(
                        "You have enabled Discord but have not set up the status, using the default one instead"
                    )
                    "Minecraft"
                } else {
                    config.status!!
                }

            discordBridge.jda.presence.setPresence(Activity.playing(status), false)

            val serverHasStartedMessage =
                if (config.serverHasStartedMessage == null) {
                    ChatOG.plugin.logger.warning(
                        "You have enabled Discord but have not set up the server has started message, using the default one instead"
                    )
                    "The server has started"
                } else {
                    config.serverHasStartedMessage!!
                }

            if (config.guildId == null) {
                ChatOG.plugin.logger.severe("You have enabled Discord but have not set up the guild ID")
                return null
            }

            discordBridge.jda.listener<ReadyEvent> {
                discordBridge.sendMessageWithBot(serverHasStartedMessage)
                val guild = discordBridge.jda.getGuildById(config.guildId!!)
                if (guild == null) {
                    ChatOG.plugin.logger.severe("Guild was null")
                    return@listener
                }
                if (config.listCommandName == null) {
                    ChatOG.plugin.logger.warning(
                        "You have enabled Discord but have not set up the list command name, the command will not be created"
                    )
                    return@listener
                }
                if (config.listCommandText == null) {
                    ChatOG.plugin.logger.warning(
                        "You have enabled Discord but have not set up the list command text, the command will not be created"
                    )
                }

                guild.upsertCommand(config.listCommandName!!, "List all online players.").queue()
            }

            discordBridge.jda.listener<SlashCommandInteractionEvent> {
                if (it.name == config.listCommandName) {
                    it.deferReply().queue()
                    try {
                        if (Bukkit.getOnlinePlayers().isEmpty()) {
                            it.hook.sendMessage("There are no players online.").queue()
                            return@listener
                        }
                        it.hook
                            .sendMessage(
                                "${
                                    config.listCommandText!!
                                        .replace("%onlineplayers%", Bukkit.getOnlinePlayers().count().toString())
                                        .replace("%maxplayers%", Bukkit.getMaxPlayers().toString())
                                }\n${
                                    Bukkit.getOnlinePlayers().joinToString(separator = ", ") { player -> player.name }
                                }"
                            )
                            .queue()
                    } catch (_: Exception) {
                        ChatOG.plugin.logger.warning("Could not respond to /list interaction")
                    }
                }
            }

            discordBridge.jda.listener<MessageReceivedEvent> {
                if (
                    it.channel.id != config.channelId &&
                        (if (config.staffDiscordEnabled) it.channel.id != config.staffChannelId else true) &&
                        (if (config.premiumDiscordEnabled) it.channel.id != config.premiumChannelId else true) &&
                        (if (config.developerDiscordEnabled) it.channel.id != config.developerChannelId else true)
                ) {
                    return@listener
                }

                if (it.author.isBot) {
                    return@listener
                }

                val message = it.message

                val user = it.author
                val member = it.member ?: return@listener
                val color = member.color
                val userColor: TextColor =
                    if (color == null) {
                        TextColor.color(153, 170, 181)
                    } else {
                        TextColor.color(color.rgb)
                    }

                val roles = member.roles
                val topRole = roles.maxByOrNull { role -> role.positionRaw }
                val sortedRoles = member.roles.sortedByDescending { role -> role.positionRaw }

                val discordComponent = Component.text("Discord: ").color(TextColor.color(88, 101, 242))
                val userComponent: TextComponent =
                    if (color == null) {
                        Component.text("@${user.name}").color(NamedTextColor.GRAY)
                    } else {
                        Component.text("[#${topRole!!.name}] @${user.name}").color(userColor)
                    }

                val messageColor =
                    if (config.roles.isEmpty()) {
                        NamedTextColor.GRAY
                    } else {
                        val roleId = sortedRoles.first { role -> role.id in config.roles }?.id
                        if (roleId != null) {
                            val roleColor = config.roleMessageColor[roleId]

                            if (roleColor is Config.RGBColor) {
                                TextColor.color(roleColor.r, roleColor.g, roleColor.b)
                            } else {
                                roleColor as NamedTextColor
                            }
                        } else NamedTextColor.GRAY
                    }

                val suffix =
                    if (config.rolesWithSuffixes.isEmpty()) {
                        ">"
                    } else {
                        val roleId = sortedRoles.first { role -> role.id in config.rolesWithSuffixes }?.id
                        if (roleId != null) {
                            config.roleSuffix[roleId]!!
                        } else {
                            ">"
                        }
                    }

                val messageComponents = mutableListOf<Component>(UtilitiesOG.trueogColorize(suffix))

                val attachmentComponents = mutableListOf<Component>()

                if (message.stickers.isNotEmpty()) {
                    attachmentComponents += Component.text("[Sticker: ${message.stickers[0].name}]").color(messageColor)
                }

                if (message.attachments.isNotEmpty()) {
                    message.attachments.forEach { attachment ->
                        var linkComponent = Component.text(attachment.url).color(TextColor.color(34, 100, 255))

                        linkComponent =
                            linkComponent.hoverEvent(
                                HoverEvent.hoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    UtilitiesOG.trueogColorize("<green>Click to open link"),
                                )
                            )

                        linkComponent =
                            linkComponent.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, attachment.url))

                        attachmentComponents += linkComponent
                    }
                }

                val useColor =
                    if (config.useColorCodeRoles) {
                        roles.any { role -> role.id in config.colorCodeRoles }
                    } else {
                        true
                    }

                if (message.contentDisplay != "") {
                    messageComponents +=
                        recolorComponent(
                            ChatUtil.processText(
                                EmojiConverter.replaceEmojisWithNames(message.contentDisplay),
                                "@${user.name}",
                                useColor,
                            ) ?: return@listener,
                            messageColor,
                        )
                }

                val contentComponent =
                    Component.join(
                        JoinConfiguration.separator(Component.text(" ")),
                        Component.join(JoinConfiguration.noSeparators(), messageComponents),
                        Component.join(JoinConfiguration.noSeparators(), attachmentComponents),
                    )

                var replyComponent = Component.text("")

                if (message.messageReference != null) {
                    val replyMessage = message.messageReference!!.message
                    if (replyMessage != null) {
                        replyComponent =
                            Component.text(
                                    "[Reply to: ${if (!replyMessage.isWebhookMessage) "@" else ""}${replyMessage.author.name}] "
                                )
                                .color(NamedTextColor.GREEN)
                    }
                }

                var messageComponent =
                    when (it.channel.id) {
                        config.staffChannelId -> {
                            val staffComponent = Component.text("STAFF | ").color(NamedTextColor.RED)
                            Component.join(
                                JoinConfiguration.noSeparators(),
                                discordComponent,
                                staffComponent,
                                replyComponent,
                                userComponent,
                                contentComponent,
                            )
                        }

                        config.premiumChannelId -> {
                            val premiumComponent = Component.text("PREMIUM | ").color(NamedTextColor.GREEN)
                            Component.join(
                                JoinConfiguration.noSeparators(),
                                discordComponent,
                                premiumComponent,
                                replyComponent,
                                userComponent,
                                contentComponent,
                            )
                        }

                        config.developerChannelId -> {
                            val developerComponent = Component.text("DEVELOPER | ").color(NamedTextColor.AQUA)
                            Component.join(
                                JoinConfiguration.noSeparators(),
                                discordComponent,
                                developerComponent,
                                replyComponent,
                                userComponent,
                                contentComponent,
                            )
                        }

                        else -> {
                            Component.join(
                                JoinConfiguration.noSeparators(),
                                discordComponent,
                                replyComponent,
                                userComponent,
                                contentComponent,
                            )
                        }
                    }

                messageComponent =
                    messageComponent.hoverEvent(
                        HoverEvent.hoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            UtilitiesOG.trueogColorize("<green>Click to translate this message"),
                        )
                    )

                val randomUUID = UUID.randomUUID()
                messageComponent =
                    messageComponent.clickEvent(
                        ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/translatemessage $randomUUID 2")
                    )

                when (it.channel.id) {
                    config.staffChannelId -> {
                        for (p in Bukkit.getOnlinePlayers()) {
                            if (p.hasPermission("chat-og.staff")) {
                                p.sendMessage(messageComponent)
                            }
                        }
                    }

                    config.premiumChannelId -> {
                        for (p in Bukkit.getOnlinePlayers()) {
                            if (p.hasPermission("chat-og.premium")) {
                                p.sendMessage(messageComponent)
                            }
                        }
                    }

                    else -> {
                        for (p in Bukkit.getOnlinePlayers()) {
                            p.sendMessage(messageComponent)
                        }
                    }
                }

                TranslateMessage.customMessages[randomUUID] =
                    TranslateMessage.SentCustomMessage(
                        message.contentDisplay,
                        member.effectiveName,
                        Component.join(JoinConfiguration.noSeparators(), discordComponent, userComponent),
                        Component.text("$suffix "),
                    )
            }
            return discordBridge
        }
    }

    fun shutdownNow() {
        jda.shutdownNow()
    }

    fun getGuildById(id: String): Guild? {
        return jda.getGuildById(id)
    }

    fun sendMessageWithBot(message: String) {
        if (config.channelId == null) return

        val channel = jda.getChannel<MessageChannel>(config.channelId!!)
        if (channel == null) {
            ChatOG.plugin.logger.warning("channelId has not been set or is invalid")
            return
        }

        channel.sendMessage(UtilitiesOG.stripFormatting(message)).complete()
    }

    suspend fun sendMessage(message: String, player: Player) {
        val webhookMessage =
            WebhookMessageBuilder()
                .setUsername(UtilitiesOG.stripFormatting(ChatUtil.getPlayerPartString(player)))
                .setContent(UtilitiesOG.stripFormatting(stripGroupMentions(convertMentions(message))))
                .setAvatarUrl("https://minotar.net/helm/${player.uniqueId}.png")

        webhook.send(webhookMessage.build())
    }

    suspend fun sendMessage(message: String, name: String, uuid: UUID?) {
        val webhookMessage =
            WebhookMessageBuilder().apply {
                setUsername(UtilitiesOG.stripFormatting(name))
                setContent(UtilitiesOG.stripFormatting(stripGroupMentions((convertMentions(message)))))
                if (uuid != null) setAvatarUrl("https://minotar.net/helm/$uuid.png")
            }

        webhook.send(webhookMessage.build())
    }

    suspend fun sendStaffMessage(message: String, name: String, uuid: UUID?) {
        if (!config.staffDiscordEnabled) return

        if (staffWebhook == null) {
            ChatOG.plugin.logger.warning("staffWebhook has not been set or is invalid")
            return
        }

        val webhookMessage =
            WebhookMessageBuilder().apply {
                setUsername(UtilitiesOG.stripFormatting(name))
                setContent(UtilitiesOG.stripFormatting(stripGroupMentions((convertMentions(message)))))
                if (uuid != null) setAvatarUrl("https://minotar.net/helm/$uuid.png")
            }

        staffWebhook!!.send(webhookMessage.build())
    }

    suspend fun sendPremiumMessage(message: String, name: String, uuid: UUID?) {
        if (!config.premiumDiscordEnabled) return

        if (premiumWebhook == null) {
            ChatOG.plugin.logger.warning("premiumWebhook has not been set or is invalid")
            return
        }

        val webhookMessage =
            WebhookMessageBuilder().apply {
                setUsername(UtilitiesOG.stripFormatting(name))
                setContent(UtilitiesOG.stripFormatting(stripGroupMentions((convertMentions(message)))))
                if (uuid != null) setAvatarUrl("https://minotar.net/helm/$uuid.png")
            }

        premiumWebhook!!.send(webhookMessage.build())
    }

    suspend fun sendDeveloperMessage(message: String, name: String, uuid: UUID?) {
        if (!config.developerDiscordEnabled) return

        if (developerWebhook == null) {
            ChatOG.plugin.logger.warning("developerWebhook has not been set or is invalid")
            return
        }

        val webhookMessage =
            WebhookMessageBuilder().apply {
                setUsername(UtilitiesOG.stripFormatting(name))
                setContent(UtilitiesOG.stripFormatting(stripGroupMentions((convertMentions(message)))))
                if (uuid != null) setAvatarUrl("https://minotar.net/helm/$uuid.png")
            }

        developerWebhook!!.send(webhookMessage.build())
    }

    fun sendEmbed(message: String, uuid: UUID?, color: Int?) {
        var iconUrl: String? = null
        if (uuid != null) {
            iconUrl = "https://minotar.net/helm/$uuid.png"
        }

        val webhookMessage =
            WebhookEmbedBuilder().apply {
                if (color != null)
                    setColor(color)
                        .setAuthor(WebhookEmbed.EmbedAuthor(UtilitiesOG.stripFormatting(message), iconUrl, null))
            }

        webhook.send(webhookMessage.build())
    }
}
