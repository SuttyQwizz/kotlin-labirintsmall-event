package com.example.myplugin

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

class EventListener : Listener {

    @EventHandler
    fun onChestOpen(event: PlayerInteractEvent) {
        if (event.hand != EquipmentSlot.HAND || event.clickedBlock?.type != Material.CHEST) return

        val chestLocation = event.clickedBlock!!.location

        if (!GameManager.isGameRunning() && GameManager.isChestProtected(chestLocation)) {
            event.isCancelled = true
            event.player.sendMessage("Сундуки в зоне спавна заблокированы!")
            return
        }

        if (GameManager.isGameRunning() && GameManager.isChestsLocked()) {
            event.isCancelled = true
            event.player.sendMessage("Сундуки заблокированы!")
            return
        }

        if (GameManager.isGameRunning()) {
            GameManager.applyBlindness(event.player)
        }
    }

    @EventHandler
    fun onPvp(event: EntityDamageByEntityEvent) {
        if (!GameManager.isPvpEnabled() && event.damager is org.bukkit.entity.Player && event.entity is org.bukkit.entity.Player) {
            event.isCancelled = true
            return
        }
        if (event.damager is org.bukkit.entity.Player && event.entity is org.bukkit.entity.Player) {
            val damager = event.damager as org.bukkit.entity.Player
            val itemInHand = damager.inventory.itemInHand
            if (itemInHand.type in listOf(
                    Material.WOOD_AXE,
                    Material.STONE_AXE,
                    Material.IRON_AXE,
                    Material.GOLD_AXE,
                    Material.DIAMOND_AXE
                )) {
                event.isCancelled = true
                damager.sendMessage("Топоры не наносят урон!")
            }
        }
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        if (GameManager.isBlockProtected(event.block.location)) {
            event.isCancelled = true
            event.player.sendMessage("Нельзя ломать блоки в зоне спавна!")
            return
        }
        if (GameManager.isResourceLootCooldown()) {
            val restrictedResources = setOf(
                Material.IRON_ORE,
                Material.GOLD_ORE
            )
            if (event.block.type in restrictedResources) {
                event.isCancelled = true
                event.player.sendMessage("теост")
            }
        }
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        GameManager.addPlacedBlock(event.block.location)
    }
}