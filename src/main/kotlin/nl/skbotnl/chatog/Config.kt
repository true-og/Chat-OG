package nl.skbotnl.chatog

import java.io.File
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.configuration.file.YamlConfiguration

internal class Config private constructor() {
    lateinit var prefix: String
    var blocklistEnabled = false
    var blocklists = listOf<String>()
    lateinit var redisUrl: String
    var openAIEnabled = false
    var openAIBaseUrl: String? = null
    var openAIApiKey: String? = null
    var openAIModel: String? = null
    var discordEnabled = false
    var staffDiscordEnabled = false
    var premiumDiscordEnabled = false
    var developerDiscordEnabled = false
    var status: String? = null
    var serverHasStartedMessage: String? = null
    var serverHasStoppedMessage: String? = null
    var botToken: String? = null
    var channelId: String? = null
    var staffChannelId: String? = null
    var premiumChannelId: String? = null
    var developerChannelId: String? = null
    var guildId: String? = null
    var webhook: String? = null
    var staffWebhook: String? = null
    var premiumWebhook: String? = null
    var developerWebhook: String? = null
    var listCommandName: String? = null
    var listCommandText: String? = null
    var useColorCodeRoles = false
    var colorCodeRoles = listOf<String>()
    var roles = setOf<String>()
    var roleMessageColor: MutableMap<String, Any> = mutableMapOf()
    var rolesWithSuffixes = setOf<String>()
    var roleSuffix: MutableMap<String, String> = mutableMapOf()

    data class RGBColor(val r: Int, val g: Int, val b: Int)

    companion object {
        fun create(): Config? {
            val config = Config()

            val file = File(ChatOG.plugin.dataFolder, "config.yml")
            if (!file.exists()) {
                ChatOG.plugin.saveDefaultConfig()
            }
            val yamlConfig = YamlConfiguration.loadConfiguration(file)

            try {
                config.prefix = yamlConfig.get("prefix") as String
            } catch (_: Exception) {
                ChatOG.plugin.logger.severe("Failed to parse config option \"prefix\" as a string")
                return null
            }

            try {
                config.blocklistEnabled = yamlConfig.get("blocklistEnabled") as Boolean
            } catch (_: Exception) {
                ChatOG.plugin.logger.severe("Failed to parse config option \"blocklistEnabled\" as a boolean")
                return null
            }

            if (config.blocklistEnabled) {
                config.blocklists = yamlConfig.getStringList("blocklists")
            }

            try {
                config.redisUrl = yamlConfig.get("redisUrl") as String
            } catch (_: Exception) {
                ChatOG.plugin.logger.severe("Failed to parse config option \"redisUrl\" as a string")
                return null
            }

            try {
                config.openAIEnabled = yamlConfig.get("openAIEnabled") as Boolean
            } catch (_: Exception) {
                ChatOG.plugin.logger.severe("Failed to parse config option \"openAIEnabled\" as a boolean")
                return null
            }

            if (config.openAIEnabled) {
                try {
                    config.openAIBaseUrl = yamlConfig.get("baseUrl") as String
                } catch (_: Exception) {
                    ChatOG.plugin.logger.severe("Failed to parse config option \"baseUrl\" as a string")
                    return null
                }

                try {
                    config.openAIApiKey = yamlConfig.get("apiKey") as String
                } catch (_: Exception) {
                    ChatOG.plugin.logger.severe("Failed to parse config option \"apiKey\" as a string")
                    return null
                }

                config.openAIModel =
                    try {
                        yamlConfig.get("model") as String
                    } catch (_: Exception) {
                        ""
                    }
            }

            try {
                config.discordEnabled = yamlConfig.get("discordEnabled") as Boolean
            } catch (_: Exception) {
                ChatOG.plugin.logger.severe("Failed to parse config option \"discordEnabled\" as a boolean")
                return null
            }

            try {
                config.staffDiscordEnabled = yamlConfig.get("staffDiscordEnabled") as Boolean
            } catch (_: Exception) {
                ChatOG.plugin.logger.severe("Failed to parse config option \"staffDiscordEnabled\" as a boolean")
                return null
            }

            try {
                config.premiumDiscordEnabled = yamlConfig.get("premiumDiscordEnabled") as Boolean
            } catch (_: Exception) {
                ChatOG.plugin.logger.severe("Failed to parse config option \"premiumDiscordEnabled\" as a boolean")
                return null
            }

            try {
                config.developerDiscordEnabled = yamlConfig.get("developerDiscordEnabled") as Boolean
            } catch (_: Exception) {
                ChatOG.plugin.logger.severe("Failed to parse config option \"developerDiscordEnabled\" as a boolean")
                return null
            }

            if (config.discordEnabled) {
                try {
                    config.status = yamlConfig.get("status") as String
                } catch (_: Exception) {
                    ChatOG.plugin.logger.severe("Failed to parse config option \"status\" as a string")
                    return null
                }

                try {
                    config.serverHasStartedMessage = yamlConfig.get("serverHasStartedMessage") as String
                } catch (_: Exception) {
                    ChatOG.plugin.logger.severe("Failed to parse config option \"serverHasStartedMessage\" as a string")
                    return null
                }

                try {
                    config.serverHasStoppedMessage = yamlConfig.get("serverHasStoppedMessage") as String
                } catch (_: Exception) {
                    ChatOG.plugin.logger.severe("Failed to parse config option \"serverHasStoppedMessage\" as a string")
                    return null
                }

                try {
                    config.botToken = yamlConfig.get("botToken") as String
                } catch (_: Exception) {
                    ChatOG.plugin.logger.severe("Failed to parse config option \"botToken\" as a string")
                    return null
                }

                try {
                    config.guildId = (yamlConfig.get("guildId") as Long).toString()
                } catch (_: Exception) {
                    ChatOG.plugin.logger.severe("Failed to parse config option \"guildId\" as a long")
                    return null
                }

                try {
                    config.channelId = (yamlConfig.get("channelId") as Long).toString()
                } catch (_: Exception) {
                    ChatOG.plugin.logger.severe("Failed to parse config option \"channelId\" as a long")
                    return null
                }

                if (config.staffDiscordEnabled) {
                    try {
                        config.staffChannelId = (yamlConfig.get("staffChannelId") as Long).toString()
                    } catch (_: Exception) {
                        ChatOG.plugin.logger.severe("Failed to parse config option \"staffChannelId\" as a long")
                        return null
                    }

                    try {
                        config.staffWebhook = yamlConfig.get("staffWebhook") as String
                    } catch (_: Exception) {
                        ChatOG.plugin.logger.severe("Failed to parse config option \"staffWebhook\" as a string")
                        return null
                    }
                }

                if (config.premiumDiscordEnabled) {
                    try {
                        config.premiumChannelId = (yamlConfig.get("premiumChannelId") as Long).toString()
                    } catch (_: Exception) {
                        ChatOG.plugin.logger.severe("Failed to parse config option \"premiumChannelId\" as a long")
                        return null
                    }

                    try {
                        config.premiumWebhook = yamlConfig.get("premiumWebhook") as String
                    } catch (_: Exception) {
                        ChatOG.plugin.logger.severe("Failed to parse config option \"premiumWebhook\" as a string")
                        return null
                    }
                }

                if (config.developerDiscordEnabled) {
                    try {
                        config.developerChannelId = (yamlConfig.get("developerChannelId") as Long).toString()
                    } catch (_: Exception) {
                        ChatOG.plugin.logger.severe("Failed to parse config option \"developerChannelId\" as a long")
                        return null
                    }

                    try {
                        config.developerWebhook = yamlConfig.get("developerWebhook") as String
                    } catch (_: Exception) {
                        ChatOG.plugin.logger.severe("Failed to parse config option \"developerWebhook\" as a string")
                        return null
                    }
                }

                try {
                    config.webhook = yamlConfig.get("webhook") as String
                } catch (_: Exception) {
                    ChatOG.plugin.logger.severe("Failed to parse config option \"webhook\" as a string")
                    return null
                }

                try {
                    config.listCommandName = yamlConfig.get("listCommandName") as String
                } catch (_: Exception) {
                    ChatOG.plugin.logger.severe("Failed to parse config option \"listCommandName\" as a string")
                    return null
                }

                try {
                    config.listCommandText = yamlConfig.get("listCommandText") as String
                } catch (_: Exception) {
                    ChatOG.plugin.logger.severe("Failed to parse config option \"listCommandText\" as a string")
                    return null
                }

                try {
                    config.useColorCodeRoles = yamlConfig.get("useColorCodeRoles") as Boolean
                } catch (_: Exception) {
                    ChatOG.plugin.logger.severe("Failed to parse config option \"useColorCodeRoles\" as a boolean")
                    return null
                }

                config.colorCodeRoles = yamlConfig.getStringList("colorCodeRoles")

                config.roles = yamlConfig.getConfigurationSection("roles")?.getKeys(false) ?: setOf()
                config.rolesWithSuffixes = yamlConfig.getConfigurationSection("roleSuffixes")?.getKeys(false) ?: setOf()
            }

            if (config.roles.isNotEmpty()) {
                val messageColors =
                    mapOf<String, NamedTextColor>(
                        Pair("BLACK", NamedTextColor.BLACK),
                        Pair("DARK_BLUE", NamedTextColor.DARK_BLUE),
                        Pair("DARK_GREEN", NamedTextColor.DARK_GREEN),
                        Pair("DARK_AQUA", NamedTextColor.DARK_AQUA),
                        Pair("DARK_RED", NamedTextColor.DARK_RED),
                        Pair("DARK_PURPLE", NamedTextColor.DARK_PURPLE),
                        Pair("GOLD", NamedTextColor.GOLD),
                        Pair("GRAY", NamedTextColor.GRAY),
                        Pair("DARK_GRAY", NamedTextColor.DARK_GRAY),
                        Pair("BLUE", NamedTextColor.BLUE),
                        Pair("GREEN", NamedTextColor.GREEN),
                        Pair("AQUA", NamedTextColor.AQUA),
                        Pair("RED", NamedTextColor.RED),
                        Pair("LIGHT_PURPLE", NamedTextColor.LIGHT_PURPLE),
                        Pair("YELLOW", NamedTextColor.YELLOW),
                        Pair("WHITE", NamedTextColor.WHITE),
                    )
                config.roles.forEach {
                    val messageColorList = yamlConfig.getStringList("roles.$it.message_color")
                    config.roleMessageColor[it] =
                        if (messageColorList.isEmpty()) {
                            try {
                                yamlConfig.get("roles.$it.message_color") as String
                            } catch (_: Exception) {
                                ChatOG.plugin.logger.warning(
                                    "Failed to parse config option \"role.$it.message_color\" as a string"
                                )
                                return@forEach
                            }

                            if (!messageColors.contains(messageColorList[0].uppercase())) {
                                ChatOG.plugin.logger.severe(
                                    "The message color for \"$it\" (\"${messageColorList[0]}\") is invalid."
                                )
                                return@forEach
                            }

                            messageColors[messageColorList[0].uppercase()]!!
                        } else {
                            if (messageColorList.size != 3) {
                                ChatOG.plugin.logger.severe(
                                    "The message color for \"$it\" is not a color or an RGB value."
                                )
                                return@forEach
                            }

                            messageColorList.forEach colorForEach@{ colorInList ->
                                try {
                                    colorInList.toUByte()
                                } catch (_: Exception) {
                                    ChatOG.plugin.logger.severe("The RGB value for \"$it\" is invalid.")
                                    return@forEach
                                }
                            }
                            RGBColor(
                                messageColorList[0].toInt(),
                                messageColorList[1].toInt(),
                                messageColorList[2].toInt(),
                            )
                        }
                }
            } else {
                config.roleMessageColor = mutableMapOf()
            }

            if (config.rolesWithSuffixes.isNotEmpty()) {
                config.rolesWithSuffixes.forEach {
                    val suffixList =
                        try {
                            yamlConfig.get("roleSuffixes.$it.suffix") as String
                        } catch (_: Exception) {
                            ChatOG.plugin.logger.warning(
                                "Failed to parse config option \"roleSuffixes.$it.suffix\" as a string"
                            )
                            return@forEach
                        }
                    config.roleSuffix[it] = suffixList
                }
            } else {
                config.roleSuffix = mutableMapOf()
            }

            return config
        }
    }
}
