package net.onelitefeather.alioth.listener

import io.papermc.paper.event.entity.EntityMoveEvent
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent
import net.onelitefeather.alioth.Alioth
import net.onelitefeather.alioth.villager.VillagerManager
import org.bukkit.entity.Player
import org.bukkit.entity.Villager
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.VillagerCareerChangeEvent
import org.bukkit.event.inventory.TradeSelectEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.meta.BookMeta

class AliothVillagerListener(
    private val plugin: Alioth,
    private val manager: VillagerManager
) : Listener {

    @EventHandler
    fun onVillagerSpawn(event: PlayerDropItemEvent) {
        val itemStack = event.itemDrop.itemStack
        val itemMeta = itemStack.itemMeta as? BookMeta
        val player = event.player
        if (itemMeta?.title == "Shop") {
            event.itemDrop.remove()
            manager.create(player)
        }
    }

    @EventHandler
    fun onInteraction(event: PlayerInteractEntityEvent) {
        val entity = event.rightClicked
        if (entity is Villager && manager.isShop(entity)) {
            event.isCancelled = true
            if (manager.isOpen(entity)) {
                manager.open(event.player, entity)
            } else if (manager.isPermitted(event.player, entity)) {
                if (event.player.isSneaking) {
                    manager.openModify(entity, event.player)
                    //manager.remove(event.player, entity)
                } else {
                    manager.open(event.player, entity)
                }
            } else {
                entity.shakeHead()
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onInteract(event: PrePlayerAttackEntityEvent) {
        val entity = event.attacked
        val player = event.player
        if (entity is Villager && manager.isShop(entity)) {
            event.isCancelled = true
            if (manager.isPermitted(player, entity)) {
                if (player.isSneaking) {
                    val isOpen = manager.isOpen(entity)
                    manager.setOpen(entity, !isOpen)
                }
            } else {
                entity.shakeHead()
            }
        }
    }

    @EventHandler
    fun onInvincibility(event: EntityDamageEvent) {
        if (event.entity !is Villager) return
        if (manager.isShop(event.entity as Villager)) event.isCancelled = true
    }

    @EventHandler
    fun onCareerChange(event: VillagerCareerChangeEvent) {
        if (manager.isShop(event.entity)) event.isCancelled = true
    }

    private val tradeSelection = mutableMapOf<Villager, Int>()

    @EventHandler
    fun onSelect(event: TradeSelectEvent) {
        val merchant = event.merchant
        val player = event.whoClicked as Player
        val selectedRecipe = merchant.getRecipe(event.index)
        val villager = event.view.topInventory.holder as Villager
        if (manager.isShop(villager)) {
            if (manager.isOpen(villager)) return
            event.isCancelled = true
            event.result = Event.Result.DENY
            if (event.index == 0) {
                if (manager.isPermitted(player, villager)) {
                    if (tradeSelection.containsKey(villager)) {
                        val selected = tradeSelection[villager] ?: return
                        val costItem = event.inventory.getItem(0) ?: return
                        val resultItem = event.inventory.getItem(1) ?: return
                        manager.updateTrade(merchant, selected, costItem, resultItem)
                        tradeSelection.remove(villager)
                        event.inventory.clear()
                        player.closeInventory()
                    } else {
                        val costItem = event.inventory.getItem(0) ?: return
                        val resultItem = event.inventory.getItem(1) ?: return
                        manager.addTrade(merchant, costItem, resultItem)
                        event.inventory.clear()
                        player.closeInventory()
                    }
                }
            } else {
                if (event.index > 0) {
                    val recipes = merchant.recipes.toMutableList()
                    val costItem = selectedRecipe.ingredients.first()
                    if (selectedRecipe.uses == selectedRecipe.maxUses) {
                        recipes.removeAt(event.index)
                        manager.updateTrades(merchant, recipes)
                        player.inventory.addItem(costItem)
                        player.closeInventory()
                    } else {
                        tradeSelection[villager] = event.index
                        val resultItem = selectedRecipe.result
                        event.inventory.setItem(0, costItem)
                        event.inventory.setItem(1, resultItem)
                        (event.whoClicked as Player).updateInventory()

                        /*recipes.removeAt(event.index)
                        manager.updateTrades(merchant, recipes)
                        player.inventory.addItem(costItem, resultItem)
                        player.closeInventory()*/
                    }
                }
            }
        }
    }

    @EventHandler
    fun onVillagerMovement(event: EntityMoveEvent) {
        val entity = event.entity
        if (entity is Villager && manager.isShop(entity)) {
            event.isCancelled = true
        }
    }
}
