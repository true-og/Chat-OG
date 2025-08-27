package nl.skbotnl.chatog.commands

import net.trueog.utilitiesog.UtilitiesOG
import nl.skbotnl.chatog.ChatOG.Companion.config
import nl.skbotnl.chatog.ChatSystem
import nl.skbotnl.chatog.ChatSystem.ChatType
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

internal class StaffChat : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) {
            sender.sendMessage("You can only execute this command as a player.")
            return true
        }

        if (!sender.hasPermission("chat-og.staff")) {
            sender.sendMessage(
                UtilitiesOG.trueogColorize(
                    "${config.prefix}<reset>: <red>You do not have permission to run this command."
                )
            )
            return true
        }

        if (args == null || args.isEmpty()) {
            if (ChatSystem.inChat[sender.uniqueId] == ChatType.STAFF_CHAT) {
                ChatSystem.inChat[sender.uniqueId] = ChatType.GENERAL_CHAT

                sender.sendMessage(
                    UtilitiesOG.trueogColorize("${config.prefix}<reset>: You are now talking in general chat.")
                )
                return true
            }
            ChatSystem.inChat[sender.uniqueId] = ChatType.STAFF_CHAT
            sender.sendMessage(
                UtilitiesOG.trueogColorize("${config.prefix}<reset>: You are now talking in staff chat.")
            )
            return true
        }

        ChatSystem.sendMessageInStaffChat(sender, args.joinToString(separator = " "))

        return true
    }
}
