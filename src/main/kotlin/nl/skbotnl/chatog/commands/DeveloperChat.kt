package nl.skbotnl.chatog.commands

import kotlin.collections.set
import net.trueog.utilitiesog.UtilitiesOG
import nl.skbotnl.chatog.ChatOG.Companion.config
import nl.skbotnl.chatog.ChatSystem
import nl.skbotnl.chatog.ChatSystem.ChatType
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class DeveloperChat : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) {
            sender.sendMessage("You can only execute this command as a player.")
            return true
        }

        if (!sender.hasPermission("chat-og.developer")) {
            sender.sendMessage(
                UtilitiesOG.trueogColorize(
                    "${config.prefix}<reset>: <red>You do not have permission to run this command."
                )
            )
            return true
        }

        if (args == null || args.isEmpty()) {
            if (ChatSystem.inChat[sender.uniqueId] == ChatType.DEVELOPER_CHAT) {
                ChatSystem.inChat[sender.uniqueId] = ChatType.GENERAL_CHAT

                sender.sendMessage(
                    UtilitiesOG.trueogColorize("${config.prefix}<reset>: You are now talking in the general chat.")
                )
                return true
            }
            ChatSystem.inChat[sender.uniqueId] = ChatType.DEVELOPER_CHAT
            sender.sendMessage(
                UtilitiesOG.trueogColorize("${config.prefix}<reset>: You are now talking in the developer chat.")
            )
            return true
        }

        ChatSystem.sendMessageInDeveloperChat(sender, args.joinToString(separator = " "))

        return true
    }
}
