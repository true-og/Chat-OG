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

class GeneralChat : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) {
            sender.sendMessage("You can only execute this command as a player.")
            return true
        }

        if (args == null || args.isEmpty()) {
            if (ChatSystem.inChat[sender.uniqueId] != ChatType.GENERAL_CHAT) {
                ChatSystem.inChat[sender.uniqueId] = ChatType.GENERAL_CHAT

                sender.sendMessage(
                    UtilitiesOG.trueogColorize("${config.prefix}<reset>: You are now talking in the general chat.")
                )
                return true
            }
            sender.sendMessage(
                UtilitiesOG.trueogColorize("${config.prefix}<reset>: You are already talking in the general chat.")
            )
            return true
        }

        val originalChat = ChatSystem.inChat[sender.uniqueId]
        ChatSystem.inChat[sender.uniqueId] = ChatType.GENERAL_CHAT
        sender.chat(args.joinToString(separator = " "))
        if (originalChat != null) ChatSystem.inChat[sender.uniqueId] = originalChat
        return true
    }
}
