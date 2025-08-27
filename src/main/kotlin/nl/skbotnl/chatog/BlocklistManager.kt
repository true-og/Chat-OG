package nl.skbotnl.chatog

import java.net.URI
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BlocklistManager {
    private val lock = Any()
    private val blockList = mutableListOf<String>()

    init {
        ChatOG.plugin.logger.info("Loading the blocklists...")
        refresh()
        ChatOG.plugin.logger.info("Loaded the blocklists!")

        ChatOG.scope.launch {
            while (true) {
                delay(TimeUnit.DAYS.toMillis(1))
                refresh()
            }
        }
    }

    private fun refresh() {
        blockList.clear()
        ChatOG.config.blocklists.forEach { blocklist ->
            URI(blocklist).toURL().openStream().use { input ->
                input.bufferedReader().use { bufferedReader ->
                    synchronized(lock) { bufferedReader.lines().forEach { if (!it.startsWith("#")) blockList.add(it) } }
                }
            }
        }
    }

    private val urlRegex = Regex("(?:https?://)?([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})")

    fun checkUrl(url: String): Boolean {
        val match = urlRegex.find(url) ?: return false
        val baseUrl = match.groups[1]?.value ?: return false
        return synchronized(lock) { blockList.contains(baseUrl) }
    }
}
