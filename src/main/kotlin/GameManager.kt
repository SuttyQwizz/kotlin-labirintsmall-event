package com.example.myplugin

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable

object GameManager {
    private val spawnLocation = Location(Bukkit.getWorlds()[0], -180.0, 76.0, 256.0)
    private val radius = 50
    private val spawnProtectRadius = 10.0
    private var chestsLocked = true
    private var pvpEnabled = false
    private var resourceLootCooldown = true
    private var gameRunning = false
    private val placedBlocks = mutableSetOf<Location>()
    private lateinit var bossBar: BossBar
    private lateinit var waitingTask: BukkitRunnable
    private lateinit var resourceTask: BukkitRunnable
    private lateinit var checkTask: BukkitRunnable

    fun startGame(starter: String) {
        gameRunning = true
        placedBlocks.clear()
        for (player in Bukkit.getOnlinePlayers()) {
            player.teleport(spawnLocation)
            player.gameMode = GameMode.SURVIVAL
            player.inventory.clear()
        }

        Bukkit.broadcastMessage("${ChatColor.BLUE}Jr.Dev ${ChatColor.WHITE}| ${ChatColor.WHITE}$starter сейчас запустил ивент: Лабиринт Тип игры: Тест")

        fillChests()

        bossBar = Bukkit.createBossBar("Ожидание: 15 сек", BarColor.BLUE, BarStyle.SEGMENTED_10)
        for (player in Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(player)
        }

        waitingTask = object : BukkitRunnable() {
            var time = 15
            override fun run() {
                if (!gameRunning) {
                    cancel()
                    return
                }
                if (time > 0) {
                    bossBar.progress = time / 15.0
                    bossBar.title = "Ожидание: $time сек (Сундуки сейчас нельзя открыть)"
                    time--
                } else {
                    chestsLocked = false
                    bossBar.title = "Сундуки открыты! ЛУТАЙ РЕСЫ"
                    startResourceCooldown()
                    cancel()
                }
            }
        }
        waitingTask.runTaskTimer(Main.instance, 0L, 20L)
    }

    fun stopGame() {
        gameRunning = false
        if (::waitingTask.isInitialized && !waitingTask.isCancelled) {
            waitingTask.cancel()
        }
        if (::resourceTask.isInitialized && !resourceTask.isCancelled) {
            resourceTask.cancel()
        }
        if (::checkTask.isInitialized && !checkTask.isCancelled) {
            checkTask.cancel()
        }
        if (::bossBar.isInitialized) {
            bossBar.removeAll()
        }
        for (player in Bukkit.getOnlinePlayers()) {
            player.removePotionEffect(PotionEffectType.BLINDNESS)
            player.gameMode = GameMode.SURVIVAL
        }
        chestsLocked = true
        pvpEnabled = false
        resourceLootCooldown = true
        placedBlocks.clear()
    }

    private fun startResourceCooldown() {
        resourceTask = object : BukkitRunnable() {
            var time = 30
            override fun run() {
                if (!gameRunning) {
                    cancel()
                    return
                }
                if (time > 0) {
                    bossBar.progress = time / 30.0
                    bossBar.title = "ЛУТАЙ РЕСЫ: $time сек (Пвп выкл)"
                    time--
                } else {
                    resourceLootCooldown = false
                    pvpEnabled = true
                    for (player in Bukkit.getOnlinePlayers()) {
                        player.removePotionEffect(PotionEffectType.BLINDNESS)
                    }
                    bossBar.title = "Ивент в процессе! Пвп включено."
                    bossBar.progress = 1.0
                    checkLastSurvivor()
                    cancel()
                }
            }
        }
        resourceTask.runTaskTimer(Main.instance, 0L, 20L)
    }

    private fun checkLastSurvivor() {
        checkTask = object : BukkitRunnable() {
            override fun run() {
                if (!gameRunning) {
                    cancel()
                    return
                }
                val alivePlayers = Bukkit.getOnlinePlayers().filter {
                    it.gameMode == GameMode.SURVIVAL &&
                            it.health > 0.0 &&
                            it.world == spawnLocation.world
                }
                if (alivePlayers.size == 1) {
                    val winner = alivePlayers[0]
                    endGame(winner)
                    cancel()
                } else if (alivePlayers.isEmpty()) {
                    stopGame()
                    Bukkit.broadcastMessage("${ChatColor.RED}Ивент завершен: нет выживших!")
                    cancel()
                }
            }
        }
        checkTask.runTaskTimer(Main.instance, 0L, 20L)
    }

    private fun endGame(winner: Player) {
        gameRunning = false
        if (::bossBar.isInitialized) {
            bossBar.removeAll()
        }
        Bukkit.broadcastMessage("${ChatColor.GOLD}Победитель: ${winner.name}")
        for (player in Bukkit.getOnlinePlayers()) {
            player.sendTitle("${ChatColor.GOLD}Победитель: ${winner.name}", "", 10, 70, 20)
            player.removePotionEffect(PotionEffectType.BLINDNESS)
            player.gameMode = GameMode.SURVIVAL
        }
        chestsLocked = true
        pvpEnabled = false
        resourceLootCooldown = true
        placedBlocks.clear()
    }

    private fun fillChests() {
        val world = spawnLocation.world
        val minX = spawnLocation.blockX - radius
        val minY = spawnLocation.blockY - radius
        val minZ = spawnLocation.blockZ - radius
        val maxX = spawnLocation.blockX + radius
        val maxY = spawnLocation.blockY + radius
        val maxZ = spawnLocation.blockZ + radius

        for (x in minX..maxX) {
            for (y in minY..maxY) {
                for (z in minZ..maxZ) {
                    val block = world.getBlockAt(x, y, z)
                    if (block.type == Material.CHEST) {
                        val chest = block.state as org.bukkit.block.Chest
                        val inv = chest.inventory
                        inv.clear()
                        val items = listOf(
                            ItemStack(Material.IRON_INGOT, (1..5).random()),
                            ItemStack(Material.IRON_NUGGET, (1..10).random()),
                            ItemStack(Material.GOLD_INGOT, (1..3).random()),
                            ItemStack(Material.APPLE, (1..2).random()),
                            ItemStack(Material.BREAD, (1..4).random()),
                            ItemStack(Material.WHEAT, (1..5).random()),
                            ItemStack(Material.LOG, (1..3).random()),
                            ItemStack(Material.STICK, (1..8).random())
                        )
                        inv.addItem(*items.shuffled().take((1..5).random()).toTypedArray())
                    }
                }
            }
        }
    }

    fun isChestsLocked(): Boolean = chestsLocked

    fun isPvpEnabled(): Boolean = pvpEnabled

    fun isResourceLootCooldown(): Boolean = resourceLootCooldown

    fun isGameRunning(): Boolean = gameRunning

    fun applyBlindness(player: Player) {
        if (!pvpEnabled && !chestsLocked) {
            player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, Int.MAX_VALUE, 2))
        }
    }

    fun isBlockProtected(location: Location): Boolean {
        return location.world == spawnLocation.world &&
                location.distance(spawnLocation) <= spawnProtectRadius &&
                !placedBlocks.contains(location)
    }

    fun isChestProtected(location: Location): Boolean {
        return location.world == spawnLocation.world &&
                location.distance(spawnLocation) <= spawnProtectRadius
    }

    fun addPlacedBlock(location: Location) {
        placedBlocks.add(location)
    }
}