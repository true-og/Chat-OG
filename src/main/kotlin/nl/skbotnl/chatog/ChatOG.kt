package nl.skbotnl.chatog

import com.earth2me.essentials.Essentials
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import net.milkbowl.vault.chat.Chat
import nl.skbotnl.chatog.commands.*
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class ChatOG : JavaPlugin() {
    @OptIn(DelicateCoroutinesApi::class)
    companion object {
        lateinit var plugin: JavaPlugin
        lateinit var chat: Chat
        var essentials = Bukkit.getServer().pluginManager.getPlugin("Essentials") as Essentials

        // API
        @JvmStatic
        @Suppress("unused")
        fun sendMessageWithBot(message: String) {
            GlobalScope.launch { DiscordBridge.sendMessageWithBot(message) }
        }

        @JvmStatic
        @Suppress("unused")
        fun sendMessage(message: String, player: String, uuid: UUID?) {
            GlobalScope.launch { DiscordBridge.sendMessage(message, player, uuid) }
        }

        @JvmStatic
        @Suppress("unused")
        fun sendStaffMessage(message: String, player: String, uuid: UUID?) {
            GlobalScope.launch { DiscordBridge.sendStaffMessage(message, player, uuid) }
        }

        @JvmStatic
        @Suppress("unused")
        fun sendPremiumMessage(message: String, player: String, uuid: UUID?) {
            GlobalScope.launch { DiscordBridge.sendPremiumMessage(message, player, uuid) }
        }

        @JvmStatic
        @Suppress("unused")
        fun sendEmbed(message: String, uuid: UUID?, color: Int) {
            GlobalScope.launch { DiscordBridge.sendEmbed(message, uuid, color) }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onEnable() {
        plugin = this

        if (Config.load()) {
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }

        LanguageDatabase.init()
        GlobalScope.launch {
            BlocklistManager.load()
            EmojiConverter.load()
            ArgosTranslate.init()
        }

        val rsp = server.servicesManager.getRegistration(Chat::class.java)
        chat = rsp!!.provider

        this.server.pluginManager.registerEvents(Events(), this)
        this.getCommand("translatemessage")?.setExecutor(TranslateMessage())
        this.getCommand("translatesettings")?.setExecutor(TranslateSettings())
        this.getCommand("translatesettings")?.tabCompleter = TranslateSettingsTabCompleter()
        this.getCommand("chatconfigreload")?.setExecutor(ChatConfigReload())
        this.getCommand("sc")?.setExecutor(StaffChat())
        this.getCommand("p")?.setExecutor(PremiumChat())

        if (Config.discordEnabled) {
            GlobalScope.launch {
                DiscordBridge.main()
            }
        }
    }

    override fun onDisable() {
        if (Config.discordEnabled) {
            if (DiscordBridge.jda != null) {
                DiscordBridge.sendMessageWithBot(Config.serverHasStoppedMessage)
                DiscordBridge.jda!!.shutdownNow()
            }
        }
    }
}
