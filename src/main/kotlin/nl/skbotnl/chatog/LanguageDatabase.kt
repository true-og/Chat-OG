package nl.skbotnl.chatog

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisConnectionException
import java.util.*
import nl.skbotnl.chatog.ChatOG.Companion.config

internal class LanguageDatabase {
    private val redisClient: RedisClient = RedisClient.create(config.redisUrl)

    /** @return True if failed */
    fun testConnection(): Boolean {
        try {
            val connection = redisClient.connect()
            connection.close()
            return false
        } catch (_: RedisConnectionException) {
            return true
        }
    }

    fun getPlayerLanguage(uuid: UUID): String? {
        val connection = redisClient.connect()
        val commands = connection.sync()
        val value = commands.get("chatog:language:$uuid")
        connection.close()
        return value
    }

    fun setPlayerLanguage(uuid: UUID, language: String) {
        val connection = redisClient.connect()
        val commands = connection.sync()
        commands.set("chatog:language:$uuid", language)
        connection.close()
    }

    fun shutdown() {
        redisClient.shutdown()
    }
}
