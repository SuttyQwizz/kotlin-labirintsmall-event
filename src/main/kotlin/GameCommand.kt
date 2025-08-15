package com.example.myplugin

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class GameCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage("используй: /game <start|stop>")
            return true
        }

        when (args[0]) {
            "start" -> {
                if (GameManager.isGameRunning()) {
                    sender.sendMessage("Сначала заверши 1 ивент /game stop")
                    return true
                }
                GameManager.startGame(sender.name)
                sender.sendMessage("Ивент начался!")
            }
            "stop" -> {
                GameManager.stopGame()
                sender.sendMessage("Ивент остановлен!")
            }
            else -> sender.sendMessage("используй: /game <start|stop>")
        }
        return true
    }
}