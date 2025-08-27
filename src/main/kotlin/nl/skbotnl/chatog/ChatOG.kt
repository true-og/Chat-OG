package nl.skbotnl.chatog

import com.earth2me.essentials.Essentials
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlinx.coroutines.*
import net.luckperms.api.LuckPerms
import nl.skbotnl.chatog.commands.*
import org.bukkit.Bukkit
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin

internal class ChatOG : JavaPlugin() {
    @OptIn(DelicateCoroutinesApi::class)
    companion object {
        val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

        lateinit var plugin: JavaPlugin
        lateinit var config: Config
        var blocklistManager: BlocklistManager? = null
        lateinit var luckPerms: LuckPerms
        lateinit var languageDatabase: LanguageDatabase
        var translator: OpenAI? = null
        var discordBridge: DiscordBridge? = null
        val discordBridgeLock = ReentrantReadWriteLock()
        var essentials = Bukkit.getServer().pluginManager.getPlugin("Essentials") as Essentials
        var lastMessagedMap: MutableMap<UUID, UUID> = HashMap()

        fun isLanguageDatabaseInitialized() = ::languageDatabase.isInitialized
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onEnable() {
        plugin = this

        Companion.config =
            Config.create()
                ?: run {
                    Bukkit.getPluginManager().disablePlugin(this)
                    return
                }

        if (Companion.config.blocklistEnabled) {
            blocklistManager = BlocklistManager()
        }

        translator =
            if (Companion.config.openAIEnabled) {
                if (Companion.config.openAIBaseUrl == null) {
                    this.logger.warning(
                        "You have enabled OpenAI translation but have not set up the base url, not enabling the translator"
                    )
                    null
                } else if (Companion.config.openAIApiKey == null) {
                    this.logger.warning(
                        "You have enabled OpenAI translation but have not set up the api key, not enabling the translator"
                    )
                    null
                } else {
                    OpenAI()
                }
            } else null

        languageDatabase = LanguageDatabase()
        if (languageDatabase.testConnection()) {
            logger.severe("Could not connect to Redis")
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }

        scope.launch { EmojiConverter.load() }

        val luckPermsProvider = Bukkit.getServicesManager().getRegistration(LuckPerms::class.java)
        if (luckPermsProvider == null) {
            this.logger.severe("LuckPerms API is null, quitting...")
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }
        luckPerms = luckPermsProvider.provider

        this.server.pluginManager.registerEvents(Events(), this)
        this.getCommand("translatemessage")?.setExecutor(TranslateMessage())
        this.getCommand("translatesettings")?.setExecutor(TranslateSettings())
        this.getCommand("translatesettings")?.tabCompleter = TranslateSettingsTabCompleter()
        this.getCommand("chatconfigreload")?.setExecutor(ChatConfigReload())
        this.getCommand("gc")?.setExecutor(GeneralChat())
        this.getCommand("g")?.setExecutor(GeneralChat())
        this.getCommand("sc")?.setExecutor(StaffChat())
        this.getCommand("s")?.setExecutor(StaffChat())
        this.getCommand("pc")?.setExecutor(PremiumChat())
        this.getCommand("p")?.setExecutor(PremiumChat())
        this.getCommand("dc")?.setExecutor(DeveloperChat())
        this.getCommand("d")?.setExecutor(DeveloperChat())
        this.getCommand("msg")?.setExecutor(PrivateMessage())
        this.getCommand("whisper")?.setExecutor(PrivateMessage())
        this.getCommand("pm")?.setExecutor(PrivateMessage())
        this.getCommand("reply")?.setExecutor(Reply())
        this.getCommand("r")?.setExecutor(Reply())

        if (Companion.config.discordEnabled) {
            scope.launch { discordBridgeLock.write { discordBridge = DiscordBridge.create() } }
        }

        val chatAPI = ChatAPI()
        this.server.servicesManager.register(ChatAPI::class.java, chatAPI, this, ServicePriority.Normal)
    }

    override fun onDisable() {
        if (discordBridge != null) {
            discordBridgeLock.read {
                val discordBridge = discordBridge
                val serverHasStoppedMessage =
                    if (Companion.config.serverHasStoppedMessage == null) {
                        this.logger.warning(
                            "You have enabled Discord but have not set up the server has stopped message, using the default one instead"
                        )
                        "The server has stopped"
                    } else {
                        Companion.config.serverHasStoppedMessage!!
                    }
                discordBridge!!.sendMessageWithBot(serverHasStoppedMessage)
                discordBridge.shutdownNow()
            }
        }

        if (isLanguageDatabaseInitialized()) {
            languageDatabase.shutdown()
        }

        scope.cancel()

        runBlocking { scope.coroutineContext[Job]?.join() }
    }
}
