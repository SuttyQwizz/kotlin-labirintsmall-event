package com.example.myplugin

import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {
    companion object {
        lateinit var instance: Main
    }

    override fun onEnable() {
        instance = this
        getCommand("game")?.setExecutor(GameCommand())
        server.pluginManager.registerEvents(EventListener(), this)
        logger.info("Включился!")
    }

    override fun onDisable() {
        logger.info("выключился!")
    }
}