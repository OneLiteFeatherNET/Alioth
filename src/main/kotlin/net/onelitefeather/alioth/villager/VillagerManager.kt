package net.onelitefeather.alioth.villager

import net.kyori.adventure.inventory.Book
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.onelitefeather.alioth.Alioth
import net.onelitefeather.alioth.extension.edit
import net.onelitefeather.alioth.extension.legacyDisplay
import net.onelitefeather.alioth.extension.legacyLore
import org.apache.commons.lang.StringUtils
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.Villager
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Merchant
import org.bukkit.inventory.MerchantRecipe
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

class VillagerManager(private val plugin: Alioth) {

    private val shopNamespace = NamespacedKey.minecraft("shop")
    private val ownerNamespace = NamespacedKey.minecraft("owner")
    private val openNamespace = NamespacedKey.minecraft("open")

    private val shopTitle = "<gold>Shop"

    private val displayFormat = "<display> <gray>| <reset><state>"
    private val symbolOpen = "<green>⏼"
    private val symbolClosed = "<red>⏻"

    private val optionAddTrade: MerchantRecipe by lazy {
        createOptionAddTrade()
    }

    init {
        object : BukkitRunnable() {
            override fun run() {
                // shake head if stock is empty
            }
        }.runTaskTimerAsynchronously(plugin, 20 * 3, 20 * 3)
    }

    private fun createOptionAddTrade(): MerchantRecipe {
        return MerchantRecipe(
            ItemStack(Material.LIGHT).edit {
                it.legacyDisplay("&6Add or update trade")
                it.legacyLore(
                    "&7Select this button to add trade to shop",
                    "",
                    " &8- &6First slot &7| &eItem to buy",
                    " &8- &6Second slot &7| &eItem to sale",
                )
            }, 1
        ).apply {
            setIgnoreDiscounts(true)
            addIngredient(ItemStack(Material.AIR))
            addIngredient(ItemStack(Material.AIR))
        }
    }

    fun create(player: Player) {
        val villager = player.world.spawn(player.location, Villager::class.java)
        val shopId = UUID.randomUUID()
        villager.isSilent = true
        villager.profession = Villager.Profession.MASON
        villager.villagerType = Villager.Type.PLAINS
        villager.persistentDataContainer.set(
            ownerNamespace,
            PersistentDataType.STRING,
            player.uniqueId.toString()
        )
        villager.persistentDataContainer.set(
            shopNamespace, PersistentDataType.STRING,
            shopId.toString()
        )
        villager.persistentDataContainer.set(
            openNamespace, PersistentDataType.BOOLEAN,
            false
        )
        updateDisplayName(villager)
        villager.recipes = listOf(optionAddTrade)
    }

    fun open(player: Player, villager: Villager) {
        player.openMerchant(villager, true)
    }

    fun isPermitted(player: Player, entity: Villager): Boolean {
        val persistent = entity.persistentDataContainer
        return persistent.get(ownerNamespace, PersistentDataType.STRING) == player.uniqueId.toString()
    }

    fun isShop(entity: Villager): Boolean {
        val persistent = entity.persistentDataContainer
        return persistent.has(shopNamespace)
    }

    fun isOpen(entity: Villager): Boolean {
        val persistent = entity.persistentDataContainer
        return persistent.getOrDefault(openNamespace, PersistentDataType.BOOLEAN, false)
    }

    private fun updateDisplayName(entity: Villager) {
        entity.customName(
            MiniMessage.miniMessage().deserialize(
                displayFormat, TagResolver.standard(),
                Placeholder.parsed("display", shopTitle), Placeholder.parsed("state", if (isOpen(entity)) symbolOpen else symbolClosed)
            )
        )
    }

    fun addTrade(merchant: Merchant, buy: ItemStack, sale: ItemStack) {
        val recipe = MerchantRecipe(sale, 1)
        recipe.setIgnoreDiscounts(true)
        recipe.addIngredient(buy)
        val recipes = merchant.recipes.toMutableList()
        recipes.add(recipe)
        merchant.recipes = recipes
    }

    fun updateTrade(merchant: Merchant, index: Int, buy: ItemStack, sale: ItemStack) {
        val recipes = merchant.recipes.toMutableList()
        val recipe = MerchantRecipe(sale, 1)
        //recipe.setIgnoreDiscounts(true)
        recipe.addIngredient(buy)
        recipe.priceMultiplier = 10f
        recipes[index] = recipe
        merchant.recipes = recipes
    }

    fun updateTrades(merchant: Merchant, recipe: List<MerchantRecipe>) {
        merchant.recipes = recipe
    }

    fun setOpen(entity: Villager, open: Boolean) {
        val persistent = entity.persistentDataContainer
        persistent[openNamespace, PersistentDataType.BOOLEAN] = open
        if (open) {
            val recipes = entity.recipes.toMutableList()
            recipes.removeAll { it.result == optionAddTrade.result }
            entity.recipes = recipes
        } else {
            entity.recipes = listOf(optionAddTrade) + entity.recipes
        }
        updateDisplayName(entity)
    }

    fun openModify(entity: Villager, player: Player) {
        player.openBook(
            Book.builder()
                .addPage(
                    text(StringUtils.center("======= Info ======", 19))
                        .appendNewline()
                        .append(text("Name: ").append(entity.name().clickEvent(ClickEvent.runCommand("/trader rename ${entity.uniqueId}"))))
                )
                .addPage(text("Admin"))
                .build()
        )
    }

    fun remove(player: Player, entity: Entity) {
        player.world.dropItem(player.location, ItemStack(Material.WRITTEN_BOOK).edit(BookMeta::class.java) {
            it.title = "Shop"
            it.author = player.name
        }).pickupDelay = 0
        entity.remove()
    }
}